package com.nandohypesoft.comav

import com.nandohypesoft.comav.ui.QuebraAutomatica
import org.junit.Assert.assertEquals
import org.junit.Test

class QuebraAutomaticaTest {

    @Test
    fun testarQuebraInteligentePuloDeDois() {
        // Linha 1 longa que vai quebrar em 2 partes (C1 e C3), seguida por Linha 2 normal (C5)
        val cifraExemplo = "A          D          E          G\nO mundo é um oceano minha carne é um furacão\nBm          A\nSegunda linha da musica"
        val resultado = QuebraAutomatica.quebraInteligente(cifraExemplo, maxChars = 20)

        assert(resultado.isNotEmpty())
        
        // Verificar indices com pulo de 2: C(1), L(1), C(3), L(3), C(5), L(5)
        val temC1 = resultado.any { it.startsWith("C(1):") }
        val temL1 = resultado.any { it.startsWith("L(1):") }
        val temC3 = resultado.any { it.startsWith("C(3):") }
        val temL3 = resultado.any { it.startsWith("L(3):") }
        val temC5 = resultado.any { it.startsWith("C(5):") }
        val temL5 = resultado.any { it.startsWith("L(5):") }

        assertEquals(true, temC1)
        assertEquals(true, temL1)
        assertEquals(true, temC3)
        assertEquals(true, temL3)
        assertEquals(true, temC5)
        assertEquals(true, temL5)
    }
}
