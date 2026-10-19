package com.nandohypesoft.comav.model

data class EstatisticaMusica(
    val tituloMusica: String,
    val totalUso: Int = 0,
    val repeticoesPorMesAno: Map<String, Int> = emptyMap(), // ex: "10/2026" -> 3
    val ministroMaisFrequente: String = "N/D",
    val cantorMaisFrequente: String = "N/D"
)
