package com.nandohypesoft.comav.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nandohypesoft.comav.model.ModoDeBusca
import com.nandohypesoft.comav.model.Musica
import com.nandohypesoft.comav.model.TipoDeConteudo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val musicas by viewModel.musicas.collectAsState()
    val consulta by viewModel.consulta.collectAsState()
    val modoBusca by viewModel.modoBusca.collectAsState()
    val musicaAberta by viewModel.musicaAberta.collectAsState()
    val visualizadorVisivel by viewModel.visualizadorVisivel.collectAsState()
    val editorVisivel by viewModel.editorVisivel.collectAsState()
    val rascunho by viewModel.rascunho.collectAsState()
    val temaEscuro by viewModel.temaEscuro.collectAsState()
    val escalaFonte by viewModel.escalaFonte.collectAsState()
    val mensagemAlerta by viewModel.mensagemAlerta.collectAsState()

    var musicaParaExcluir by remember { mutableStateOf<Musica?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensagemAlerta) {
        mensagemAlerta?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparAlerta()
        }
    }

    // Launchers de Arquivo
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMAV",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Cifras e letras offline",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tema escuro",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked = temaEscuro,
                        onCheckedChange = { viewModel.alterarTema(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Busca
            val placeholderTexto = if (modoBusca == ModoDeBusca.CIFRA) {
                "Buscar acorde ou título (ex.: Am G C)"
            } else {
                "Buscar trecho da letra ou título"
            }

            OutlinedTextField(
                value = consulta,
                onValueChange = { viewModel.atualizarConsulta(it) },
                placeholder = { Text(placeholderTexto) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Seletor do Modo de Busca
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModoBuscaBotao(
                    texto = "Letras",
                    ativo = modoBusca == ModoDeBusca.LETRA,
                    onClick = { viewModel.atualizarModoBusca(ModoDeBusca.LETRA) },
                    modifier = Modifier.weight(1f)
                )
                ModoBuscaBotao(
                    texto = "Cifras",
                    ativo = modoBusca == ModoDeBusca.CIFRA,
                    onClick = { viewModel.atualizarModoBusca(ModoDeBusca.CIFRA) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.abrirCriacao() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Nova música")
                }

                OutlinedButton(
                    onClick = {
                        launcherImportarDoc.launch(
                            arrayOf(
                                "application/pdf",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Importar DOCX/PDF")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val nomePadrao = "comav-backup-${dateFormat.format(Date())}.json"
                        launcherExportarBackup.launch(nomePadrao)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Exportar backup")
                }

                OutlinedButton(
                    onClick = {
                        launcherImportarBackup.launch(arrayOf("application/json"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Abrir backup")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controle de Fonte
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Fonte",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = "A−",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.alterarEscala(escalaFonte - 0.1f) }
                )
                Text(
                    text = "A+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.alterarEscala(escalaFonte + 0.1f) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Músicas
            if (musicas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma música encontrada. Crie uma ou importe um arquivo.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(musicas, key = { it.identificador }) { musica ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.abrirVisualizador(musica) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = musica.titulo,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val tipoTexto = if (musica.tipoDeConteudo == TipoDeConteudo.CIFRA) "Cifra" else "Letra"
                                    Text(
                                        text = "$tipoTexto · ${musica.tipoDeOrigem}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                TextButton(
                                    onClick = { musicaParaExcluir = musica }
                                ) {
                                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Confirmação de Exclusão
    musicaParaExcluir?.let { musica ->
        AlertDialog(
            onDismissRequest = { musicaParaExcluir = null },
            title = { Text("Excluir música?") },
            text = { Text("“${musica.titulo}” será removida do aparelho.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.excluirMusica(musica.identificador)
                        musicaParaExcluir = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { musicaParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal Visualizador
    if (visualizadorVisivel && musicaAberta != null) {
        Dialog(
            onDismissRequest = { viewModel.fecharVisualizador() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = musicaAberta!!.titulo,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val tipoTexto = if (musicaAberta!!.tipoDeConteudo == TipoDeConteudo.CIFRA) "Cifra" else "Letra"
                            Text(
                                text = tipoTexto,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(onClick = { viewModel.fecharVisualizador() }) {
                            Text("Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val linhas = remember(musicaAberta!!.conteudo) { musicaAberta!!.conteudo.split("\n") }
                    val secoes = remember(musicaAberta!!.conteudo) { detectarSecoesCifra(musicaAberta!!.conteudo) }
                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()

                    if (secoes.isNotEmpty()) {
                        Text(
                            text = "Atalhos rápidos:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(secoes) { secao ->
                                Surface(
                                    onClick = {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(secao.indiceLinha)
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = secao.titulo,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(linhas) { index, linha ->
                            val isHeader = secoes.any { it.indiceLinha == index }
                            SelectionContainer {
                                Text(
                                    text = if (linha.isEmpty()) " " else linha,
                                    fontSize = (18 * escalaFonte).sp,
                                    lineHeight = (28 * escalaFonte).sp,
                                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = if (isHeader) 4.dp else 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            musicaAberta?.let { viewModel.abrirEdicao(it) }
                            viewModel.fecharVisualizador()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Editar música")
                    }
                }
            }
        }
    }

    // Modal Editor
    if (editorVisivel) {
        Dialog(
            onDismissRequest = { viewModel.fecharEditor() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (musicaAberta != null) "Editar música" else "Nova música",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(onClick = { viewModel.fecharEditor() }) {
                            Text("Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = rascunho.titulo,
                        onValueChange = { viewModel.atualizarRascunho(rascunho.copy(titulo = it)) },
                        placeholder = { Text("Título") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tipo de Conteúdo (Letra / Cifra)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModoBuscaBotao(
                            texto = "Letra",
                            ativo = rascunho.tipoDeConteudo == TipoDeConteudo.LETRA,
                            onClick = { viewModel.atualizarRascunho(rascunho.copy(tipoDeConteudo = TipoDeConteudo.LETRA)) },
                            modifier = Modifier.weight(1f)
                        )
                        ModoBuscaBotao(
                            texto = "Cifra",
                            ativo = rascunho.tipoDeConteudo == TipoDeConteudo.CIFRA,
                            onClick = { viewModel.atualizarRascunho(rascunho.copy(tipoDeConteudo = TipoDeConteudo.CIFRA)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rascunho.conteudo,
                        onValueChange = { viewModel.atualizarRascunho(rascunho.copy(conteudo = normalizarQuebrasDeLinha(it))) },
                        placeholder = { Text("Digite ou revise a letra/cifra") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.salvarRascunho(onSucesso = { })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Salvar música")
                    }
                }
            }
        }
    }
}

data class SecaoCifra(
    val titulo: String,
    val indiceLinha: Int
)

fun detectarSecoesCifra(conteudo: String): List<SecaoCifra> {
    val linhas = conteudo.split("\n")
    val secoes = mutableListOf<SecaoCifra>()
    
    val regexSecao = Regex("^\\[?(intro|introducao|introdução|refrao|refrão|coro|ponte|bridge|solo|verso|verse|pre-refrao|pré-refrão|outro|final|instrumental)\\d*\\b", RegexOption.IGNORE_CASE)
    
    for ((index, linha) in linhas.withIndex()) {
        val linhaLimpa = linha.trim()
        if (linhaLimpa.isEmpty()) continue
        
        val semColchetes = linhaLimpa.removeSurrounding("[", "]").removeSurrounding("(", ")").trim()
        val eCabecalho = semColchetes.length <= 35 && (
            regexSecao.containsMatchIn(linhaLimpa) ||
            semColchetes.endsWith(":") ||
            (linhaLimpa.startsWith("[") && linhaLimpa.endsWith("]")) ||
            (linhaLimpa.startsWith("(") && linhaLimpa.endsWith(")"))
        )
        
        if (eCabecalho) {
            val nomeSecao = semColchetes.removeSuffix(":").trim()
            if (nomeSecao.isNotBlank()) {
                secoes.add(SecaoCifra(titulo = nomeSecao, indiceLinha = index))
            }
        }
    }
    return secoes
}

@Composable
fun ModoBuscaBotao(
    texto: String,
    ativo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (ativo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = texto,
                fontWeight = if (ativo) FontWeight.Bold else FontWeight.Normal,
                color = if (ativo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
