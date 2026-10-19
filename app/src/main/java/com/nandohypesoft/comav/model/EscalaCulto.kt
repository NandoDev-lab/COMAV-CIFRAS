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
    val itens: List<ItemEscala> = emptyList(),
    val criadaEm: Long = System.currentTimeMillis()
)
