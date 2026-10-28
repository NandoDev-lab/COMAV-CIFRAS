package com.nandohypesoft.comav.ui.components

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogPerfilUsuario(
    funcaoAtual: TipoFuncaoEquipe,
    nomeAtual: String = "",
    onConfirmar: (TipoFuncaoEquipe, String) -> Unit,
    onDismiss: () -> Unit
) {
    var funcaoSelecionada by remember(funcaoAtual) {
        mutableStateOf(if (funcaoAtual == TipoFuncaoEquipe.NAO_DEFINIDO) TipoFuncaoEquipe.MINISTRO_CANTOR else funcaoAtual)
    }

    var nomeInput by remember(nomeAtual) {
        mutableStateOf(nomeAtual.uppercase())
    }

    var erroNome by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "👤 Cadastro de Perfil e Nome",
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
                    text = "Informe seu nome e selecione sua função no grupo de louvor COMAV:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nomeInput,
                    onValueChange = {
                        nomeInput = it.uppercase()
                        erroNome = false
                    },
                    label = { Text("Seu Nome (Obrigatório / MAIÚSCULAS)") },
                    placeholder = { Text("EX: FERNANDO, VAL, DANIEL...") },
                    singleLine = true,
                    isError = erroNome,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (erroNome) {
                    Text(
                        text = "Por favor, digite seu nome em maiúsculas.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Selecione sua função:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                val opcoes = listOf(
                    Triple(
                        TipoFuncaoEquipe.MINISTRO_CANTOR,
                        "🎤 Ministro / Cantor",
                        "Agendamento de cultos e visualização da lista de letras"
                    ),
                    Triple(
                        TipoFuncaoEquipe.MUSICO,
                        "🎸 Músico Instrumentista",
                        "Acesso às cifras, letras e escalas dos cultos agendados"
                    ),
                    Triple(
                        TipoFuncaoEquipe.ADMIN_LIDER,
                        "👑 Líder / Desenvolvedor",
                        "Acesso integral a todas as telas, funções e painel administrativo"
                    )
                )

                opcoes.forEach { (tipo, titulo, descricao) ->
                    val ehSelecionado = funcaoSelecionada == tipo

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ehSelecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { funcaoSelecionada = tipo }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = ehSelecionado,
                                onClick = { funcaoSelecionada = tipo }
                            )

                            Column {
                                Text(
                                    text = titulo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (ehSelecionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = descricao,
                                    fontSize = 11.sp,
                                    color = if (ehSelecionado) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nomeInput.isBlank()) {
                        erroNome = true
                    } else {
                        onConfirmar(funcaoSelecionada, nomeInput.trim().uppercase())
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirmar Perfil", fontWeight = FontWeight.Bold)
            }
        }
    )
}
