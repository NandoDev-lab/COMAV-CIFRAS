package com.nandohypesoft.comav

import com.nandohypesoft.comav.ui.components.BaseDadosAcordes
import com.nandohypesoft.comav.ui.components.PosicaoInversao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChordDiagramTest {

    private val acordesParaTestar = listOf(
        "C", "Cm", "C#", "C#m", "D", "Dm", "D#", "D#m", "Eb", "E", "Em",
        "F", "Fm", "F#", "F#m", "G", "Gm", "G#", "G#m", "Ab", "A", "Am",
        "A#", "A#m", "Bb", "B", "Bm"
    )

    @Test
    fun testNotasTriadeMaioresEMenores() {
        // C Major: C (0), E (4), G (7)
        val triadeC = BaseDadosAcordes.obterNotasTriade("C")
        assertEquals(0, triadeC.raiz)
        assertEquals(4, triadeC.terca)
        assertEquals(7, triadeC.quinta)

        // D Major: D (2), F# (6), A (9)
        val triadeD = BaseDadosAcordes.obterNotasTriade("D")
        assertEquals(2, triadeD.raiz)
        assertEquals(6, triadeD.terca)
        assertEquals(9, triadeD.quinta)

        // G Major: G (7), B (11), D (2)
        val triadeG = BaseDadosAcordes.obterNotasTriade("G")
        assertEquals(7, triadeG.raiz)
        assertEquals(11, triadeG.terca)
        assertEquals(2, triadeG.quinta)

        // A Minor: A (9), C (0), E (4)
        val triadeAm = BaseDadosAcordes.obterNotasTriade("Am")
        assertEquals(9, triadeAm.raiz)
        assertEquals(0, triadeAm.terca)
        assertEquals(4, triadeAm.quinta)
    }

    @Test
    fun testDiferenciacaoNasTresPosicoesDoTeclado() {
        for (acorde in acordesParaTestar) {
            val triade = BaseDadosAcordes.obterNotasTriade(acorde)

            val notasPos1 = triade.obterNotasComOitavas(PosicaoInversao.FUNDAMENTAL)
            val notasPos2 = triade.obterNotasComOitavas(PosicaoInversao.PRIMEIRA_INVERSAO)
            val notasPos3 = triade.obterNotasComOitavas(PosicaoInversao.SEGUNDA_INVERSAO)

            // As notas em 1ª, 2ª e 3ª posições devem ser fisicamente distintas nas oitavas
            assertNotEquals("Pos1 e Pos2 idênticas para $acorde", notasPos1, notasPos2)
            assertNotEquals("Pos2 e Pos3 idênticas para $acorde", notasPos2, notasPos3)
            assertNotEquals("Pos1 e Pos3 idênticas para $acorde", notasPos1, notasPos3)

            // Garantir ordem estritamente ascendente em cada inversão no teclado
            assertTrue("Notas fora de ordem na Pos1 para $acorde", notasPos1[0] < notasPos1[1] && notasPos1[1] < notasPos1[2])
            assertTrue("Notas fora de ordem na Pos2 para $acorde", notasPos2[0] < notasPos2[1] && notasPos2[1] < notasPos2[2])
            assertTrue("Notas fora de ordem na Pos3 para $acorde", notasPos3[0] < notasPos3[1] && notasPos3[1] < notasPos3[2])
        }
    }

    @Test
    fun testPosicoesViolaoValidas() {
        for (acorde in acordesParaTestar) {
            for (inversao in PosicaoInversao.entries) {
                val posViolao = BaseDadosAcordes.buscarPosicaoViolao(acorde, inversao)
                assertNotNull("Posição nula para $acorde em ${inversao.titulo}", posViolao)
                assertEquals("Violão deve ter exatamente 6 cordas para $acorde", 6, posViolao!!.cordas.size)
            }
        }
    }

    @Test
    fun testFormatacaoTextoDasNotas() {
        val triadeD = BaseDadosAcordes.obterNotasTriade("D")
        assertEquals("Ré + Fá# + Lá", triadeD.formatarNotas(PosicaoInversao.FUNDAMENTAL))
        assertEquals("Fá# + Lá + Ré", triadeD.formatarNotas(PosicaoInversao.PRIMEIRA_INVERSAO))
        assertEquals("Lá + Ré + Fá#", triadeD.formatarNotas(PosicaoInversao.SEGUNDA_INVERSAO))
    }

    @Test
    fun testSiMenorTresNotasVisiveisNoTeclado() {
        val triadeBm = BaseDadosAcordes.obterNotasTriade("Bm")
        val notasPos3 = triadeBm.obterNotasComOitavas(PosicaoInversao.SEGUNDA_INVERSAO)

        assertEquals("Bm em 3ª Posição deve ter 3 notas", 3, notasPos3.size)
        assertTrue("Todas as 3 notas de Bm em 3ª Posição devem caber no teclado de 2 oitavas (0..23)", notasPos3.all { it in 0..23 })
    }
}
