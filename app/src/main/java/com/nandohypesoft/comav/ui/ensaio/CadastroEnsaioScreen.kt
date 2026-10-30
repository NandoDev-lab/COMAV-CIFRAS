package com.nandohypesoft.comav.ui.ensaio

import android.content.Context
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

    val displayDateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val storageDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var dataBrInput by remember { mutableStateOf("") }
    var linkYoutube1Input by remember { mutableStateOf("") }
    var linkYoutube2Input by remember { mutableStateOf("") }

    val diaSemanaCalculado = remember(dataBrInput) {
        try {
            val date = displayDateFormat.parse(dataBrInput.trim())
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
                text = "📅 Informe a data do ensaio e os links do YouTube para estudo dos louvores:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = dataBrInput,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(8)
                    val formatted = when {
                        digits.length >= 5 -> "${digits.substring(0, 2)}/${digits.substring(2, 4)}/${digits.substring(4)}"
                        digits.length >= 3 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
                        else -> digits
                    }
                    dataBrInput = formatted
                },
                label = { Text("Data do Ensaio (DD/MM/AAAA)") },
                placeholder = { Text("Ex: 27/02/2026") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (diaSemanaCalculado.isNotBlank()) {
                Text(
                    text = "Dia da semana: $diaSemanaCalculado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
                    if (dataBrInput.isBlank() || diaSemanaCalculado.isBlank()) {
                        Toast.makeText(context, "Por favor, digite uma data válida (DD/MM/AAAA).", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val storageDateStr = try {
                        val d = displayDateFormat.parse(dataBrInput.trim())
                        if (d != null) storageDateFormat.format(d) else dataBrInput.trim()
                    } catch (_: Exception) {
                        dataBrInput.trim()
                    }

                    val novoEnsaio = Ensaio(
                        dataEnsaio = storageDateStr,
                        diaSemana = diaSemanaCalculado,
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
