package com.nandohypesoft.comav.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

enum class InstrumentoDiagrama {
    VIOLAO,
    TECLADO
}

enum class PosicaoInversao(val id: Int, val titulo: String, val descricao: String) {
    FUNDAMENTAL(1, "1ª Posição", "Fundamental"),
    PRIMEIRA_INVERSAO(2, "2ª Posição", "1ª Inversão"),
    SEGUNDA_INVERSAO(3, "3ª Posição", "2ª Inversão")
}

data class PosicaoViolao(
    val pestanaFret: Int = 0,
    // Trastes para as 6 cordas da 6ª (E grave) para a 1ª (E agudo). -1 = abafada (X), 0 = solta (O), 1..5 = traste
    val cordas: List<Int>
)

data class PosicaoTeclado(
    val notas: List<Int>
)

data class TriadeNotas(val raiz: Int, val terca: Int, val quinta: Int) {

    fun obterInversao(inversao: PosicaoInversao): List<Int> {
        return when (inversao) {
            PosicaoInversao.FUNDAMENTAL -> listOf(raiz, terca, quinta)
            PosicaoInversao.PRIMEIRA_INVERSAO -> listOf(terca, quinta, raiz)
            PosicaoInversao.SEGUNDA_INVERSAO -> listOf(quinta, raiz, terca)
        }
    }

    /**
     * Retorna os índices das notas com deslocamento de oitava real no teclado de 2 oitavas (0..23).
     */
    fun obterNotasComOitavas(inversao: PosicaoInversao): List<Int> {
        return when (inversao) {
            PosicaoInversao.FUNDAMENTAL -> {
                listOf(raiz, terca, quinta)
            }
            PosicaoInversao.PRIMEIRA_INVERSAO -> {
                val n1 = terca
                val n2 = if (quinta < terca) quinta + 12 else quinta
                val n3 = raiz + 12
                listOf(n1, n2, n3)
            }
            PosicaoInversao.SEGUNDA_INVERSAO -> {
                val n1 = quinta
                val n2 = if (raiz < quinta) raiz + 12 else raiz
                val n3 = if (terca < quinta) terca + 12 else terca
                listOf(n1, n2, n3)
            }
        }
    }

    fun formatarNotas(inversao: PosicaoInversao): String {
        val nomes = arrayOf("Dó", "Dó#", "Ré", "Ré#", "Mi", "Fá", "Fá#", "Sol", "Sol#", "Lá", "Lá#", "Si")
        val notasList = when (inversao) {
            PosicaoInversao.FUNDAMENTAL -> listOf(raiz, terca, quinta)
            PosicaoInversao.PRIMEIRA_INVERSAO -> listOf(terca, quinta, raiz)
            PosicaoInversao.SEGUNDA_INVERSAO -> listOf(quinta, raiz, terca)
        }
        return notasList.joinToString(" + ") { nomes[it % 12] }
    }
}

object BaseDadosAcordes {
    val ACORDES_VIOLAO = mapOf(
        // C
        "C" to PosicaoViolao(cordas = listOf(-1, 3, 2, 0, 1, 0)),
        "C/E" to PosicaoViolao(cordas = listOf(0, 3, 2, 0, 1, 0)),
        "C/G" to PosicaoViolao(cordas = listOf(3, 3, 2, 0, 1, 0)),
        "C#" to PosicaoViolao(pestanaFret = 4, cordas = listOf(-1, 1, 3, 3, 3, 1)),
        "C#/F" to PosicaoViolao(pestanaFret = 1, cordas = listOf(1, 4, 3, 1, 2, 1)),
        "C#/G#" to PosicaoViolao(pestanaFret = 4, cordas = listOf(1, 1, 3, 3, 3, 1)),
        "Cm" to PosicaoViolao(pestanaFret = 3, cordas = listOf(-1, 1, 3, 3, 2, 1)),
        "Cm/Eb" to PosicaoViolao(pestanaFret = 3, cordas = listOf(-1, 3, 1, 1, 1, 1)),
        "Cm/G" to PosicaoViolao(pestanaFret = 3, cordas = listOf(1, 1, 3, 3, 2, 1)),

        // D
        "D" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 3, 2)),
        "D/F#" to PosicaoViolao(cordas = listOf(2, 0, 0, 2, 3, 2)),
        "D/A" to PosicaoViolao(cordas = listOf(-1, 0, 0, 2, 3, 2)),
        "D#" to PosicaoViolao(pestanaFret = 6, cordas = listOf(-1, 1, 3, 3, 3, 1)),
        "D#/G" to PosicaoViolao(pestanaFret = 3, cordas = listOf(1, 3, 3, 3, 1, 1)),
        "D#/A#" to PosicaoViolao(pestanaFret = 6, cordas = listOf(1, 1, 3, 3, 3, 1)),
        "Dm" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 3, 1)),
        "Dm/F" to PosicaoViolao(cordas = listOf(1, -1, 0, 2, 3, 1)),
        "Dm/A" to PosicaoViolao(cordas = listOf(-1, 0, 0, 2, 3, 1)),

        // E
        "E" to PosicaoViolao(cordas = listOf(0, 2, 2, 1, 0, 0)),
        "E/G#" to PosicaoViolao(pestanaFret = 4, cordas = listOf(1, 2, 2, 1, 0, 0)),
        "E/B" to PosicaoViolao(cordas = listOf(-1, 2, 2, 1, 0, 0)),
        "Em" to PosicaoViolao(cordas = listOf(0, 2, 2, 0, 0, 0)),
        "Em/G" to PosicaoViolao(cordas = listOf(3, 2, 2, 0, 0, 0)),
        "Em/B" to PosicaoViolao(cordas = listOf(-1, 2, 2, 0, 0, 0)),

        // F
        "F" to PosicaoViolao(pestanaFret = 1, cordas = listOf(1, 3, 3, 2, 1, 1)),
        "F/A" to PosicaoViolao(cordas = listOf(-1, 0, 3, 2, 1, 1)),
        "F/C" to PosicaoViolao(cordas = listOf(-1, 3, 3, 2, 1, 1)),
        "Fm" to PosicaoViolao(pestanaFret = 1, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "Fm/Ab" to PosicaoViolao(pestanaFret = 4, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "Fm/C" to PosicaoViolao(pestanaFret = 1, cordas = listOf(-1, 3, 3, 1, 1, 1)),

        // F# / Gb
        "F#" to PosicaoViolao(pestanaFret = 2, cordas = listOf(1, 3, 3, 2, 1, 1)),
        "F#/A#" to PosicaoViolao(pestanaFret = 1, cordas = listOf(-1, 1, 4, 3, 2, 2)),
        "F#/C#" to PosicaoViolao(pestanaFret = 4, cordas = listOf(-1, 1, 1, 3, 4, 2)),
        "F#m" to PosicaoViolao(pestanaFret = 2, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "F#m/A" to PosicaoViolao(cordas = listOf(-1, 0, 4, 2, 2, 2)),
        "F#m/C#" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 1, 2, 1)),

        // G
        "G" to PosicaoViolao(cordas = listOf(3, 2, 0, 0, 0, 3)),
        "G/B" to PosicaoViolao(cordas = listOf(-1, 2, 0, 0, 3, 3)),
        "G/D" to PosicaoViolao(cordas = listOf(-1, -1, 0, 0, 0, 3)),
        "Gm" to PosicaoViolao(pestanaFret = 3, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "Gm/Bb" to PosicaoViolao(pestanaFret = 1, cordas = listOf(-1, 1, 0, 0, 3, 3)),
        "Gm/D" to PosicaoViolao(cordas = listOf(-1, -1, 0, 3, 3, 3)),

        // A
        "A" to PosicaoViolao(cordas = listOf(-1, 0, 2, 2, 2, 0)),
        "A/C#" to PosicaoViolao(cordas = listOf(-1, 4, 2, 2, 2, 0)),
        "A/E" to PosicaoViolao(cordas = listOf(0, 0, 2, 2, 2, 0)),
        "Am" to PosicaoViolao(cordas = listOf(-1, 0, 2, 2, 1, 0)),
        "Am/C" to PosicaoViolao(cordas = listOf(-1, 3, 2, 2, 1, 0)),
        "Am/E" to PosicaoViolao(cordas = listOf(0, 0, 2, 2, 1, 0)),

        // B / Bb
        "B" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 3, 3, 1)),
        "B/D#" to PosicaoViolao(pestanaFret = 4, cordas = listOf(-1, 3, 1, 1, 1, 1)),
        "B/F#" to PosicaoViolao(pestanaFret = 2, cordas = listOf(1, 1, 3, 3, 3, 1)),
        "Bm" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 3, 2, 1)),
        "Bm/D" to PosicaoViolao(cordas = listOf(-1, -1, 0, 4, 3, 2)),
        "Bm/F#" to PosicaoViolao(pestanaFret = 2, cordas = listOf(1, 1, 3, 3, 2, 1)),
        "Bb" to PosicaoViolao(pestanaFret = 1, cordas = listOf(-1, 1, 3, 3, 3, 1)),
        "Bb/D" to PosicaoViolao(pestanaFret = 3, cordas = listOf(-1, 3, 1, 1, 1, 1)),
        "Bb/F" to PosicaoViolao(pestanaFret = 1, cordas = listOf(1, 1, 3, 3, 3, 1))
    )

    fun obterNotasTriade(acorde: String): TriadeNotas {
        val limpo = acorde.trim().takeWhile { it != '/' }
        val temAcidente = limpo.length >= 2 && (limpo[1] == '#' || limpo[1] == 'b')
        val notaStr = if (temAcidente) limpo.take(2) else limpo.take(1)
        val ehMenor = (temAcidente && limpo.length >= 3 && limpo[2] == 'm') || (!temAcidente && limpo.length >= 2 && limpo[1] == 'm')

        val raiz = when (notaStr.uppercase()) {
            "C" -> 0; "C#", "DB" -> 1; "D" -> 2; "D#", "EB" -> 3; "E" -> 4; "F" -> 5
            "F#", "GB" -> 6; "G" -> 7; "G#", "AB" -> 8; "A" -> 9; "A#", "BB" -> 10; "B" -> 11
            else -> 0
        }

        val terca = (raiz + if (ehMenor) 3 else 4) % 12
        val quinta = (raiz + 7) % 12

        return TriadeNotas(raiz, terca, quinta)
    }

    fun buscarPosicaoViolao(acorde: String, inversao: PosicaoInversao = PosicaoInversao.FUNDAMENTAL): PosicaoViolao? {
        val limpo = acorde.trim()
        val semBarra = limpo.takeWhile { it != '/' }

        val triade = obterNotasTriade(limpo)
        val nomesInversao = triade.obterInversao(inversao)
        val nomesNotasPt = arrayOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
        val notaBaixoNome = nomesNotasPt[nomesInversao[0] % 12]

        val chaveInversao = if (inversao == PosicaoInversao.FUNDAMENTAL) semBarra else "$semBarra/$notaBaixoNome"

        return ACORDES_VIOLAO[chaveInversao]
            ?: ACORDES_VIOLAO[semBarra]
            ?: ACORDES_VIOLAO[semBarra.take(if (semBarra.length >= 2 && (semBarra[1] == '#' || semBarra[1] == 'b')) 2 else 1)]
    }
}

@Composable
fun ChordDiagramDialog(
    nomeAcorde: String,
    onDismissRequest: () -> Unit
) {
    var instrumento by remember { mutableStateOf(InstrumentoDiagrama.VIOLAO) }
    var inversao by remember { mutableStateOf(PosicaoInversao.FUNDAMENTAL) }

    val acordeLimpo = remember(nomeAcorde) {
        nomeAcorde.trim().replace(Regex("[()\\[\\].,;:!?|]"), "")
    }

    val triade = remember(acordeLimpo) {
        BaseDadosAcordes.obterNotasTriade(acordeLimpo)
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(350.dp).padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Acorde $acordeLimpo",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Notas: ${triade.formatarNotas(inversao)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    TextButton(onClick = onDismissRequest) {
                        Text("Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seletor de Instrumento (Violão / Teclado)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { instrumento = InstrumentoDiagrama.VIOLAO },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = if (instrumento == InstrumentoDiagrama.VIOLAO)
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("🎸 Violão", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { instrumento = InstrumentoDiagrama.TECLADO },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = if (instrumento == InstrumentoDiagrama.TECLADO)
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("🎹 Teclado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seletor das 3 Posições (Fundamental, 1ª Inversão, 2ª Inversão)
                Text(
                    text = "Posição / Inversão:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PosicaoInversao.entries.forEach { pos ->
                        OutlinedButton(
                            onClick = { inversao = pos },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(32.dp),
                            colors = if (inversao == pos)
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(pos.titulo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (instrumento == InstrumentoDiagrama.VIOLAO) {
                    val posViolao = remember(acordeLimpo, inversao) {
                        BaseDadosAcordes.buscarPosicaoViolao(acordeLimpo, inversao)
                    }
                    if (posViolao != null) {
                        DesenharDiagramaViolao(posViolao)
                    } else {
                        Text(
                            text = "Posição do acorde $acordeLimpo disponível em breve.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    val posTeclado = remember(acordeLimpo, inversao) {
                        PosicaoTeclado(notas = triade.obterNotasComOitavas(inversao))
                    }
                    DesenharDiagramaTeclado(posTeclado)
                }
            }
        }
    }
}

@Composable
fun DesenharDiagramaViolao(posicao: PosicaoViolao) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (posicao.pestanaFret > 0) {
            Text("Pestana na ${posicao.pestanaFret}ª casa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Spacer(modifier = Modifier.height(6.dp))
        }

        Canvas(modifier = Modifier.size(width = 200.dp, height = 200.dp)) {
            val numCordas = 6
            val numTrastes = 5
            val startX = 25.dp.toPx()
            val startY = 35.dp.toPx()
            val gridWidth = size.width - 50.dp.toPx()
            val gridHeight = size.height - 50.dp.toPx()
            val stringStep = gridWidth / (numCordas - 1)
            val fretStep = gridHeight / numTrastes

            // Nut / Pestana superior
            drawRect(
                color = onSurfaceColor,
                topLeft = Offset(startX, startY - 4.dp.toPx()),
                size = Size(gridWidth, 4.dp.toPx())
            )

            // Linhas dos trastes (horizontais)
            for (f in 0..numTrastes) {
                val y = startY + f * fretStep
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.5f),
                    start = Offset(startX, y),
                    end = Offset(startX + gridWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Linhas das cordas (verticais)
            for (c in 0 until numCordas) {
                val x = startX + c * stringStep
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.7f),
                    start = Offset(x, startY),
                    end = Offset(x, startY + gridHeight),
                    strokeWidth = (1.5f + c * 0.3f).dp.toPx()
                )

                val trasteVal = posicao.cordas.getOrNull(c) ?: 0
                if (trasteVal == 0) {
                    drawCircle(
                        color = primaryColor,
                        radius = 4.5.dp.toPx(),
                        center = Offset(x, startY - 12.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                } else if (trasteVal == -1) {
                    val r = 4.dp.toPx()
                    val cx = x
                    val cy = startY - 12.dp.toPx()
                    drawLine(color = Color.Red, start = Offset(cx - r, cy - r), end = Offset(cx + r, cy + r), strokeWidth = 2.dp.toPx())
                    drawLine(color = Color.Red, start = Offset(cx + r, cy - r), end = Offset(cx - r, cy + r), strokeWidth = 2.dp.toPx())
                } else if (trasteVal > 0) {
                    val cy = startY + (trasteVal - 0.5f) * fretStep
                    drawCircle(
                        color = primaryColor,
                        radius = 8.dp.toPx(),
                        center = Offset(x, cy)
                    )
                }
            }
        }
    }
}

@Composable
fun DesenharDiagramaTeclado(posicao: PosicaoTeclado) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Mapeamento das 14 teclas brancas cobrindo 2 oitavas (C1 D1 E1 F1 G1 A1 B1 | C2 D2 E2 F2 G2 A2 B2)
    val whiteKeys = listOf(
        0 to "C1", 2 to "D1", 4 to "E1", 5 to "F1", 7 to "G1", 9 to "A1", 11 to "B1",
        12 to "C2", 14 to "D2", 16 to "E2", 17 to "F2", 19 to "G2", 21 to "A2", 23 to "B2"
    )

    // Mapeamento das 10 teclas pretas cobrindo 2 oitavas: nota -> boundaryIndex da linha entre teclas brancas
    val blackKeys = listOf(
        // Oitava 1
        1 to 1,   // C#1 / Db1
        3 to 2,   // D#1 / Eb1
        6 to 4,   // F#1 / Gb1
        8 to 5,   // G#1 / Ab1
        10 to 6,  // A#1 / Bb1
        // Oitava 2
        13 to 8,  // C#2 / Db2
        15 to 9,  // D#2 / Eb2
        18 to 11, // F#2 / Gb2
        20 to 12, // G#2 / Ab2
        22 to 13  // A#2 / Bb2
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 280.dp, height = 110.dp)) {
            val numWhite = 14
            val wWidth = size.width / numWhite
            val wHeight = size.height
            val bWidth = wWidth * 0.60f
            val bHeight = wHeight * 0.62f

            // 1. Desenhar Teclas Brancas (2 Oitavas)
            whiteKeys.forEachIndexed { i, (notaIndex, _) ->
                val left = i * wWidth
                val estaPressionada = posicao.notas.contains(notaIndex)

                // Fundo da tecla branca
                drawRect(
                    color = if (estaPressionada) primaryColor else Color.White,
                    topLeft = Offset(left + 0.5f.dp.toPx(), 0f),
                    size = Size(wWidth - 1.dp.toPx(), wHeight)
                )

                // Borda da tecla branca
                drawRect(
                    color = onSurfaceColor.copy(alpha = 0.4f),
                    topLeft = Offset(left + 0.5f.dp.toPx(), 0f),
                    size = Size(wWidth - 1.dp.toPx(), wHeight),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Ponto indicador se pressionada
                if (estaPressionada) {
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = Offset(left + wWidth / 2f, wHeight - 12.dp.toPx())
                    )
                }
            }

            // 2. Desenhar Teclas Pretas (2 Oitavas)
            blackKeys.forEach { (notaIndex, boundaryIndex) ->
                val estaPressionada = posicao.notas.contains(notaIndex)
                val left = boundaryIndex * wWidth - bWidth / 2f

                // Fundo da tecla preta
                drawRect(
                    color = if (estaPressionada) primaryColor else Color(0xFF1E1E1E),
                    topLeft = Offset(left, 0f),
                    size = Size(bWidth, bHeight)
                )

                // Borda da tecla preta
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(left, 0f),
                    size = Size(bWidth, bHeight),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Ponto indicador se pressionada
                if (estaPressionada) {
                    drawCircle(
                        color = Color.White,
                        radius = 3.0.dp.toPx(),
                        center = Offset(left + bWidth / 2f, bHeight - 8.dp.toPx())
                    )
                }
            }
        }
    }
}
