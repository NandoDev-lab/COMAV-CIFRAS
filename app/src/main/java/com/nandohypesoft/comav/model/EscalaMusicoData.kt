package com.nandohypesoft.comav.model

data class EscalaMusicoData(
    val dataIso: String = "", // formato "yyyy-MM-dd"
    val nomeMinistro: String = "",
    val vocal1: String = "",
    val vocal2: String = "",
    val vocal3: String = "",
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = "",
    val baixista: String = ""
) {
    val vocais: String
        get() = listOf(vocal1, vocal2, vocal3).filter { it.isNotBlank() && it != "Selecione..." }.joinToString(", ")
}
