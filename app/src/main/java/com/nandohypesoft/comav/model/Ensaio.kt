package com.nandohypesoft.comav.model

data class Ensaio(
    val id: String = "",
    val dataEnsaio: String = "", // Formato yyyy-MM-dd
    val diaSemana: String = "",  // Quinta-feira, Sábado, etc.
    val linkYoutube1: String = "",
    val linkYoutube2: String = "",
    val status: String = "AGENDADO", // "AGENDADO", "CONFIRMADO", "CANCELADO"
    val criadaEm: Long = System.currentTimeMillis()
)
