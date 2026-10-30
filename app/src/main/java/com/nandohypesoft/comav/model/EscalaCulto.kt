package com.nandohypesoft.comav.model

data class ItemEscala(
    val tituloMusica: String = "",
    val cantor: String = "",
    val ehExtra: Boolean = false,
    val identificadorMusica: Long = 0L
)

data class EscalaCulto(
    val id: String = "",
    val nomeMinistro: String = "",
    val dataCulto: String = "", // formato "yyyy-MM-dd"
    val diaSemana: String = "",
    val vocal1: String = "",
    val vocal2: String = "",
    val vocal3: String = "",
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = "",
    val baixista: String = "",
    val itens: List<ItemEscala> = emptyList(),
    val statusPreenchimento: String = "PENDENTE_PREENCHIMENTO",
    val criadaEm: Long = System.currentTimeMillis(),
    val preenchidaEm: Long = 0L
) {
    val vocais: String
        get() = listOf(vocal1, vocal2, vocal3).filter { it.isNotBlank() && it != "Selecione..." }.joinToString(", ")
}
