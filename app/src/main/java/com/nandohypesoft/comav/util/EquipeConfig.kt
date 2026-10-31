package com.nandohypesoft.comav.util

import com.nandohypesoft.comav.model.TipoFuncaoEquipe

object EquipeConfig {

    val LISTA_MINISTROS = listOf(
        "VALDINÉIA",
        "FERNANDA",
        "MAÍSA",
        "ISABELE",
        "FABIANA",
        "DANIEL"
    )

    val OPCOES_MINISTROS_SPINNER = listOf("Selecione...", "VALDINÉIA", "FERNANDA", "MAÍSA", "ISABELE", "FABIANA", "DANIEL")

    val OPCOES_VOCAIS_SPINNER = listOf("Selecione...", "VALDINÉIA", "FERNANDA", "MAÍSA", "ISABELE", "FABIANA", "DANIEL", "FERNANDO")

    val OPCOES_TECLADO_SPINNER = listOf("Selecione...", "AGNALDO", "FERNANDO")

    val OPCOES_VIOLAO_SPINNER = listOf("Selecione...", "DANIEL", "FERNANDO")

    val OPCOES_BATERIA_SPINNER = listOf("Selecione...", "VICTOR", "FELIPE")

    fun ehDesenvolvedor(nome: String): Boolean {
        return nome.trim().uppercase().contains("FERNANDO")
    }

    fun ehLider(nome: String): Boolean {
        val nomeLimpo = nome.trim().uppercase()
        return nomeLimpo.contains("VALDINÉIA") || nomeLimpo.contains("VALDINEIA") || ehDesenvolvedor(nome)
    }

    fun obterFuncaoPorNome(nome: String): TipoFuncaoEquipe {
        val nomeLimpo = nome.trim().uppercase()
        return when {
            ehDesenvolvedor(nomeLimpo) || ehLider(nomeLimpo) -> TipoFuncaoEquipe.ADMIN_LIDER
            LISTA_MINISTROS.any { nomeLimpo.contains(it) } -> TipoFuncaoEquipe.MINISTRO_CANTOR
            else -> TipoFuncaoEquipe.MUSICO
        }
    }
}
