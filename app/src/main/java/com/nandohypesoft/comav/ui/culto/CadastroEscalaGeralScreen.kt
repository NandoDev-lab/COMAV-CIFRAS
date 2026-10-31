package com.nandohypesoft.comav.ui.culto

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandohypesoft.comav.model.EscalaCulto
import com.nandohypesoft.comav.ui.MainViewModel
import com.nandohypesoft.comav.ui.TelaDestino
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroEscalaGeralScreen(viewModel: MainViewModel, onVoltar: () -> Unit) {
    val context = LocalContext.current

    val displayDateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val storageDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var dataCultoBrInput by remember { mutableStateOf("") }
    var ministroInput by remember { mutableStateOf("") }
    var vocal1Input by remember { mutableStateOf("") }
    var vocal2Input by remember { mutableStateOf("") }
    var vocal3Input by remember { mutableStateOf("") }
    var tecladistaInput by remember { mutableStateOf("") }
    var violonistaInput by remember { mutableStateOf("") }
    var bateristaInput by remember { mutableStateOf("") }
    var baixistaInput by remember { mutableStateOf("") }

    val opcoesMinistros = com.nandohypesoft.comav.util.EquipeConfig.OPCOES_MINISTROS_SPINNER
    val opcoesVocais = com.nandohypesoft.comav.util.EquipeConfig.OPCOES_VOCAIS_SPINNER
    val opcoesTeclado = com.nandohypesoft.comav.util.EquipeConfig.OPCOES_TECLADO_SPINNER
    val opcoesViolao = com.nandohypesoft.comav.util.EquipeConfig.OPCOES_VIOLAO_SPINNER
    val opcoesBateria = com.nandohypesoft.comav.util.EquipeConfig.OPCOES_BATERIA_SPINNER

    val diaSemanaCalculado = remember(dataCultoBrInput) {
        try {
            val date = displayDateFormat.parse(dataCultoBrInput.trim())
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> "Domingo"
                    Calendar.MONDAY -> "Segunda-feira"
                    Calendar.TUESDAY -> "Terça-feira"
                    Calendar.WEDNESDAY -> "Quarta-feira"
                    Calendar.THURSDAY -> "Quinta-feira"
                    Calendar.FRIDAY -> "Sexta-feira"
                    Calendar.SATURDAY -> "Sábado"
                    else -> ""
                }
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Escala Geral da Equipe") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "📅 Cadastre a data do culto e Escale a equipe completa (Ministro, Vocais e Músicos). O ministro escalado preencherá o repertório no domingo anterior:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = dataCultoBrInput,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(8)
                    val formatted = when {
                        digits.length >= 5 -> "${digits.substring(0, 2)}/${digits.substring(2, 4)}/${digits.substring(4)}"
                        digits.length >= 3 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
                        else -> digits
                    }
                    dataCultoBrInput = formatted
                },
                label = { Text("Data do Culto (DD/MM/AAAA)") },
                placeholder = { Text("Ex: 01/11/2026") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (diaSemanaCalculado.isNotBlank()) {
                Text(
                    text = "Dia do Culto: $diaSemanaCalculado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            Text(
                text = "🎤 Ministro / Dirigente Escalado:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            SeletorMusicoSpinner(
                label = "Ministro / Dirigente Escalado",
                icone = "🎼",
                musicoSelecionado = ministroInput,
                opcoesMusicos = opcoesMinistros,
                onMusicoSelecionado = { ministroInput = it }
            )

            HorizontalDivider()

            Text(
                text = "🗣️ Backing Vocais (Até 3 por culto):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            SeletorMusicoSpinner(
                label = "Vocal 1",
                icone = "🎤",
                musicoSelecionado = vocal1Input,
                opcoesMusicos = opcoesVocais,
                onMusicoSelecionado = { vocal1Input = it }
            )

            SeletorMusicoSpinner(
                label = "Vocal 2",
                icone = "🎤",
                musicoSelecionado = vocal2Input,
                opcoesMusicos = opcoesVocais,
                onMusicoSelecionado = { vocal2Input = it }
            )

            SeletorMusicoSpinner(
                label = "Vocal 3",
                icone = "🎤",
                musicoSelecionado = vocal3Input,
                opcoesMusicos = opcoesVocais,
                onMusicoSelecionado = { vocal3Input = it }
            )

            HorizontalDivider()

            Text(
                text = "🎸 Equipe de Músicos:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            SeletorMusicoSpinner(
                label = "Teclado",
                icone = "🎹",
                musicoSelecionado = tecladistaInput,
                opcoesMusicos = opcoesTeclado,
                onMusicoSelecionado = { tecladistaInput = it }
            )

            SeletorMusicoSpinner(
                label = "Violão",
                icone = "🎸",
                musicoSelecionado = violonistaInput,
                opcoesMusicos = opcoesViolao,
                onMusicoSelecionado = { violonistaInput = it }
            )

            SeletorMusicoSpinner(
                label = "Bateria",
                icone = "🥁",
                musicoSelecionado = bateristaInput,
                opcoesMusicos = opcoesBateria,
                onMusicoSelecionado = { bateristaInput = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (dataCultoBrInput.isBlank() || diaSemanaCalculado.isBlank()) {
                        Toast.makeText(context, "Por favor, informe uma data válida (DD/MM/AAAA).", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (ministroInput.isBlank()) {
                        Toast.makeText(context, "Por favor, informe o nome do ministro escalado.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val storageDateStr = try {
                        val d = displayDateFormat.parse(dataCultoBrInput.trim())
                        if (d != null) storageDateFormat.format(d) else dataCultoBrInput.trim()
                    } catch (_: Exception) {
                        dataCultoBrInput.trim()
                    }

                    val novaEscalaGeral = EscalaCulto(
                        nomeMinistro = ministroInput.trim().uppercase(),
                        dataCulto = storageDateStr,
                        diaSemana = diaSemanaCalculado,
                        vocal1 = vocal1Input.trim(),
                        vocal2 = vocal2Input.trim(),
                        vocal3 = vocal3Input.trim(),
                        tecladista = tecladistaInput.trim().uppercase(),
                        violonista = violonistaInput.trim().uppercase(),
                        baterista = bateristaInput.trim().uppercase(),
                        baixista = baixistaInput.trim().uppercase(),
                        itens = emptyList(),
                        statusPreenchimento = "PENDENTE_PREENCHIMENTO"
                    )

                    viewModel.salvarEscalaCulto(novaEscalaGeral, context = context) {
                        viewModel.navegarPara(TelaDestino.HOME)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("💾 Salvar Escala da Equipe", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
