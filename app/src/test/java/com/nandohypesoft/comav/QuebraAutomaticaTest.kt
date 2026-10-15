package com.nandohypesoft.comav

import com.nandohypesoft.comav.ui.QuebraAutomatica
import org.junit.Assert.assertEquals
import org.junit.Test

class QuebraAutomaticaTest {

    @Test
    fun testarQuebraInteligente() {
        val cifraExemplo = "A          D\nO mundo é um oceano minha carne é um furacão"
        val resultado = QuebraAutomatica.quebraInteligente(cifraExemplo, maxChars = 20)

        assert(resultado.isNotEmpty())
        // Verificar que contem C(1) e L(1)
        val temC1 = resultado.any { it.startsWith("C(1):") }
        val temL1 = resultado.any { it.startsWith("L(1):") }
        val temC3 = resultado.any { it.startsWith("C(3):") }
        val temL3 = resultado.any { it.startsWith("L(3):") }

        assertEquals(true, temC1)
        assertEquals(true, temL1)
        assertEquals(true, temC3)
        assertEquals(true, temL3)
    }
}
