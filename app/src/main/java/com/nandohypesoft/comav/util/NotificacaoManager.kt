package com.nandohypesoft.comav.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.nandohypesoft.comav.R
import com.nandohypesoft.comav.model.EscalaCulto
import com.nandohypesoft.comav.model.EscalaMusicoData
import com.nandohypesoft.comav.model.TipoFuncaoEquipe
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificacaoManager {

    private const val CHANNEL_ID = "comav_notificacoes_channel"
    private const val CHANNEL_NAME = "Alertas do COMAV Cifras"

    fun criarCanalNotificacao(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notificações para agendamento e alertas da equipe de louvor"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun dispararNotificacaoLocal(context: Context, titulo: String, mensagem: String, notificationId: Int = 1001) {
        criarCanalNotificacao(context)

        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (_: Exception) {}
    }

    fun registrarPerfilDispositivoFirebase(context: Context, funcao: TipoFuncaoEquipe) {
        try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "device_unknown"
            val firestore = FirebaseFirestore.getInstance()

            val dadosDispositivo = mapOf(
                "dispositivoId" to androidId,
                "funcao" to funcao.name,
                "modelo" to Build.MODEL,
                "atualizadoEm" to System.currentTimeMillis()
            )

            firestore.collection("perfis_dispositivos").document(androidId).set(dadosDispositivo)
        } catch (_: Exception) {}
    }

    fun verificarEAgendarAlertas(
        context: Context,
        funcaoUsuario: TipoFuncaoEquipe,
        escalasCulto: List<EscalaCulto>,
        escalasMusicosMap: Map<String, EscalaMusicoData>
    ) {
        if (funcaoUsuario == TipoFuncaoEquipe.NAO_DEFINIDO) return

        val cal = Calendar.getInstance()
        val diaDaSemana = cal.get(Calendar.DAY_OF_WEEK) // 2 = Segunda, 3 = Terça

        val eDiaAlertaMinistro = (diaDaSemana == Calendar.MONDAY || diaDaSemana == Calendar.TUESDAY)

        val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val hojeStr = formatoIso.format(Date())

        // 1. Verificar cultos futuros cadastrados em escala_musicos_datas sem agendamento do ministro
        if (eDiaAlertaMinistro && (funcaoUsuario == TipoFuncaoEquipe.MINISTRO_CANTOR || funcaoUsuario == TipoFuncaoEquipe.AMBOS)) {
            val datasComMusicosSemAgendamento = escalasMusicosMap.keys.filter { dataIso ->
                dataIso >= hojeStr && escalasCulto.none { it.dataCulto == dataIso }
            }

            if (datasComMusicosSemAgendamento.isNotEmpty()) {
                val dataProxima = datasComMusicosSemAgendamento.sorted().first()
                val partes = dataProxima.split("-")
                val dataBr = if (partes.size == 3) "${partes[2]}/${partes[1]}" else dataProxima

                dispararNotificacaoLocal(
                    context = context,
                    titulo = "⚠️ Lembrete de Agendamento do Culto!",
                    mensagem = "Ministro, você tem culto agendado para $dataBr. Por favor, informe os louvores até terça-feira!",
                    notificationId = 2001
                )
            }
        }
    }

    fun notificarAvisoMusicosNovoCulto(
        context: Context,
        funcaoUsuario: TipoFuncaoEquipe,
        nomeMinistro: String,
        dataBr: String
    ) {
        if (funcaoUsuario == TipoFuncaoEquipe.MUSICO || funcaoUsuario == TipoFuncaoEquipe.AMBOS) {
            dispararNotificacaoLocal(
                context = context,
                titulo = "🎶 Novo Culto Agendado!",
                mensagem = "$nomeMinistro agendou os louvores do culto de $dataBr. Abra o app para ver as cifras e ensaiar!",
                notificationId = 3001
            )
        }
    }
}
