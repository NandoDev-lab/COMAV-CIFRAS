package com.nandohypesoft.comav

import com.nandohypesoft.comav.ui.CifraParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CifraParserTest {

    @Test
    fun testarParseCifraComAcentosEValores() {
        val cifraExemplo = "[Refrão]\nG          D          Em\nO mundo é um oceano com águas profundas"
        val resultado = CifraParser.parseCifra(cifraExemplo)

        assertEquals(2, resultado.size)
        assertEquals(true, resultado[0].isHeader)
        assertEquals("[Refrão]", resultado[0].letra)

        assertEquals("O mundo é um oceano com águas profundas", resultado[1].letra)
        val acordes = resultado[1].acordes
        assertEquals(3, acordes.size)
        assertEquals("G", acordes[0].nome)
        assertEquals(0, acordes[0].charIndex)
        assertEquals("D", acordes[1].nome)
        assertEquals(11, acordes[1].charIndex)
        assertEquals("Em", acordes[2].nome)
        assertEquals(22, acordes[2].charIndex)
    }

    @Test
    fun testarLinhaApenasComAcordes() {
        val cifraExemplo = "G    C    D"
        val resultado = CifraParser.parseCifra(cifraExemplo)

        assertEquals(1, resultado.size)
        assertEquals("", resultado[0].letra)
        assertEquals(3, resultado[0].acordes.size)
        assertEquals("G", resultado[0].acordes[0].nome)
    }
}
