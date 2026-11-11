package com.nandohypesoft.comav.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class PosicaoViolao(
    val pestanaFret: Int = 0,
    // Trastes para as 6 cordas da 6ª (E grave) para a 1ª (E agudo). -1 = abafada (X), 0 = solta (O), 1..5 = traste
    val cordas: List<Int>,
    val dedos: List<Int> = emptyList()
)

data class PosicaoTeclado(
    // Notas no teclado (0 = C, 1 = C#, 2 = D ... 11 = B)
    val notas: List<Int>
)

object BaseDadosAcordes {
    // Mapeamento simplificado dos acordes mais tocados em igrejas
    val ACORDES_VIOLAO = mapOf(
        "C" to PosicaoViolao(cordas = listOf(-1, 3, 2, 0, 1, 0), dedos = listOf(0, 3, 2, 0, 1, 0)),
        "Cm" to PosicaoViolao(pestanaFret = 3, cordas = listOf(-1, 1, 3, 3, 2, 1)),
        "C7" to PosicaoViolao(cordas = listOf(-1, 3, 2, 3, 1, 0)),
        "Cadd9" to PosicaoViolao(cordas = listOf(-1, 3, 2, 0, 3, 3)),
        "D" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 3, 2)),
        "Dm" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 3, 1)),
        "D7" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 1, 2)),
        "Dsus4" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 3, 3)),
        "E" to PosicaoViolao(cordas = listOf(0, 2, 2, 1, 0, 0)),
        "Em" to PosicaoViolao(cordas = listOf(0, 2, 2, 0, 0, 0)),
        "E7" to PosicaoViolao(cordas = listOf(0, 2, 0, 1, 0, 0)),
        "F" to PosicaoViolao(pestanaFret = 1, cordas = listOf(1, 3, 3, 2, 1, 1)),
        "Fm" to PosicaoViolao(pestanaFret = 1, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "F#m" to PosicaoViolao(pestanaFret = 2, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "G" to PosicaoViolao(cordas = listOf(3, 2, 0, 0, 0, 3)),
        "G/B" to PosicaoViolao(cordas = listOf(-1, 2, 0, 0, 3, 3)),
        "Gm" to PosicaoViolao(pestanaFret = 3, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "A" to PosicaoViolao(cordas = listOf(-1, 0, 2, 2, 2, 0)),
        "Am" to PosicaoViolao(cordas = listOf(-1, 0, 2, 2, 1, 0)),
        "A7" to PosicaoViolao(cordas = listOf(-1, 0, 2, 0, 2, 0)),
        "Am7" to PosicaoViolao(cordas = listOf(-1, 0, 2, 0, 1, 0)),
        "A/C#" to PosicaoViolao(cordas = listOf(-1, 4, 2, 2, 2, 0)),
        "B" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 3, 3, 1)),
        "Bm" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 3, 2, 1)),
        "Bm7" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 1, 2, 1))
    )

    val ACORDES_TECLADO = mapOf(
        "C" to PosicaoTeclado(notas = listOf(0, 4, 7)),     // C E G
        "Cm" to PosicaoTeclado(notas = listOf(0, 3, 7)),    // C Eb G
        "C7" to PosicaoTeclado(notas = listOf(0, 4, 7, 10)),// C E G Bb
        "Cadd9" to PosicaoTeclado(notas = listOf(0, 2, 4, 7)),// C D E G
        "D" to PosicaoTeclado(notas = listOf(2, 6, 9)),     // D F# A
        "Dm" to PosicaoTeclado(notas = listOf(2, 5, 9)),    // D F A
        "D7" to PosicaoTeclado(notas = listOf(2, 6, 9, 0)), // D F# A C
        "E" to PosicaoTeclado(notas = listOf(4, 8, 11)),    // E G# B
        "Em" to PosicaoTeclado(notas = listOf(4, 7, 11)),   // E G B
        "F" to PosicaoTeclado(notas = listOf(5, 9, 0)),     // F A C
        "Fm" to PosicaoTeclado(notas = listOf(5, 8, 0)),    // F Ab C
        "F#m" to PosicaoTeclado(notas = listOf(6, 9, 1)),   // F# A C#
        "G" to PosicaoTeclado(notas = listOf(7, 11, 2)),    // G B D
        "G/B" to PosicaoTeclado(notas = listOf(11, 2, 7)),  // B D G
        "Gm" to PosicaoTeclado(notas = listOf(7, 10, 2)),   // G Bb D
        "A" to PosicaoTeclado(notas = listOf(9, 1, 4)),     // A C# E
        "Am" to PosicaoTeclado(notas = listOf(9, 0, 4)),    // A C E
        "Am7" to PosicaoTeclado(notas = listOf(9, 0, 4, 7)),// A C E G
        "B" to PosicaoTeclado(notas = listOf(11, 3, 6)),    // B D# F#
        "Bm" to PosicaoTeclado(notas = listOf(11, 2, 6)),   // B D F#
        "Bm7" to PosicaoTeclado(notas = listOf(11, 2, 6, 9))// B D F# A
    )
}

@Composable
fun ChordDiagramDialog(
    nomeAcorde: String,
    onDismissRequest: () -> Unit
) {
    var instrumento by remember { mutableStateOf(InstrumentoDiagrama.VIOLAO) }
    val acordeLimpo = remember(nomeAcorde) { nomeAcorde.trim().replace(Regex("[()\\[\\].,;:!?|]"), "") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(320.dp).padding(8.dp)
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
                    Text(
                        text = "Acorde $acordeLimpo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(onClick = onDismissRequest) {
                        Text("Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                            androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("🎸 Violão", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { instrumento = InstrumentoDiagrama.TECLADO },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = if (instrumento == InstrumentoDiagrama.TECLADO)
                            androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("🎹 Teclado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (instrumento == InstrumentoDiagrama.VIOLAO) {
                    val posViolao = BaseDadosAcordes.ACORDES_VIOLAO[acordeLimpo]
                        ?: BaseDadosAcordes.ACORDES_VIOLAO[acordeLimpo.takeWhile { it != '/' }]

                    if (posViolao != null) {
                        DesenharDiagramaViolao(posViolao)
                    } else {
                        Text(
                            text = "Posição do acorde $acordeLimpo disponível em breve na biblioteca.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    val posTeclado = BaseDadosAcordes.ACORDES_TECLADO[acordeLimpo]
                        ?: BaseDadosAcordes.ACORDES_TECLADO[acordeLimpo.takeWhile { it != '/' }]

                    if (posTeclado != null) {
                        DesenharDiagramaTeclado(posTeclado)
                    } else {
                        Text(
                            text = "Posição do acorde $acordeLimpo disponível em breve na biblioteca.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
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
            Text("Pestana na ${posicao.pestanaFret}ª casa", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
            Spacer(modifier = Modifier.height(6.dp))
        }

        Canvas(modifier = Modifier.size(width = 180.dp, height = 180.dp)) {
            val numCordas = 6
            val numTrastes = 5
            val startX = 20.dp.toPx()
            val startY = 30.dp.toPx()
            val gridWidth = size.width - 40.dp.toPx()
            val gridHeight = size.height - 40.dp.toPx()
            val stringStep = gridWidth / (numCordas - 1)
            val fretStep = gridHeight / numTrastes

            // Desenhar Nut / Pestana superior
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

                // Indicadores de cordas solta (O) ou abafada (X) no topo
                val trasteVal = posicao.cordas.getOrNull(c) ?: 0
                if (trasteVal == 0) {
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, startY - 12.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                } else if (trasteVal == -1) {
                    // Desenhar 'X'
                    val r = 4.dp.toPx()
                    val cx = x
                    val cy = startY - 12.dp.toPx()
                    drawLine(color = Color.Red, start = Offset(cx - r, cy - r), end = Offset(cx + r, cy + r), strokeWidth = 2.dp.toPx())
                    drawLine(color = Color.Red, start = Offset(cx + r, cy - r), end = Offset(cx - r, cy + r), strokeWidth = 2.dp.toPx())
                } else if (trasteVal > 0) {
                    // Desenhar bolinha do dedo na casa correspondente
                    val cy = startY + (trasteVal - 0.5f) * fretStep
                    drawCircle(
                        color = primaryColor,
                        radius = 7.dp.toPx(),
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

    // Oitava completa (12 teclas: C C# D D# E F F# G G# A A# B)
    val teclasBrancas = listOf(0, 2, 4, 5, 7, 9, 11)
    val teclasPretas = mapOf(1 to 0, 3 to 1, 6 to 3, 8 to 4, 10 to 5) // Mapeia nota para o índice da tecla branca à esquerda

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            teclasBrancas.forEach { notaIndex ->
                val estaPressionada = posicao.notas.contains(notaIndex)
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(100.dp)
                        .padding(horizontal = 1.dp)
                        .background(
                            color = if (estaPressionada) primaryColor.copy(alpha = 0.8f) else Color.White,
                            shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (estaPressionada) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .size(10.dp)
                                .background(Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
