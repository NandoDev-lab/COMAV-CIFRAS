package com.nandohypesoft.comav.util

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object MetronomoManager {

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    } catch (_: Exception) {
        null
    }

    private var metronomoJob: Job? = null

    private val _ativo = MutableStateFlow(false)
    val ativo: StateFlow<Boolean> = _ativo.asStateFlow()

    private val _bpm = MutableStateFlow(120)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _pulsoVisual = MutableStateFlow(false)
    val pulsoVisual: StateFlow<Boolean> = _pulsoVisual.asStateFlow()

    private val _batidaAtual = MutableStateFlow(1)
    val batidaAtual: StateFlow<Int> = _batidaAtual.asStateFlow()

    private val tapTimestamps = mutableListOf<Long>()

    fun definirBpm(novoBpm: Int) {
        _bpm.value = novoBpm.coerceIn(30, 240)
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

        metronomoJob = scope.launch(Dispatchers.Default) {
            while (isActive && _ativo.value) {
                val intervaloMs = (60_000L / _bpm.value).coerceAtLeast(100L)
                val batida = _batidaAtual.value

                // Emitir tom e pulso visual
                try {
                    if (batida == 1) {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
                    } else {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
                    }
                } catch (_: Exception) {}

                _pulsoVisual.value = true
                delay(50L)
                _pulsoVisual.value = false

                val restanteIntervalo = intervaloMs - 50L
                if (restanteIntervalo > 0) {
                    delay(restanteIntervalo)
                }

                _batidaAtual.value = if (batida >= 4) 1 else batida + 1
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

    /**
     * Calcula o BPM dinamicamente através do recurso Tap Tempo (toques ritmados do usuário).
     */
    fun registrarTapTempo() {
        val agora = System.currentTimeMillis()

        // Se o último toque foi há mais de 2 segundos, zera a lista de medições
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
