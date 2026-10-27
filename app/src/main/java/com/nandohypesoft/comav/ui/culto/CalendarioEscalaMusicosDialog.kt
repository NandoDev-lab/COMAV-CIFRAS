package com.nandohypesoft.comav.ui.culto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandohypesoft.comav.model.EscalaMusicoData
import com.nandohypesoft.comav.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioEscalaMusicosDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val escalasMusicosMap by viewModel.escalasMusicosDatas.collectAsState()

    var cal by remember { mutableStateOf(Calendar.getInstance()) }

    val nomeMes = remember(cal.timeInMillis) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        sdf.format(cal.time).replaceFirstChar { it.uppercase() }
    }

    val diasNoMes = remember(cal.timeInMillis) { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }

    val primeiroDiaSemana = remember(cal.timeInMillis) {
        val tempCal = cal.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Dom, 2 = Seg, ..., 5 = Qui, 7 = Sáb
    }

    var diaSelecionado by remember { mutableStateOf<Int?>(null) }

    val ano = remember(cal.timeInMillis) { cal.get(Calendar.YEAR) }
    val mesIndex = remember(cal.timeInMillis) { cal.get(Calendar.MONTH) } // 0-based

    val dataIsoSelecionada = remember(diaSelecionado, ano, mesIndex) {
        if (diaSelecionado != null) {
            String.format(Locale.US, "%04d-%02d-%02d", ano, mesIndex + 1, diaSelecionado)
        } else ""
    }

    val escalaDoDia = remember(dataIsoSelecionada, escalasMusicosMap) {
        escalasMusicosMap[dataIsoSelecionada] ?: EscalaMusicoData(dataIso = dataIsoSelecionada)
    }

    var tecladistaEdicao by remember(escalaDoDia) { mutableStateOf(escalaDoDia.tecladista) }
    var violonistaEdicao by remember(escalaDoDia) { mutableStateOf(escalaDoDia.violonista) }
    var bateristaEdicao by remember(escalaDoDia) { mutableStateOf(escalaDoDia.baterista) }

    val opcoesTeclado = listOf("Selecione...", "Fernando", "Agnaldo", "Outro...")
    val opcoesViolao = listOf("Selecione...", "Fernando", "Victor", "Daniel", "Outro...")
    val opcoesBateria = listOf("Selecione...", "Victor", "Felipe", "Outro...")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📅 Calendário de Músicos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cadastre previamente a equipe para Quintas e Domingos:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Cabeçalho Navegação de Mês
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            val newCal = cal.clone() as Calendar
                            newCal.add(Calendar.MONTH, -1)
                            cal = newCal
                            diaSelecionado = null
                        }) {
                            Text("←", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Text(
                            text = nomeMes,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        TextButton(onClick = {
                            val newCal = cal.clone() as Calendar
                            newCal.add(Calendar.MONTH, 1)
                            cal = newCal
                            diaSelecionado = null
                        }) {
                            Text("→", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                // Dias da semana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEachIndexed { idx, d ->
                        val ehDiaCulto = idx == 0 || idx == 4 // Dom ou Qui
                        Text(
                            text = d,
                            fontSize = 11.sp,
                            fontWeight = if (ehDiaCulto) FontWeight.Bold else FontWeight.Normal,
                            color = if (ehDiaCulto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Grid do Calendário (Linhas de 7 dias)
                val totalSlots = (primeiroDiaSemana - 1) + diasNoMes
                val totalLinhas = (totalSlots + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (linha in 0 until totalLinhas) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0 until 7) {
                                val indexSlot = linha * 7 + col
                                val diaNumero = indexSlot - (primeiroDiaSemana - 1) + 1

                                if (diaNumero in 1..diasNoMes) {
                                    val ehQuintaOuDomingo = (col == 0 || col == 4)
                                    val dataIsoLoop = String.format(Locale.US, "%04d-%02d-%02d", ano, mesIndex + 1, diaNumero)
                                    val temMusicoCadastrado = escalasMusicosMap.containsKey(dataIsoLoop) &&
                                            (escalasMusicosMap[dataIsoLoop]?.tecladista?.isNotBlank() == true ||
                                             escalasMusicosMap[dataIsoLoop]?.violonista?.isNotBlank() == true ||
                                             escalasMusicosMap[dataIsoLoop]?.baterista?.isNotBlank() == true)

                                    val ehSelecionado = diaSelecionado == diaNumero

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    ehSelecionado -> MaterialTheme.colorScheme.primary
                                                    temMusicoCadastrado -> MaterialTheme.colorScheme.tertiaryContainer
                                                    ehQuintaOuDomingo -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            )
                                            .clickable {
                                                diaSelecionado = diaNumero
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = diaNumero.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = if (ehQuintaOuDomingo) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    ehSelecionado -> MaterialTheme.colorScheme.onPrimary
                                                    temMusicoCadastrado -> MaterialTheme.colorScheme.onTertiaryContainer
                                                    ehQuintaOuDomingo -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            if (temMusicoCadastrado) {
                                                Text("🎵", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }

                // Legenda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
                        Text("Culto (Qui/Dom)", fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer))
                        Text("Músicos Escala", fontSize = 10.sp)
                    }
                }

                // Formulário de edição para o dia selecionado
                if (diaSelecionado != null) {
                    val tempCal = cal.clone() as Calendar
                    tempCal.set(Calendar.DAY_OF_MONTH, diaSelecionado!!)
                    val diaSemanaNome = when (tempCal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "Domingo"
                        Calendar.THURSDAY -> "Quinta-feira"
                        else -> "Dia comum"
                    }

                    val mesFormatado = String.format(Locale.US, "%02d", mesIndex + 1)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Escala para $diaSelecionado/$mesFormatado ($diaSemanaNome):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SeletorMusicoSpinner(
                        label = "Teclado",
                        icone = "🎹",
                        musicoSelecionado = tecladistaEdicao,
                        opcoesMusicos = opcoesTeclado,
                        onMusicoSelecionado = { tecladistaEdicao = it }
                    )

                    SeletorMusicoSpinner(
                        label = "Violão",
                        icone = "🎸",
                        musicoSelecionado = violonistaEdicao,
                        opcoesMusicos = opcoesViolao,
                        onMusicoSelecionado = { violonistaEdicao = it }
                    )

                    SeletorMusicoSpinner(
                        label = "Bateria",
                        icone = "🥁",
                        musicoSelecionado = bateristaEdicao,
                        opcoesMusicos = opcoesBateria,
                        onMusicoSelecionado = { bateristaEdicao = it }
                    )

                    Button(
                        onClick = {
                            val novaEscala = EscalaMusicoData(
                                dataIso = dataIsoSelecionada,
                                tecladista = tecladistaEdicao.trim(),
                                violonista = violonistaEdicao.trim(),
                                baterista = bateristaEdicao.trim()
                            )
                            viewModel.salvarEscalaMusicoData(novaEscala)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Salvar Escala do Dia $diaSelecionado", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
