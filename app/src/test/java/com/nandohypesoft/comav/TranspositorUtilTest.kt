package com.nandohypesoft.comav

import com.nandohypesoft.comav.util.TranspositorUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranspositorUtilTest {

    @Test
    fun testTransporAcordesIndividuais() {
        assertEquals("D", TranspositorUtil.transporAcordeToken("C", 2))
        assertEquals("D", TranspositorUtil.transporAcordeToken("C#", 1))
        assertEquals("F", TranspositorUtil.transporAcordeToken("G", -2))
        assertEquals("Bm7", TranspositorUtil.transporAcordeToken("Am7", 2))
        assertEquals("A/C#", TranspositorUtil.transporAcordeToken("G/B", 2))
    }

    @Test
    fun testTransporLinhaCifra() {
        val linhaOriginal = "C      G/B      Am7    F"
        val linhaEsperada = "D      A/C#     Bm7    G"
        assertEquals(linhaEsperada, TranspositorUtil.transporLinha(linhaOriginal, 2))
    }

    @Test
    fun testDiferenciacaoEntreLinhaCifraELetra() {
        assertTrue(TranspositorUtil.ehLinhaDeCifra("C    G/B    Am7    F"))
        assertTrue(TranspositorUtil.ehLinhaDeCifra("Am  C  D  G"))

        assertTrue(!TranspositorUtil.ehLinhaDeCifra("Em Tua presença há plenitude de alegria"))
        assertTrue(!TranspositorUtil.ehLinhaDeCifra("A Ele a glória e o louvor"))
        assertTrue(!TranspositorUtil.ehLinhaDeCifra("Grandioso és Tu meu Deus Senhor"))

        val fraseLetra = "Em Tua presença há paz"
        assertEquals(fraseLetra, TranspositorUtil.transporLinha(fraseLetra, 2))
    }

    @Test
    fun testTransporConteudoCompleto() {
        val cifra = """
            [Intro]
            C  G/B  Am7  F
            
            Verso 1
            C             G/B
            Grandioso és Tu
        """.trimIndent()

        val resultado = TranspositorUtil.transporConteudo(cifra, 2)

        assertTrue(resultado.contains("A/C#"))
        assertTrue(resultado.contains("Bm7"))
        assertTrue(resultado.contains("Grandioso és Tu"))
    }
}
