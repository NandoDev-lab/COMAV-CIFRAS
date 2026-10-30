package com.nandohypesoft.comav.model

data class EscalaMusicoData(
    val dataIso: String = "", // formato "yyyy-MM-dd"
    val nomeMinistro: String = "",
    val vocais: String = "",
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = "",
    val baixista: String = ""
)
