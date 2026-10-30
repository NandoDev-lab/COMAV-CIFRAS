package com.nandohypesoft.comav.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nandohypesoft.comav.model.TipoFuncaoEquipe
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SessaoManager {

    private var listenerSessoes: ListenerRegistration? = null
    private var listenerForaHorario: ListenerRegistration? = null
    private var sessaoAtivaId: String? = null

    fun ehHorarioComercial(): Boolean {
        val cal = Calendar.getInstance()
        val hora = cal.get(Calendar.HOUR_OF_DAY)
        val minuto = cal.get(Calendar.MINUTE)
        val totalMinutos = hora * 60 + minuto

        val inicioComercial = 7 * 60 + 30 // 07:30 = 450 min
        val fimComercial = 21 * 60 // 21:00 = 1260 min

        return totalMinutos in inicioComercial..fimComercial
    }

    fun iniciarSessaoAtiva(context: Context, nomeUsuario: String, tipoFuncao: TipoFuncaoEquipe) {
        try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "device_unknown"
            sessaoAtivaId = androidId
            val firestore = FirebaseFirestore.getInstance()

            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formatoDataIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val agora = Date()
            val horaStr = formatoHora.format(agora)
            val dataIsoStr = formatoDataIso.format(agora)

            val dadosSessao = mapOf(
                "dispositivoId" to androidId,
                "nomeUsuario" to nomeUsuario.trim().uppercase(),
                "funcao" to tipoFuncao.titulo,
                "status" to "ONLINE",
                "horarioInicio" to horaStr,
                "inicioEm" to System.currentTimeMillis(),
                "ehHorarioComercial" to ehHorarioComercial()
            )

            // Registrar a sessão em tempo real
            firestore.collection("sessoes_ativas").document(androidId).set(dadosSessao)

            // Se for fora do horário comercial (antes das 07:30 ou após as 21:00), registrar histórico para aviso posterior
            if (!ehHorarioComercial() && nomeUsuario.isNotBlank()) {
                val idHistorico = "${androidId}_${System.currentTimeMillis()}"
                val dadosHistorico = mapOf(
                    "id" to idHistorico,
                    "dispositivoId" to androidId,
                    "nomeUsuario" to nomeUsuario.trim().uppercase(),
                    "funcao" to tipoFuncao.titulo,
                    "horario" to horaStr,
                    "dataIso" to dataIsoStr,
                    "timestamp" to System.currentTimeMillis(),
                    "notificado" to false
                )
                firestore.collection("historico_acessos_forahorario").document(idHistorico).set(dadosHistorico)
            }
        } catch (_: Exception) {}
    }

    fun encerrarSessaoAtiva(context: Context) {
        try {
            val androidId = sessaoAtivaId ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: return
            val firestore = FirebaseFirestore.getInstance()

            val dadosEncerramento = mapOf(
                "status" to "OFFLINE",
                "fimEm" to System.currentTimeMillis()
            )

            firestore.collection("sessoes_ativas").document(androidId).update(dadosEncerramento)
        } catch (_: Exception) {}
    }

    fun escutarAtividadesSessaoFirebase(context: Context, tipoFuncao: TipoFuncaoEquipe, nomeUsuario: String) {
        val ehLiderOuDev = (tipoFuncao == TipoFuncaoEquipe.ADMIN_LIDER || nomeUsuario.trim().uppercase().contains("FERNANDO"))
        if (!ehLiderOuDev) return

        val tempoInicioEscuta = System.currentTimeMillis() - 5000L // Ignorar eventos ocorridos antes da abertura do app

        try {
            val myAndroidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
            val firestore = FirebaseFirestore.getInstance()

            // 1. Escutar sessões ativas (online/offline) em tempo real
            listenerSessoes?.remove()
            listenerSessoes = firestore.collection("sessoes_ativas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    for (change in snapshot.documentChanges) {
                        // Ignorar documentos existentes antigos carregados no snapshot inicial
                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) continue

                        val doc = change.document
                        val dispositivoId = doc.getString("dispositivoId") ?: ""
                        if (dispositivoId == myAndroidId) continue // ignorar o próprio dispositivo

                        val fimEm = doc.getLong("fimEm") ?: 0L
                        val inicioEm = doc.getLong("inicioEm") ?: 0L
                        val eventoTempo = maxOf(fimEm, inicioEm)

                        if (eventoTempo < tempoInicioEscuta) continue

                        val nome = doc.getString("nomeUsuario") ?: "Membro"
                        val funcao = doc.getString("funcao") ?: "Membro"
                        val status = doc.getString("status") ?: ""
                        val emHorarioComercial = doc.getBoolean("ehHorarioComercial") ?: true

                        if (status == "ONLINE" && emHorarioComercial && ehHorarioComercial()) {
                            NotificacaoManager.dispararNotificacaoLocal(
                                context = context,
                                titulo = "🟢 Membro Conectado",
                                mensagem = "$funcao $nome está com o app aberto",
                                notificationId = (dispositivoId.hashCode() and 0x7FFFFFFF)
                            )
                        } else if (status == "OFFLINE" && emHorarioComercial && ehHorarioComercial()) {
                            NotificacaoManager.dispararNotificacaoLocal(
                                context = context,
                                titulo = "🔴 Sessão Encerrada",
                                mensagem = "$funcao $nome fechou o app (Sessão encerrada)",
                                notificationId = (dispositivoId.hashCode() and 0x7FFFFFFF) + 100
                            )
                        }
                    }
                }
        } catch (_: Exception) {}
    }

    fun pararEscuta() {
        listenerSessoes?.remove()
        listenerForaHorario?.remove()
        listenerSessoes = null
        listenerForaHorario = null
    }
}
