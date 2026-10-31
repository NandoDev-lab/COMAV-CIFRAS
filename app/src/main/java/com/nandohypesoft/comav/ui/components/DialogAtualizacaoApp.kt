package com.nandohypesoft.comav.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandohypesoft.comav.model.VersaoApp
import com.nandohypesoft.comav.ui.MainViewModel

@Composable
fun DialogAtualizacaoApp(
    versaoApp: VersaoApp,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var baixando by remember { mutableStateOf(false) }
    var progresso by remember { mutableIntStateOf(0) }
    var erroDownload by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!versaoApp.obrigatoria && !baixando) onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🚀 Nova Atualização Disponível!", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Versão ${versaoApp.versionName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (versaoApp.publicadoEm.isNotBlank()) {
                            Text(
                                text = versaoApp.publicadoEm,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (versaoApp.notasAtualizacao.isNotBlank()) {
                    Text("O que há de novo nesta versão:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = versaoApp.notasAtualizacao,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (baixando) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Baixando atualização... $progresso%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LinearProgressIndicator(
                            progress = { progresso / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (erroDownload != null) {
                    Text(
                        text = "⚠️ $erroDownload",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!baixando) {
                        baixando = true
                        erroDownload = null
                        viewModel.baixarEInstalarAtualizacaoApp(
                            context = context,
                            urlApk = versaoApp.urlApk,
                            onProgresso = { pct -> progresso = pct },
                            onError = { err ->
                                baixando = false
                                erroDownload = err.localizedMessage ?: "Erro de conexão ao baixar o APK."
                            }
                        )
                    }
                },
                enabled = !baixando,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (baixando) "Baixando..." else "📥 Baixar e Atualizar Agora", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!versaoApp.obrigatoria && !baixando) {
                TextButton(onClick = onDismiss) {
                    Text("Lembrar Mais Tarde")
                }
            }
        }
    )
}
