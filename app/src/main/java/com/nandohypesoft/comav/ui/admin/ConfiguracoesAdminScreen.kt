package com.nandohypesoft.comav.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandohypesoft.comav.model.TipoFuncaoEquipe
import com.nandohypesoft.comav.ui.MainViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesAdminScreen(viewModel: MainViewModel, onVoltar: () -> Unit) {
    val context = LocalContext.current
    val tipoFuncaoEquipe by viewModel.tipoFuncaoEquipe.collectAsState()
    val mostrarEst by viewModel.mostrarEstatisticas.collectAsState()

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

    Scaffold(
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
