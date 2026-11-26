package com.nandohypesoft.comav

import com.nandohypesoft.comav.ui.QuebraAutomatica
import org.junit.Assert.assertEquals
import org.junit.Test

class QuebraAutomaticaTest {

    @Test
    fun testarQuebraInteligenteBlocosPuloDeDois() {
        // Linha 1 longa que vai quebrar em 2 partes (1 e 3), seguida por Linha 2 normal (5)
        val cifraExemplo = "A          D          E          G\nO mundo é um oceano minha carne é um furacão\nBm          A\nSegunda linha da musica"
        val resultado = QuebraAutomatica.quebraInteligente(cifraExemplo, maxChars = 20)

        assert(resultado.isNotEmpty())
        
        val indices = resultado.map { it.indice }
        assertEquals(true, indices.contains(1))
        assertEquals(true, indices.contains(3))
        assertEquals(true, indices.contains(5))
    }

    @Test
    fun testarQuebraInteligenteEstruturada() {
        val linhas = com.nandohypesoft.comav.ui.CifraParser.parseCifra("G          D          Em\nO mundo é um oceano minha carne é um furacão")
        val resultado = QuebraAutomatica.quebraInteligenteEstruturada(linhas, maxChars = 20)

        assert(resultado.size > 1)
        val primeiroChunk = resultado[0]
        assertEquals(true, primeiroChunk.letra.isNotBlank())
        assert(primeiroChunk.acordes.isNotEmpty())
    }
}
