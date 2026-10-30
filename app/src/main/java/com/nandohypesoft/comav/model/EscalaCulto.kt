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
    val dataCulto: String = "", // formato "yyyy-MM-dd" para ordenação e filtro
    val diaSemana: String = "", // ex: Domingo, Quarta-feira
    val vocais: String = "",    // ex: "VAL, FABIANA"
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = "",
    val baixista: String = "",
    val itens: List<ItemEscala> = emptyList(),
    val statusPreenchimento: String = "PENDENTE_PREENCHIMENTO", // "PENDENTE_PREENCHIMENTO" ou "PREENCHIDO"
    val criadaEm: Long = System.currentTimeMillis(),
    val preenchidaEm: Long = 0L
)
