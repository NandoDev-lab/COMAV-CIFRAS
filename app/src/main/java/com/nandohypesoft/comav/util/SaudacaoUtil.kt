package com.nandohypesoft.comav.util

object SaudacaoUtil {

    private val LISTA_MULHERES = setOf(
        "VAL", "NANDA", "FABIANA", "ISABELA", "ISABELE", "MAISA", "PASTORA VAL"
    )

    private val LISTA_HOMENS = setOf(
        "FERNANDO", "DANIEL", "VICTOR", "AGNALDO", "FELIPE", "LUCAS", "MATEUS", "GABRIEL", "SAMUEL", "RAFAEL", "RODRIGO", "GUSTAVO"
    )

    fun ehMulher(nome: String): Boolean {
        val nomeLimpo = nome.trim().uppercase()
        if (nomeLimpo.isBlank()) return false

        val primeiroNome = nomeLimpo.split(" ").firstOrNull() ?: nomeLimpo

        return when {
            LISTA_MULHERES.contains(primeiroNome) || LISTA_MULHERES.contains(nomeLimpo) -> true
            LISTA_HOMENS.contains(primeiroNome) || LISTA_HOMENS.contains(nomeLimpo) -> false
            primeiroNome.endsWith("A") -> true
            else -> false
        }
    }

    fun gerarSaudacao(nome: String): String {
        val nomeLimpo = nome.trim().uppercase()
        if (nomeLimpo.isBlank()) return "Bem-vindo(a) de volta!"

        val prefixo = if (ehMulher(nomeLimpo)) "Bem-vinda de volta" else "Bem-vindo de volta"
        return "$prefixo, $nomeLimpo!"
    }
}
