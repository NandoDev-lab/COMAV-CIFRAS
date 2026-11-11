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

data class PosicaoViolao(
    val pestanaFret: Int = 0,
    // Trastes para as 6 cordas da 6ª (E grave) para a 1ª (E agudo). -1 = abafada (X), 0 = solta (O), 1..5 = traste
    val cordas: List<Int>
)

data class PosicaoTeclado(
    // Notas no teclado (0 = C, 1 = C#, 2 = D, 3 = D#, 4 = E, 5 = F, 6 = F#, 7 = G, 8 = G#, 9 = A, 10 = A#, 11 = B)
    val notas: List<Int>
)

object BaseDadosAcordes {
    val ACORDES_VIOLAO = mapOf(
        "C" to PosicaoViolao(cordas = listOf(-1, 3, 2, 0, 1, 0)),
        "Cm" to PosicaoViolao(pestanaFret = 3, cordas = listOf(-1, 1, 3, 3, 2, 1)),
        "C7" to PosicaoViolao(cordas = listOf(-1, 3, 2, 3, 1, 0)),
        "Cadd9" to PosicaoViolao(cordas = listOf(-1, 3, 2, 0, 3, 3)),
        "Cmaj7" to PosicaoViolao(cordas = listOf(-1, 3, 2, 0, 0, 0)),
        "D" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 3, 2)),
        "Dm" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 3, 1)),
        "D7" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 1, 2)),
        "Dsus4" to PosicaoViolao(cordas = listOf(-1, -1, 0, 2, 3, 3)),
        "D/F#" to PosicaoViolao(cordas = listOf(2, 0, 0, 2, 3, 2)),
        "E" to PosicaoViolao(cordas = listOf(0, 2, 2, 1, 0, 0)),
        "Em" to PosicaoViolao(cordas = listOf(0, 2, 2, 0, 0, 0)),
        "E7" to PosicaoViolao(cordas = listOf(0, 2, 0, 1, 0, 0)),
        "F" to PosicaoViolao(pestanaFret = 1, cordas = listOf(1, 3, 3, 2, 1, 1)),
        "Fm" to PosicaoViolao(pestanaFret = 1, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "F#m" to PosicaoViolao(pestanaFret = 2, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "F#m7" to PosicaoViolao(pestanaFret = 2, cordas = listOf(1, 3, 1, 1, 1, 1)),
        "G" to PosicaoViolao(cordas = listOf(3, 2, 0, 0, 0, 3)),
        "G/B" to PosicaoViolao(cordas = listOf(-1, 2, 0, 0, 3, 3)),
        "Gm" to PosicaoViolao(pestanaFret = 3, cordas = listOf(1, 3, 3, 1, 1, 1)),
        "G7" to PosicaoViolao(cordas = listOf(3, 2, 0, 0, 0, 1)),
        "Gadd9" to PosicaoViolao(cordas = listOf(3, 2, 0, 2, 0, 3)),
        "A" to PosicaoViolao(cordas = listOf(-1, 0, 2, 2, 2, 0)),
        "Am" to PosicaoViolao(cordas = listOf(-1, 0, 2, 2, 1, 0)),
        "A7" to PosicaoViolao(cordas = listOf(-1, 0, 2, 0, 2, 0)),
        "Am7" to PosicaoViolao(cordas = listOf(-1, 0, 2, 0, 1, 0)),
        "A/C#" to PosicaoViolao(cordas = listOf(-1, 4, 2, 2, 2, 0)),
        "B" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 3, 3, 1)),
        "Bm" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 3, 2, 1)),
        "Bm7" to PosicaoViolao(pestanaFret = 2, cordas = listOf(-1, 1, 3, 1, 2, 1)),
        "Bb" to PosicaoViolao(pestanaFret = 1, cordas = listOf(-1, 1, 3, 3, 3, 1)),
        "Bbm" to PosicaoViolao(pestanaFret = 1, cordas = listOf(-1, 1, 3, 3, 2, 1)),
        "Eb" to PosicaoViolao(pestanaFret = 6, cordas = listOf(-1, 1, 3, 3, 3, 1)),
        "Ab" to PosicaoViolao(pestanaFret = 4, cordas = listOf(1, 3, 3, 2, 1, 1))
    )

    val ACORDES_TECLADO = mapOf(
        "C" to PosicaoTeclado(notas = listOf(0, 4, 7)),
        "Cm" to PosicaoTeclado(notas = listOf(0, 3, 7)),
        "C7" to PosicaoTeclado(notas = listOf(0, 4, 7, 10)),
        "Cadd9" to PosicaoTeclado(notas = listOf(0, 2, 4, 7)),
        "Cmaj7" to PosicaoTeclado(notas = listOf(0, 4, 7, 11)),
        "D" to PosicaoTeclado(notas = listOf(2, 6, 9)),
        "Dm" to PosicaoTeclado(notas = listOf(2, 5, 9)),
        "D7" to PosicaoTeclado(notas = listOf(2, 6, 9, 0)),
        "D/F#" to PosicaoTeclado(notas = listOf(6, 9, 2)),
        "E" to PosicaoTeclado(notas = listOf(4, 8, 11)),
        "Em" to PosicaoTeclado(notas = listOf(4, 7, 11)),
        "F" to PosicaoTeclado(notas = listOf(5, 9, 0)),
        "Fm" to PosicaoTeclado(notas = listOf(5, 8, 0)),
        "F#m" to PosicaoTeclado(notas = listOf(6, 9, 1)),
        "F#m7" to PosicaoTeclado(notas = listOf(6, 9, 1, 4)),
        "G" to PosicaoTeclado(notas = listOf(7, 11, 2)),
        "G/B" to PosicaoTeclado(notas = listOf(11, 2, 7)),
        "Gm" to PosicaoTeclado(notas = listOf(7, 10, 2)),
        "A" to PosicaoTeclado(notas = listOf(9, 1, 4)),
        "Am" to PosicaoTeclado(notas = listOf(9, 0, 4)),
        "Am7" to PosicaoTeclado(notas = listOf(9, 0, 4, 7)),
        "A/C#" to PosicaoTeclado(notas = listOf(1, 4, 9)),
        "B" to PosicaoTeclado(notas = listOf(11, 3, 6)),
        "Bm" to PosicaoTeclado(notas = listOf(11, 2, 6)),
        "Bm7" to PosicaoTeclado(notas = listOf(11, 2, 6, 9)),
        "Bb" to PosicaoTeclado(notas = listOf(10, 2, 5)),
        "Bbm" to PosicaoTeclado(notas = listOf(10, 1, 5)),
        "Eb" to PosicaoTeclado(notas = listOf(3, 7, 10)),
        "Ab" to PosicaoTeclado(notas = listOf(8, 0, 3))
    )

    fun buscarPosicaoViolao(acorde: String): PosicaoViolao? {
        val limpo = acorde.trim()
        return ACORDES_VIOLAO[limpo]
            ?: ACORDES_VIOLAO[limpo.takeWhile { it != '/' }]
            ?: ACORDES_VIOLAO[limpo.replace(Regex("\\d|add|maj|sus"), "")]
            ?: ACORDES_VIOLAO[limpo.take(1)]
    }

    fun buscarPosicaoTeclado(acorde: String): PosicaoTeclado? {
        val limpo = acorde.trim()
        return ACORDES_TECLADO[limpo]
            ?: ACORDES_TECLADO[limpo.takeWhile { it != '/' }]
            ?: ACORDES_TECLADO[limpo.replace(Regex("\\d|add|maj|sus"), "")]
            ?: ACORDES_TECLADO[limpo.take(1)]
    }
}

@Composable
fun ChordDiagramDialog(
    nomeAcorde: String,
    onDismissRequest: () -> Unit
) {
    var instrumento by remember { mutableStateOf(InstrumentoDiagrama.VIOLAO) }
    val acordeLimpo = remember(nomeAcorde) {
        nomeAcorde.trim().replace(Regex("[()\\[\\].,;:!?|]"), "")
    }

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
                    Column {
                        Text(
                            text = "Acorde $acordeLimpo",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Desenho no ${if (instrumento == InstrumentoDiagrama.VIOLAO) "Violão / Guitarra" else "Teclado / Piano"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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

                Spacer(modifier = Modifier.height(16.dp))

                if (instrumento == InstrumentoDiagrama.VIOLAO) {
                    val posViolao = remember(acordeLimpo) { BaseDadosAcordes.buscarPosicaoViolao(acordeLimpo) }
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
                    val posTeclado = remember(acordeLimpo) { BaseDadosAcordes.buscarPosicaoTeclado(acordeLimpo) }
                    if (posTeclado != null) {
                        DesenharDiagramaTeclado(posTeclado)
                    } else {
                        Text(
                            text = "Posição do acorde $acordeLimpo disponível em breve.",
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

    // Oitava completa (12 notas: C C# D D# E F F# G G# A A# B)
    val teclasBrancas = listOf(0, 2, 4, 5, 7, 9, 11) // C D E F G A B
    // Teclas pretas: nota -> posição entre teclas brancas
    val teclasPretas = listOf(
        1 to 0, // C# entre C(0) e D(1)
        3 to 1, // D# entre D(1) e E(2)
        6 to 3, // F# entre F(3) e G(4)
        8 to 4, // G# entre G(4) e A(5)
        10 to 5 // A# entre A(5) e B(6)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Renderizar Teclas Brancas
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            teclasBrancas.forEach { notaIndex ->
                val estaPressionada = posicao.notas.contains(notaIndex)
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(110.dp)
                        .background(
                            color = if (estaPressionada) primaryColor else Color.White,
                            shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                        )
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (estaPressionada) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.White, CircleShape)
                        )
                    }
                }
            }
        }

        // Renderizar Teclas Pretas sobrepostas
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            val teclaWidth = 32.dp
            val gap = 2.dp

            // Para posicionamento relativo preciso das 5 teclas pretas
            val posicoesPretas = listOf(1, 2, 4, 5, 6) // Posição após a N-ésima tecla branca

            posicoesPretas.forEachIndexed { idx, posBranca ->
                val (notaPretaIndex, _) = teclasPretas[idx]
                val estaPressionada = posicao.notas.contains(notaPretaIndex)

                Box(
                    modifier = Modifier
                        .padding(start = (posBranca * 34 - 10).dp)
                        .width(20.dp)
                        .height(65.dp)
                        .background(
                            color = if (estaPressionada) primaryColor else Color.Black,
                            shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (estaPressionada) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .size(8.dp)
                                .background(Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
