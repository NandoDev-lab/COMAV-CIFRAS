package com.nandohypesoft.comav.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandohypesoft.comav.ui.MainViewModel
import com.nandohypesoft.comav.util.MembroSessao
import com.nandohypesoft.comav.util.SessaoManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioAcessosScreen(viewModel: MainViewModel, onVoltar: () -> Unit) {
    var listaAcessos by remember { mutableStateOf<List<MembroSessao>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }

    val sdfHoraData = remember { SimpleDateFormat("dd/MM/yyyy — HH:mm:ss", Locale.getDefault()) }

    fun carregarRelatorio() {
        carregando = true
        SessaoManager.obterHistoricoAcessos { resultado ->
            listaAcessos = resultado
            carregando = false
        }
    }

    LaunchedEffect(Unit) {
        carregarRelatorio()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório de Acessos e Sessões") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { carregarRelatorio() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Histórico de logins, sessões ativas e saídas dos membros da equipe:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (carregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (listaAcessos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhum histórico de acesso registrado até o momento.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaAcessos, key = { "${it.dispositivoId}_${it.inicioEm}" }) { sessao ->
                        val ehOnline = sessao.status == "ONLINE"
                        val horaEntradaStr = if (sessao.inicioEm > 0) sdfHoraData.format(Date(sessao.inicioEm)) else sessao.horarioInicio
                        val horaSaidaStr = if (sessao.fimEm > 0) sdfHoraData.format(Date(sessao.fimEm)) else "Sessão Ativa"

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (ehOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${sessao.funcao} — ${sessao.nomeUsuario}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (ehOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (ehOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = if (ehOnline) "🟢 ONLINE" else "🔴 OFFLINE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ehOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "🟢 Entrada: $horaEntradaStr",
                                    fontSize = 12.sp,
                                    color = if (ehOnline) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!ehOnline && sessao.fimEm > 0) {
                                    Text(
                                        text = "🔴 Saída: $horaSaidaStr",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
