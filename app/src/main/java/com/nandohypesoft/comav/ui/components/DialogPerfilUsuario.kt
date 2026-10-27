package com.nandohypesoft.comav.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onConfirmarFuncao: (TipoFuncaoEquipe) -> Unit,
    onDismiss: () -> Unit
) {
    var funcaoSelecionada by remember(funcaoAtual) {
        mutableStateOf(if (funcaoAtual == TipoFuncaoEquipe.NAO_DEFINIDO) TipoFuncaoEquipe.MINISTRO_CANTOR else funcaoAtual)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "👤 Seu Perfil no Grupo de Louvor",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Para direcionar os alertas e notificações corretos para o seu celular, selecione sua função:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val opcoes = listOf(
                    Triple(TipoFuncaoEquipe.MINISTRO_CANTOR, "🎤 Ministro / Cantor", "Recebe alertas para cadastrar louvores do culto"),
                    Triple(TipoFuncaoEquipe.MUSICO, "🎸 Músico Instrumentista", "Recebe alertas para consultar cifras e ensaiar"),
                    Triple(TipoFuncaoEquipe.AMBOS, "🎵 Ambos (Ministro e Músico)", "Recebe todos os alertas e notificações")
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
                    onConfirmarFuncao(funcaoSelecionada)
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirmar Minha Função", fontWeight = FontWeight.Bold)
            }
        }
    )
}
