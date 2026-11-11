package com.nandohypesoft.comav.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class CompassoMetronomo(val maxBatidas: Int, val descricao: String) {
    TERNARIO(3, "3/4 Ternário"),
    QUATERNARIO(4, "4/4 Quaternário")
}

object MetronomoManager {

    private const val SAMPLE_RATE = 44100

    // Buffers PCM de áudio sintético para som de clique de metrônomo real (Woodblock/Click)
    private val pcmForte: ShortArray by lazy { gerarPcmClick(freq = 1500.0, duracaoMs = 15) }
    private val pcmFraco: ShortArray by lazy { gerarPcmClick(freq = 850.0, duracaoMs = 12) }

    private var audioTrackForte: AudioTrack? = null
    private var audioTrackFraco: AudioTrack? = null

    private var metronomoJob: Job? = null

    private val _ativo = MutableStateFlow(false)
    val ativo: StateFlow<Boolean> = _ativo.asStateFlow()

    private val _bpm = MutableStateFlow(120)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _compasso = MutableStateFlow(CompassoMetronomo.QUATERNARIO)
    val compasso: StateFlow<CompassoMetronomo> = _compasso.asStateFlow()

    private val _pulsoVisual = MutableStateFlow(false)
    val pulsoVisual: StateFlow<Boolean> = _pulsoVisual.asStateFlow()

    private val _batidaAtual = MutableStateFlow(1)
    val batidaAtual: StateFlow<Int> = _batidaAtual.asStateFlow()

    private val tapTimestamps = mutableListOf<Long>()

    private fun inicializarAudio() {
        if (audioTrackForte != null && audioTrackFraco != null) return

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            audioTrackForte = AudioTrack(
                audioAttributes, audioFormat, pcmForte.size * 2,
                AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE
            ).apply {
                write(pcmForte, 0, pcmForte.size)
            }

            audioTrackFraco = AudioTrack(
                audioAttributes, audioFormat, pcmFraco.size * 2,
                AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE
            ).apply {
                write(pcmFraco, 0, pcmFraco.size)
            }
        } catch (_: Exception) {
        }
    }

    private fun tocarClique(forte: Boolean) {
        inicializarAudio()
        val track = if (forte) audioTrackForte else audioTrackFraco
        try {
            track?.stop()
            track?.reloadStaticData()
            track?.play()
        } catch (_: Exception) {}
    }

    private fun gerarPcmClick(freq: Double, duracaoMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (duracaoMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Curva de decaimento exponencial para som percussivo de madeira (woodblock)
            val envelope = Math.exp(-t * 300.0)
            val sampleVal = sin(2.0 * Math.PI * freq * t) * envelope
            buffer[i] = (sampleVal * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    fun definirBpm(novoBpm: Int) {
        _bpm.value = novoBpm.coerceIn(30, 240)
    }

    fun definirCompasso(novoCompasso: CompassoMetronomo) {
        _compasso.value = novoCompasso
        if (_batidaAtual.value > novoCompasso.maxBatidas) {
            _batidaAtual.value = 1
        }
    }

    fun alternarMetronomo(scope: CoroutineScope) {
        if (_ativo.value) {
            parar()
        } else {
            iniciar(scope)
        }
    }

    fun iniciar(scope: CoroutineScope) {
        parar()
        _ativo.value = true
        _batidaAtual.value = 1

        metronomoJob = scope.launch(Dispatchers.Default) {
            var targetNanos = System.nanoTime()

            while (isActive && _ativo.value) {
                val bpmAtual = _bpm.value
                val maxBatidas = _compasso.value.maxBatidas
                val batida = _batidaAtual.value

                val periodNanos = (60_000_000_000.0 / bpmAtual).toLong()

                // 1. Tocar som de clique do metrônomo (forte na 1ª batida, fraco nas demais)
                tocarClique(forte = (batida == 1))

                // 2. Acionar pulso visual
                _pulsoVisual.value = true

                // 3. Atualizar próxima batida
                val proximaBatida = if (batida >= maxBatidas) 1 else batida + 1
                _batidaAtual.value = proximaBatida

                // 4. Calcular próximo carimbo de tempo absoluto (Drift-Free Nanosecond Precision)
                targetNanos += periodNanos
                val sleepNanos = targetNanos - System.nanoTime()

                // Apagar o pulso visual após 40ms
                delay(40L)
                _pulsoVisual.value = false

                val restanteSleepNanos = sleepNanos - 40_000_000L
                if (restanteSleepNanos > 0) {
                    delay(restanteSleepNanos / 1_000_000L)
                } else {
                    targetNanos = System.nanoTime()
                }
            }
        }
    }

    fun parar() {
        _ativo.value = false
        metronomoJob?.cancel()
        metronomoJob = null
        _pulsoVisual.value = false
        _batidaAtual.value = 1
    }

    fun registrarTapTempo() {
        val agora = System.currentTimeMillis()

        if (tapTimestamps.isNotEmpty() && agora - tapTimestamps.last() > 2000L) {
            tapTimestamps.clear()
        }

        tapTimestamps.add(agora)

        if (tapTimestamps.size > 5) {
            tapTimestamps.removeAt(0)
        }

        if (tapTimestamps.size >= 2) {
            val intervalos = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                intervalos.add(tapTimestamps[i] - tapTimestamps[i - 1])
            }
            val mediaIntervaloMs = intervalos.average()
            if (mediaIntervaloMs > 0) {
                val bpmCalculado = (60_000.0 / mediaIntervaloMs).toInt().coerceIn(30, 240)
                _bpm.value = bpmCalculado
            }
        }
    }
}
