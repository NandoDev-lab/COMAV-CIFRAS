package com.nandohypesoft.comav

import com.nandohypesoft.comav.ui.QuebraAutomatica
import org.junit.Assert.assertEquals
import org.junit.Test

class QuebraAutomaticaTest {

    @Test
    fun testarQuebraInteligenteSequencialNMaisUm() {
        // Linha 1 longa que vai quebrar em 2 partes (C1 e C2), seguida por Linha 2 normal (C3)
        val cifraExemplo = "A          D          E          G\nO mundo é um oceano minha carne é um furacão\nBm          A\nSegunda linha da musica"
        val resultado = QuebraAutomatica.quebraInteligente(cifraExemplo, maxChars = 20)

        assert(resultado.isNotEmpty())
        
        // Verificar índices sequenciais n+1: C(1), L(1), C(2), L(2), C(3), L(3)
        val temC1 = resultado.any { it.startsWith("C(1):") }
        val temL1 = resultado.any { it.startsWith("L(1):") }
        val temC2 = resultado.any { it.startsWith("C(2):") }
        val temL2 = resultado.any { it.startsWith("L(2):") }
        val temC3 = resultado.any { it.startsWith("C(3):") }
        val temL3 = resultado.any { it.startsWith("L(3):") }

        assertEquals(true, temC1)
        assertEquals(true, temL1)
        assertEquals(true, temC2)
        assertEquals(true, temL2)
        assertEquals(true, temC3)
        assertEquals(true, temL3)
    }
}
