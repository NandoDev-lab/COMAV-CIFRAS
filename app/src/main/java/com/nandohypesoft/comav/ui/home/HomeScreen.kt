package com.nandohypesoft.comav.ui.home

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.nandohypesoft.comav.model.EscalaCulto
import com.nandohypesoft.comav.model.TipoFuncaoEquipe
import com.nandohypesoft.comav.ui.MainViewModel
import com.nandohypesoft.comav.ui.TelaDestino
import com.nandohypesoft.comav.ui.components.DialogPerfilUsuario
import com.nandohypesoft.comav.ui.culto.CalendarioEscalaMusicosDialog
import com.nandohypesoft.comav.ui.culto.SeletorMusicoSpinner
import com.nandohypesoft.comav.util.NotificacaoManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val temaEscuro by viewModel.temaEscuro.collectAsState()
    val escalas by viewModel.escalas.collectAsState()
    val mostrarEstatisticas by viewModel.mostrarEstatisticas.collectAsState()
    val consulta by viewModel.consulta.collectAsState()
    val urlGistRemota by viewModel.urlGistRemota.collectAsState()
    val tipoFuncaoEquipe by viewModel.tipoFuncaoEquipe.collectAsState()

    var inputUrlGist by remember(urlGistRemota) { mutableStateOf(urlGistRemota) }

    var mostrarDialogPerfil by remember { mutableStateOf(false) }
    var mostrarDialogSimulacaoNotificacoes by remember { mutableStateOf(false) }
    var mostrarPainelAdmin by remember { mutableStateOf(false) }
    var mostrarDialogoSenhaAdmin by remember { mutableStateOf(false) }
    var senhaAdminDigitada by remember { mutableStateOf("") }
    var erroSenhaAdmin by remember { mutableStateOf(false) }
    var mostrarDialogoCompartilharQr by remember { mutableStateOf(false) }
    var mostrarGerenciadorCultos by remember { mutableStateOf(false) }
    var mostrarGerenciadorEscalaMusicos by remember { mutableStateOf(false) }
    var mostrarCalendarioMusicos by remember { mutableStateOf(false) }
    var escalaParaDetalhes by remember { mutableStateOf<EscalaCulto?>(null) }

    val launcherImportarDoc = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importarDocumento(context, it) }
    }

    val launcherExportarBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportarBackup(context, it) }
    }

    val launcherImportarBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importarBackup(context, it) }
    }

    LaunchedEffect(tipoFuncaoEquipe) {
        if (tipoFuncaoEquipe == TipoFuncaoEquipe.NAO_DEFINIDO) {
            mostrarDialogPerfil = true
        } else {
            viewModel.iniciarAgendadorWorkManager(context)
        }
    }

    if (mostrarDialogPerfil || tipoFuncaoEquipe == TipoFuncaoEquipe.NAO_DEFINIDO) {
        DialogPerfilUsuario(
            funcaoAtual = tipoFuncaoEquipe,
            onConfirmarFuncao = { novaFuncao ->
                viewModel.salvarTipoFuncaoEquipe(context, novaFuncao)
                mostrarDialogPerfil = false
            },
            onDismiss = { mostrarDialogPerfil = false }
        )
    }

    // Diálogo de Senha Admin
    if (mostrarDialogoSenhaAdmin) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSenhaAdmin = false },
            title = { Text("Acesso Administrativo") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Informe a senha para acessar o painel:", fontSize = 14.sp)
                    OutlinedTextField(
                        value = senhaAdminDigitada,
                        onValueChange = {
                            senhaAdminDigitada = it
                            erroSenhaAdmin = false
                        },
                        placeholder = { Text("Senha") },
                        singleLine = true,
                        isError = erroSenhaAdmin,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (erroSenhaAdmin) {
                        Text(
                            text = "Senha incorreta!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (senhaAdminDigitada == "27021986") {
                            mostrarDialogoSenhaAdmin = false
                            mostrarPainelAdmin = true
                        } else {
                            erroSenhaAdmin = true
                        }
                    }
                ) {
                    Text("Acessar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSenhaAdmin = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Painel Administrativo Completo
    if (mostrarPainelAdmin) {
        AlertDialog(
            onDismissRequest = { mostrarPainelAdmin = false },
            title = { Text("Painel Administrativo") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val mostrarEst by viewModel.mostrarEstatisticas.collectAsState()

                    Surface(
                        onClick = { mostrarDialogPerfil = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("👤 Sua Função no Grupo de Louvor:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text(
                                    text = tipoFuncaoEquipe.titulo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text("Alterar", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        onClick = { viewModel.alterarMostrarEstatisticas(!mostrarEst) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (mostrarEst) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (mostrarEst) "Estatísticas: Visíveis no Início" else "Estatísticas: Ocultas",
                                fontWeight = FontWeight.Bold,
                                color = if (mostrarEst) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = mostrarEst,
                                onCheckedChange = { viewModel.alterarMostrarEstatisticas(it) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            mostrarCalendarioMusicos = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("📅 Calendário Escala de Músicos", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            mostrarPainelAdmin = false
                            mostrarDialogSimulacaoNotificacoes = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🔔 Testar e Simular Alertas / Notificações", fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = inputUrlGist,
                        onValueChange = { inputUrlGist = it },
                        label = { Text("URL Raw do Gist (Backup)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.salvarUrlGistRemota(inputUrlGist)
                            viewModel.sincronizarComGist()
                            mostrarPainelAdmin = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sincronizar com Gist")
                    }

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            mostrarGerenciadorCultos = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("Gerenciar / Excluir Cultos", color = MaterialTheme.colorScheme.onErrorContainer)
                    }

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            launcherImportarDoc.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Importar DOCX / PDF")
                    }

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            launcherImportarBackup.launch(arrayOf("application/json"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Importar Backup (JSON)")
                    }

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            launcherExportarBackup.launch("comav_backup.json")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Exportar Backup")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarPainelAdmin = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Diálogo Gerenciar Escala de Músicos (Exclusivo Administrador)
    if (mostrarGerenciadorEscalaMusicos) {
        var escalaSelecionada by remember { mutableStateOf<EscalaCulto?>(escalas.firstOrNull()) }
        var tecladistaEdicao by remember(escalaSelecionada) { mutableStateOf(escalaSelecionada?.tecladista ?: "") }
        var violonistaEdicao by remember(escalaSelecionada) { mutableStateOf(escalaSelecionada?.violonista ?: "") }
        var bateristaEdicao by remember(escalaSelecionada) { mutableStateOf(escalaSelecionada?.baterista ?: "") }

        val opcoesTeclado = listOf("Selecione...", "Fernando", "Agnaldo", "Outro...")
        val opcoesViolao = listOf("Selecione...", "Fernando", "Victor", "Daniel", "Outro...")
        val opcoesBateria = listOf("Selecione...", "Victor", "Felipe", "Outro...")

        AlertDialog(
            onDismissRequest = { mostrarGerenciadorEscalaMusicos = false },
            title = { Text("Escala de Músicos (Administrador)") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Selecione o culto/data para cadastrar a equipe instrumental:", fontSize = 13.sp)

                    if (escalas.isEmpty()) {
                        Text("Nenhum culto agendado cadastrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        escalas.forEach { e ->
                            val ehSelecionado = escalaSelecionada?.id == e.id
                            Surface(
                                onClick = {
                                    escalaSelecionada = e
                                    tecladistaEdicao = e.tecladista
                                    violonistaEdicao = e.violonista
                                    bateristaEdicao = e.baterista
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (ehSelecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${e.diaSemana} — ${e.dataCulto}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (ehSelecionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Ministro: ${e.nomeMinistro}",
                                            fontSize = 12.sp,
                                            color = if (ehSelecionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (ehSelecionado) {
                                        Text("✓ Selecionado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        if (escalaSelecionada != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

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
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val esc = escalaSelecionada
                        if (esc != null) {
                            val escAtualizada = esc.copy(
                                tecladista = tecladistaEdicao.trim(),
                                violonista = violonistaEdicao.trim(),
                                baterista = bateristaEdicao.trim()
                            )
                            viewModel.salvarEscalaCulto(escala = escAtualizada, context = context, onSuccess = {})
                        }
                        mostrarGerenciadorEscalaMusicos = false
                    },
                    enabled = escalaSelecionada != null
                ) {
                    Text("Salvar Escala")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarGerenciadorEscalaMusicos = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo Detalhes da Equipe
    if (escalaParaDetalhes != null) {
        val esc = escalaParaDetalhes!!
        val musicoData = viewModel.obterEscalaMusicoPorDataSync(esc.dataCulto)
        val tecladistaFinal = esc.tecladista.ifBlank { musicoData?.tecladista ?: "" }
        val violonistaFinal = esc.violonista.ifBlank { musicoData?.violonista ?: "" }
        val bateristaFinal = esc.baterista.ifBlank { musicoData?.baterista ?: "" }

        AlertDialog(
            onDismissRequest = { escalaParaDetalhes = null },
            title = { Text("Detalhes da Equipe") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Culto: ${esc.diaSemana} (${esc.dataCulto})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "🎼 Dirigente / Ministro: ${esc.nomeMinistro.ifBlank { "Não informado" }}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    Text(
                        text = "Escala de Instrumentistas:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎹 Teclado:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            text = tecladistaFinal.ifBlank { "Não escalado" },
                            fontSize = 14.sp,
                            fontWeight = if (tecladistaFinal.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                            color = if (tecladistaFinal.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎸 Violão:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            text = violonistaFinal.ifBlank { "Não escalado" },
                            fontSize = 14.sp,
                            fontWeight = if (violonistaFinal.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                            color = if (violonistaFinal.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🥁 Bateria:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            text = bateristaFinal.ifBlank { "Não escalado" },
                            fontSize = 14.sp,
                            fontWeight = if (bateristaFinal.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                            color = if (bateristaFinal.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { escalaParaDetalhes = null }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Diálogo Gerenciar / Excluir Cultos
    if (mostrarGerenciadorCultos) {
        AlertDialog(
            onDismissRequest = { mostrarGerenciadorCultos = false },
            title = { Text("Gerenciar Cultos Agendados") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (escalas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum culto agendado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(escalas, key = { it.id }) { escala ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "Ministro: ${escala.nomeMinistro}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = "Data: ${escala.dataCulto} (${escala.diaSemana})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        TextButton(onClick = { viewModel.excluirCulto(escala.id) }) {
                                            Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarGerenciadorCultos = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Diálogo QR Code Compartilhar
    if (mostrarDialogoCompartilharQr) {
        val uniqueSessionId = remember { "COMAV_SHARE_${UUID.randomUUID().toString().substring(0, 8).uppercase()}" }
        val qrBitmap = remember {
            try {
                val bitMatrix = QRCodeWriter().encode(uniqueSessionId, BarcodeFormat.QR_CODE, 512, 512)
                val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
                for (x in 0 until 512) {
                    for (y in 0 until 512) {
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    }
                }
                bmp
            } catch (_: Exception) { null }
        }

        AlertDialog(
            onDismissRequest = { mostrarDialogoCompartilharQr = false },
            title = { Text("Compartilhar App & Banco (QR)") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ID Único de Sincronização:\n$uniqueSessionId",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(180.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        launcherExportarBackup.launch("comav_backup_completo.json")
                        mostrarDialogoCompartilharQr = false
                    }
                ) {
                    Text("Exportar Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCompartilharQr = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (mostrarCalendarioMusicos) {
        CalendarioEscalaMusicosDialog(
            viewModel = viewModel,
            onDismiss = { mostrarCalendarioMusicos = false }
        )
    }

    if (mostrarDialogSimulacaoNotificacoes) {
        AlertDialog(
            onDismissRequest = { mostrarDialogSimulacaoNotificacoes = false },
            title = {
                Text("🔔 Teste e Simulação de Notificações", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Clique nos botões abaixo para disparar as notificações no seu aparelho e simular os cenários em tempo real:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            NotificacaoManager.dispararNotificacaoLocal(
                                context = context,
                                titulo = "⚠️ Lembrete de Agendamento do Culto!",
                                mensagem = "Ministro, você tem culto agendado para o próximo domingo. Por favor, informe os louvores até terça-feira!",
                                notificationId = 8001
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("1. Simular Alerta para Ministro (Pendência)", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            NotificacaoManager.notificarAvisoMusicosNovoCulto(
                                context = context,
                                funcaoUsuario = TipoFuncaoEquipe.MUSICO,
                                nomeMinistro = "Fernando",
                                dataBr = "29/10/2026"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("2. Simular Alerta para Músicos (Novo Culto)", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val funcao = tipoFuncaoEquipe
                            NotificacaoManager.verificarEAgendarAlertas(
                                context = context,
                                funcaoUsuario = if (funcao == TipoFuncaoEquipe.NAO_DEFINIDO) TipoFuncaoEquipe.MINISTRO_CANTOR else funcao,
                                escalasCulto = viewModel.escalas.value,
                                escalasMusicosMap = viewModel.escalasMusicosDatas.value
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("3. Executar Regra de Checagem do Mês", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogSimulacaoNotificacoes = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "COMAV Cifras", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Gestão de Louvores e Repertório", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { mostrarDialogoCompartilharQr = true }) {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = "QR", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            senhaAdminDigitada = ""
                            erroSenhaAdmin = false
                            mostrarDialogoSenhaAdmin = true
                        }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Admin", tint = MaterialTheme.colorScheme.primary)
                        }
                        Switch(
                            checked = temaEscuro,
                            onCheckedChange = { viewModel.alterarTema(it) }
                        )
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
            // Barra de Pesquisa Rápida no Topo
            OutlinedTextField(
                value = consulta,
                onValueChange = { 
                    viewModel.atualizarConsulta(it)
                    if (it.isNotBlank()) {
                        viewModel.navegarPara(TelaDestino.LISTA_CIFRAS)
                    }
                },
                placeholder = { Text("Pesquisa rápida (#12, título ou trecho)...") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 1. Cadastrar Louvores do Culto
            Button(
                onClick = { viewModel.abrirCriacaoCulto() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("📅 Cadastrar Louvores do Culto", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // 2. Botões Quadrados Lado a Lado (Lista de Letras e Lista de Cifras)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    onClick = { viewModel.navegarPara(TelaDestino.LISTA_LETRAS) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎵", fontSize = 28.sp)
                            Text("Lista de Letras", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                Card(
                    onClick = { viewModel.navegarPara(TelaDestino.LISTA_CIFRAS) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎸", fontSize = 28.sp)
                            Text("Lista de Cifras", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            // 3. Estatísticas de Uso (Oculto até ser habilitado no Painel Administrativo)
            if (mostrarEstatisticas) {
                Button(
                    onClick = { viewModel.navegarPara(TelaDestino.ESTATISTICAS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("📊 Estatísticas de Uso", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cultos e Escalas Agendadas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hojeStr = formatoData.format(Date())
            val hojeDate = formatoData.parse(hojeStr) ?: Date()

            val escalasAtivas = escalas.filter { it.dataCulto >= hojeStr }

            if (escalasAtivas.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhum culto agendado no momento.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                escalasAtivas.forEach { escala ->
                    // Converter data yyyy-MM-dd para dd/MM/yyyy
                    val dataExibicao = try {
                        val d = formatoData.parse(escala.dataCulto)
                        if (d != null) formatoDisplay.format(d) else escala.dataCulto
                    } catch (_: Exception) {
                        escala.dataCulto
                    }

                    val cultoDate = try { formatoData.parse(escala.dataCulto) } catch (_: Exception) { null }
                    val diffDays = if (cultoDate != null) (cultoDate.time - hojeDate.time) / (1000 * 60 * 60 * 24) else 0L
                    val podeEditar = diffDays >= 1 // Visível até 1 dia antes

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Ministro: ${escala.nomeMinistro}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Culto: ${escala.diaSemana} — $dataExibicao",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text("Repertório:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            escala.itens.forEachIndexed { idx, item ->
                                val extraTag = if (item.ehExtra) " [Extra]" else ""
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text("${idx + 1}.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (item.identificadorMusica > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "Nº ${item.identificadorMusica}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${item.tituloMusica} ${if (item.cantor.isNotBlank()) "— ${item.cantor}" else ""}$extraTag",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { escalaParaDetalhes = escala }) {
                                    Text(
                                        text = "ℹ️ detalhes da equipe",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp
                                    )
                                }

                                if (podeEditar) {
                                    TextButton(onClick = { viewModel.abrirEdicaoCulto(escala) }) {
                                        Text("Editar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
