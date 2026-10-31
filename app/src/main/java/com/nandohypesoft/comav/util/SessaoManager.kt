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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MembroSessao(
    val dispositivoId: String = "",
    val nomeUsuario: String = "",
    val funcao: String = "",
    val status: String = "OFFLINE",
    val horarioInicio: String = "",
    val inicioEm: Long = 0L,
    val fimEm: Long = 0L
)

object SessaoManager {

    private var listenerSessoes: ListenerRegistration? = null
    private var listenerForaHorario: ListenerRegistration? = null
    private var sessaoAtivaId: String? = null

    private val _membrosConectados = MutableStateFlow<List<MembroSessao>>(emptyList())
    val membrosConectados: StateFlow<List<MembroSessao>> = _membrosConectados.asStateFlow()

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

            val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
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

            // Se for fora do horário comercial (antes das 07:30 ou após as 21:00), registrar histórico
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

        val tempoInicioEscuta = System.currentTimeMillis() - 5000L

        try {
            val myAndroidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
            val firestore = FirebaseFirestore.getInstance()

            // Escutar sessões ativas (online/offline) em tempo real
            listenerSessoes?.remove()
            listenerSessoes = firestore.collection("sessoes_ativas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val listaMembros = mutableListOf<MembroSessao>()
                    val sdfHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                    for (doc in snapshot.documents) {
                        val dispositivoId = doc.getString("dispositivoId") ?: ""
                        val nome = doc.getString("nomeUsuario") ?: "Membro"
                        val funcao = doc.getString("funcao") ?: "Membro"
                        val status = doc.getString("status") ?: "OFFLINE"
                        val horInicio = doc.getString("horarioInicio") ?: ""
                        val inicioEm = doc.getLong("inicioEm") ?: 0L
                        val fimEm = doc.getLong("fimEm") ?: 0L

                        val nomeExibicao = if (dispositivoId == myAndroidId && !nome.contains("(Você)")) "$nome (Você)" else nome

                        if (nome.isNotBlank()) {
                            listaMembros.add(
                                MembroSessao(
                                    dispositivoId = dispositivoId,
                                    nomeUsuario = nomeExibicao,
                                    funcao = funcao,
                                    status = status,
                                    horarioInicio = horInicio,
                                    inicioEm = inicioEm,
                                    fimEm = fimEm
                                )
                            )
                        }
                    }

                    // Garantir que o próprio admin logado apareça na lista se o snapshot remoto atrasar
                    if (myAndroidId.isNotBlank() && listaMembros.none { it.dispositivoId == myAndroidId }) {
                        listaMembros.add(
                            MembroSessao(
                                dispositivoId = myAndroidId,
                                nomeUsuario = "$nomeUsuario (Você)",
                                funcao = tipoFuncao.titulo,
                                status = "ONLINE",
                                horarioInicio = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                                inicioEm = System.currentTimeMillis()
                            )
                        )
                    }

                    _membrosConectados.value = listaMembros

                    // Processar alterações em tempo real para notificações individuais com horários exatos
                    for (change in snapshot.documentChanges) {
                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) continue

                        val doc = change.document
                        val dispositivoId = doc.getString("dispositivoId") ?: ""
                        if (dispositivoId == myAndroidId) continue

                        val fimEm = doc.getLong("fimEm") ?: 0L
                        val inicioEm = doc.getLong("inicioEm") ?: 0L
                        val eventoTempo = maxOf(fimEm, inicioEm)

                        if (eventoTempo < tempoInicioEscuta) continue

                        val nome = doc.getString("nomeUsuario") ?: "Membro"
                        val funcao = doc.getString("funcao") ?: "Membro"
                        val status = doc.getString("status") ?: ""
                        val horInicio = doc.getString("horarioInicio") ?: ""

                        if (status == "ONLINE") {
                            val horaFormatada = if (inicioEm > 0) sdfHora.format(Date(inicioEm)) else horInicio
                            NotificacaoManager.dispararNotificacaoLocal(
                                context = context,
                                titulo = "🟢 Membro Conectado às $horaFormatada",
                                mensagem = "$funcao $nome abriu o app às $horaFormatada",
                                notificationId = (dispositivoId.hashCode() and 0x7FFFFFFF)
                            )
                        } else if (status == "OFFLINE") {
                            val horaFimFormatada = if (fimEm > 0) sdfHora.format(Date(fimEm)) else sdfHora.format(Date())
                            NotificacaoManager.dispararNotificacaoLocal(
                                context = context,
                                titulo = "🔴 Sessão Encerrada às $horaFimFormatada",
                                mensagem = "$funcao $nome fechou o app às $horaFimFormatada",
                                notificationId = (dispositivoId.hashCode() and 0x7FFFFFFF) + 100
                            )
                        }
                    }
                }
        } catch (_: Exception) {}
    }

    fun obterHistoricoAcessos(onResultado: (List<MembroSessao>) -> Unit) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("sessoes_ativas").get().addOnSuccessListener { snapshot ->
                val resultado = mutableListOf<MembroSessao>()
                for (doc in snapshot.documents) {
                    val dispositivoId = doc.getString("dispositivoId") ?: ""
                    val nome = doc.getString("nomeUsuario") ?: ""
                    val funcao = doc.getString("funcao") ?: ""
                    val status = doc.getString("status") ?: "OFFLINE"
                    val horInicio = doc.getString("horarioInicio") ?: ""
                    val inicioEm = doc.getLong("inicioEm") ?: 0L
                    val fimEm = doc.getLong("fimEm") ?: 0L

                    if (nome.isNotBlank()) {
                        resultado.add(
                            MembroSessao(
                                dispositivoId = dispositivoId,
                                nomeUsuario = nome,
                                funcao = funcao,
                                status = status,
                                horarioInicio = horInicio,
                                inicioEm = inicioEm,
                                fimEm = fimEm
                            )
                        )
                    }
                }
                onResultado(resultado.sortedByDescending { maxOf(it.inicioEm, it.fimEm) })
            }.addOnFailureListener {
                onResultado(emptyList())
            }
        } catch (_: Exception) {
            onResultado(emptyList())
        }
    }

    fun pararEscuta() {
        listenerSessoes?.remove()
        listenerForaHorario?.remove()
        listenerSessoes = null
        listenerForaHorario = null
    }
}
