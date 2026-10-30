package com.nandohypesoft.comav.ui.admin

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandohypesoft.comav.model.TipoFuncaoEquipe
import com.nandohypesoft.comav.ui.MainViewModel
import com.nandohypesoft.comav.ui.culto.CalendarioEscalaMusicosDialog
import com.nandohypesoft.comav.util.NotificacaoManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesAdminScreen(viewModel: MainViewModel, onVoltar: () -> Unit) {
    val context = LocalContext.current
    val tipoFuncaoEquipe by viewModel.tipoFuncaoEquipe.collectAsState()
    val mostrarEst by viewModel.mostrarEstatisticas.collectAsState()
    val mensagemAlerta by viewModel.mensagemAlerta.collectAsState()
    val escalas by viewModel.escalas.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarGerenciadorCultos by remember { mutableStateOf(false) }
    var mostrarCalendarioMusicos by remember { mutableStateOf(false) }
    var mostrarDialogSimulacaoNotificacoes by remember { mutableStateOf(false) }

    val launcherPermissaoNotificacao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(mensagemAlerta) {
        mensagemAlerta?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparAlerta()
        }
    }

    LaunchedEffect(mostrarDialogSimulacaoNotificacoes) {
        if (mostrarDialogSimulacaoNotificacoes && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcherPermissaoNotificacao.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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

    // Calendário de Escala de Músicos
    if (mostrarCalendarioMusicos) {
        CalendarioEscalaMusicosDialog(
            viewModel = viewModel,
            onDismiss = { mostrarCalendarioMusicos = false }
        )
    }

    // Diálogo Simulação de Notificações
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
                                nomeMinistro = "FERNANDO",
                                diaSemanaCulto = "Domingo"
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

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⚙️ Configurações de Notificação do Android", fontSize = 12.sp)
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configurações do Sistema") },
                navigationIcon = {
                    TextButton(onClick = onVoltar) {
                        Text("← Voltar")
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
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("👤 Nível de Acesso Logado:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(
                            text = tipoFuncaoEquipe.titulo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Surface(
                onClick = { viewModel.alterarMostrarEstatisticas(!mostrarEst) },
                shape = RoundedCornerShape(10.dp),
                color = if (mostrarEst) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (mostrarEst) "Estatísticas: Visíveis na Home" else "Estatísticas: Ocultas",
                        fontWeight = FontWeight.Bold,
                        color = if (mostrarEst) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = mostrarEst,
                        onCheckedChange = { viewModel.alterarMostrarEstatisticas(it) }
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = "Gestão de Escalas e Cultos",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = {
                    mostrarCalendarioMusicos = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("📅 Calendário Escala de Músicos", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    mostrarGerenciadorCultos = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text("🗑️ Gerenciar / Excluir Cultos Agendados", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    mostrarDialogSimulacaoNotificacoes = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🔔 Testar e Simular Alertas / Notificações", fontWeight = FontWeight.Bold)
            }

            HorizontalDivider()

            Text(
                text = "Banco de Dados & Sincronização",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = {
                    viewModel.sincronizarComFirebase()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🔄 Sincronizar Banco Remoto", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = {
                    viewModel.migrarParaFirebase()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("☁️ Subir Base Local para Nuvem (Migração)")
            }

            HorizontalDivider()

            Text(
                text = "Backup & Restauração (Arquivos)",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = {
                    launcherImportarDoc.launch(arrayOf("*/*"))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Importar DOCX / PDF")
            }

            Button(
                onClick = {
                    launcherImportarBackup.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Importar Backup (JSON)")
            }

            Button(
                onClick = {
                    launcherExportarBackup.launch("comav_backup.json")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Exportar Backup (JSON)")
            }
        }
    }
}
