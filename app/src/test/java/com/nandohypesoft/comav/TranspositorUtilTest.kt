package com.nandohypesoft.comav

import com.nandohypesoft.comav.util.TranspositorUtil
import org.junit.Assert.assertEquals
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
    fun testTransporConteudoCompleto() {
        val cifra = """
            [Intro]
            C  G/B  Am7  F
            
            Verso 1
            C             G/B
            Grandioso és Tu
        """.trimIndent()

        val resultado = TranspositorUtil.transporConteudo(cifra, 2)

        val linhaCifraEsperada = "D  A/C#  Bm7  G"
        val versoEsperado = "D             A/C#"

        assert(resultado.contains(linhaCifraEsperada))
        assert(resultado.contains(versoEsperado))
        assert(resultado.contains("Grandioso és Tu"))
    }
}
