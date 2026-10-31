package com.nandohypesoft.comav.ui.ensaio

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nandohypesoft.comav.model.Ensaio
import com.nandohypesoft.comav.ui.MainViewModel
import com.nandohypesoft.comav.ui.TelaDestino
import com.nandohypesoft.comav.util.YouTubeUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroEnsaioScreen(viewModel: MainViewModel, onVoltar: () -> Unit) {
    val context = LocalContext.current

    val tercasDisponiveis = remember { calcularTercasDisponiveis() }

    var dataBrInput by remember { mutableStateOf(tercasDisponiveis.firstOrNull()?.first ?: "") }
    var dataIsoInput by remember { mutableStateOf(tercasDisponiveis.firstOrNull()?.second ?: "") }
    var linkYoutube1Input by remember { mutableStateOf("") }
    var linkYoutube2Input by remember { mutableStateOf("") }

    val thumb1 = remember(linkYoutube1Input) { YouTubeUtil.obterUrlThumbnail(linkYoutube1Input) }
    val thumb2 = remember(linkYoutube2Input) { YouTubeUtil.obterUrlThumbnail(linkYoutube2Input) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Ensaio da Equipe") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📅 Os ensaios acontecem às terças-feiras. Selecione a data e inclua os links do YouTube para estudo:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SeletorTercaSpinner(
                tercasDisponiveis = tercasDisponiveis,
                dataSelecionadaBr = dataBrInput,
                onDataSelecionada = { dataBr, dataIso ->
                    dataBrInput = dataBr
                    dataIsoInput = dataIso
                }
            )

            HorizontalDivider()

            Text(
                text = "🎵 Links dos Vídeos / Repertório no YouTube:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = linkYoutube1Input,
                onValueChange = { linkYoutube1Input = it },
                label = { Text("Link YouTube 1 (Vídeo ou Cifra)") },
                placeholder = { Text("https://www.youtube.com/watch?v=...") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = linkYoutube2Input,
                onValueChange = { linkYoutube2Input = it },
                label = { Text("Link YouTube 2 (Vídeo ou Referência)") },
                placeholder = { Text("https://youtu.be/...") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Pré-visualização das miniaturas
            if (thumb1 != null || thumb2 != null) {
                Text(
                    text = "Pré-visualização das Miniaturas:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (thumb1 != null) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                        ) {
                            AsyncImage(
                                model = thumb1,
                                contentDescription = "Miniatura 1",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (thumb2 != null) {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (thumb2 != null) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                        ) {
                            AsyncImage(
                                model = thumb2,
                                contentDescription = "Miniatura 2",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (thumb1 != null) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (dataBrInput.isBlank() || dataIsoInput.isBlank()) {
                        Toast.makeText(context, "Por favor, selecione uma terça-feira válida.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val novoEnsaio = Ensaio(
                        dataEnsaio = dataIsoInput,
                        diaSemana = "Terça-feira",
                        linkYoutube1 = linkYoutube1Input.trim(),
                        linkYoutube2 = linkYoutube2Input.trim(),
                        status = "AGENDADO"
                    )

                    viewModel.salvarEnsaio(novoEnsaio) {
                        viewModel.navegarPara(TelaDestino.HOME)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("💾 Confirmar e Cadastrar Ensaio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorTercaSpinner(
    tercasDisponiveis: List<Pair<String, String>>,
    dataSelecionadaBr: String,
    onDataSelecionada: (dataBr: String, dataIso: String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    val textoExibicao = if (dataSelecionadaBr.isBlank()) {
        "Selecione a terça-feira..."
    } else {
        "📅 $dataSelecionadaBr — Terça-feira"
    }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = textoExibicao,
            onValueChange = {},
            readOnly = true,
            label = { Text("📅 Data do Ensaio (Terças-feiras)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            shape = RoundedCornerShape(10.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            tercasDisponiveis.forEach { (dataBr, dataIso) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "📅 $dataBr (Terça-feira)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onDataSelecionada(dataBr, dataIso)
                        expandido = false
                    }
                )
            }
        }
    }
}

fun calcularTercasDisponiveis(): List<Pair<String, String>> {
    val resultado = mutableListOf<Pair<String, String>>()
    val cal = Calendar.getInstance()

    val hojeCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val formatoDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formatoStorage = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val mesAtual = cal.get(Calendar.MONTH)
    val maxDiasMesAtual = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    for (dia in cal.get(Calendar.DAY_OF_MONTH)..maxDiasMesAtual) {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.MONTH, mesAtual)
            set(Calendar.DAY_OF_MONTH, dia)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (tempCal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && !tempCal.before(hojeCal)) {
            resultado.add(Pair(formatoDisplay.format(tempCal.time), formatoStorage.format(tempCal.time)))
        }
    }

    if (resultado.isEmpty()) {
        val proximoMesCal = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val proximoMes = proximoMesCal.get(Calendar.MONTH)
        val maxDiasProximoMes = proximoMesCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (dia in 1..maxDiasProximoMes) {
            val tempCal = Calendar.getInstance().apply {
                set(Calendar.MONTH, proximoMes)
                set(Calendar.DAY_OF_MONTH, dia)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (tempCal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
                resultado.add(Pair(formatoDisplay.format(tempCal.time), formatoStorage.format(tempCal.time)))
            }
        }
    }

    return resultado
}
