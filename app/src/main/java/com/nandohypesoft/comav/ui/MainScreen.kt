package com.nandohypesoft.comav.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.UUID
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
    val urlGistRemota by viewModel.urlGistRemota.collectAsState()
    val mensagemAlerta by viewModel.mensagemAlerta.collectAsState()

    var inputUrlGist by remember(urlGistRemota) { mutableStateOf(urlGistRemota) }

    var musicaParaExcluir by remember { mutableStateOf<Musica?>(null) }
    var mostrarDialogoCompartilharQr by remember { mutableStateOf(false) }
    var mostrarModoProjetor by remember { mutableStateOf(false) }
    var mostrarPainelAdmin by remember { mutableStateOf(false) }
    var mostrarDialogoSenhaAdmin by remember { mutableStateOf(false) }
    var senhaAdminDigitada by remember { mutableStateOf("") }
    var erroSenhaAdmin by remember { mutableStateOf(false) }
    var modoExclusaoHabilitado by remember { mutableStateOf(false) }
    var mostrarSubMenuAbrirBackup by remember { mutableStateOf(false) }
    var mostrarLeitorQrCode by remember { mutableStateOf(false) }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                    IconButton(onClick = {
                        senhaAdminDigitada = ""
                        erroSenhaAdmin = false
                        mostrarDialogoSenhaAdmin = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Painel Administrativo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { mostrarDialogoCompartilharQr = true }) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Compartilhar Banco via QR Code",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

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

            // Botão de Nova Música
            Button(
                onClick = { viewModel.abrirCriacao() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("+ Nova música")
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

                                if (modoExclusaoHabilitado) {
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

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { mostrarModoProjetor = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Transmitir", fontSize = 12.sp)
                            }

                            TextButton(onClick = { viewModel.fecharVisualizador() }) {
                                Text("Fechar")
                            }
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

    // Diálogo Compartilhar App & Banco (QR Code)
    if (mostrarDialogoCompartilharQr) {
        val uniqueSessionId = remember { "COMAV_SHARE_${UUID.randomUUID().toString().substring(0, 8).uppercase()}" }
        val qrBitmap = remember { gerarQrCodeBitmap(uniqueSessionId) }

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
                            contentDescription = "QR Code de Compartilhamento",
                            modifier = Modifier.size(180.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Para enviar o banco de dados completo (todas as 94 cifras e 89 letras) para outro dispositivo, exporte o arquivo de backup e compartilhe-o.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        launcherExportarBackup.launch("comav_backup_completo.json")
                        mostrarDialogoCompartilharQr = false
                    }
                ) {
                    Text("Exportar Banco p/ Compartilhar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCompartilharQr = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal Modo Projetor / Transmissão de Tela
    if (mostrarModoProjetor && musicaAberta != null) {
        val sessionSongId = remember { "COMAV_CAST_${UUID.randomUUID().toString().substring(0, 6).uppercase()}" }
        val qrCastBitmap = remember { gerarQrCodeBitmap(sessionSongId) }

        Dialog(
            onDismissRequest = { mostrarModoProjetor = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.ui.graphics.Color.Black
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = musicaAberta!!.titulo,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Modo Transmissão • ID: $sessionSongId",
                                fontSize = 12.sp,
                                color = androidx.compose.ui.graphics.Color.LightGray
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (qrCastBitmap != null) {
                                Image(
                                    bitmap = qrCastBitmap.asImageBitmap(),
                                    contentDescription = "QR Cast",
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            TextButton(onClick = { mostrarModoProjetor = false }) {
                                Text("Sair", color = androidx.compose.ui.graphics.Color.Red)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = musicaAberta!!.conteudo,
                            fontSize = (26 * escalaFonte).sp,
                            lineHeight = (38 * escalaFonte).sp,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Text(
                        text = "COMAV • Transmissão ao Vivo",
                        fontSize = 12.sp,
                        color = androidx.compose.ui.graphics.Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    // Diálogo de Senha Administrativa
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

    // Painel Administrativo
    if (mostrarPainelAdmin) {
        AlertDialog(
            onDismissRequest = { mostrarPainelAdmin = false },
            title = { Text("Painel Administrativo") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = { modoExclusaoHabilitado = !modoExclusaoHabilitado },
                        shape = RoundedCornerShape(10.dp),
                        color = if (modoExclusaoHabilitado) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (modoExclusaoHabilitado) "Modo Exclusão: Ativo" else "Modo Exclusão: Inativo",
                                fontWeight = FontWeight.Bold,
                                color = if (modoExclusaoHabilitado) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = modoExclusaoHabilitado,
                                onCheckedChange = { modoExclusaoHabilitado = it }
                            )
                        }
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
                            launcherExportarBackup.launch("comav_backup.json")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Exportar Backup")
                    }

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            mostrarDialogoCompartilharQr = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Compartilhar Banco (QR Code)")
                    }

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            mostrarSubMenuAbrirBackup = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Abrir Backup")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sincronização Remota via Gist:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = inputUrlGist,
                        onValueChange = {
                            inputUrlGist = it
                            viewModel.salvarUrlGistRemota(it)
                        },
                        placeholder = { Text("URL Raw do Gist (https://gist...)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            viewModel.sincronizarComGist()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🔄 Sincronizar Banco do Gist")
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

    // Submenu Abrir Backup
    if (mostrarSubMenuAbrirBackup) {
        AlertDialog(
            onDismissRequest = { mostrarSubMenuAbrirBackup = false },
            title = { Text("Abrir Backup") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            mostrarSubMenuAbrirBackup = false
                            launcherImportarBackup.launch(arrayOf("application/json", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Armazenamento Interno")
                    }

                    Button(
                        onClick = {
                            mostrarSubMenuAbrirBackup = false
                            mostrarLeitorQrCode = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("QR Code")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarSubMenuAbrirBackup = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Leitor de QR Code Dialog
    if (mostrarLeitorQrCode) {
        QrCodeScannerDialog(
            onScanResult = { resultText ->
                mostrarLeitorQrCode = false
                viewModel.importarBackupTexto(resultText)
            },
            onDismiss = { mostrarLeitorQrCode = false }
        )
    }
}

fun gerarQrCodeBitmap(conteudo: String, largura: Int = 512, altura: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(conteudo, BarcodeFormat.QR_CODE, largura, altura)
        val bmp = Bitmap.createBitmap(largura, altura, Bitmap.Config.RGB_565)
        for (x in 0 until largura) {
            for (y in 0 until altura) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}

@Composable
fun QrCodeScannerDialog(
    onScanResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leitor de QR Code",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }

                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = androidx.camera.view.PreviewView(ctx)
                                val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(ctx)

                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = androidx.camera.core.Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imageAnalyzer = androidx.camera.core.ImageAnalysis.Builder()
                                        .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(
                                                ContextCompat.getMainExecutor(ctx),
                                                QrCodeAnalyzer { qrResult ->
                                                    onScanResult(qrResult)
                                                }
                                            )
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalyzer
                                        )
                                    } catch (_: Exception) {
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Permissão de câmera necessária para ler QR Code.")
                    }
                }

                Text(
                    text = "Aponte a câmera para o QR Code de backup.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : androidx.camera.core.ImageAnalysis.Analyzer {
    private val reader = com.google.zxing.MultiFormatReader().apply {
        setHints(
            mapOf(
                com.google.zxing.DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE)
            )
        )
    }

    override fun analyze(image: androidx.camera.core.ImageProxy) {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val width = image.width
        val height = image.height

        val source = com.google.zxing.PlanarYUVLuminanceSource(
            bytes, width, height, 0, 0, width, height, false
        )
        val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
        try {
            val result = reader.decode(binaryBitmap)
            onQrCodeScanned(result.text)
        } catch (_: Exception) {
        } finally {
            image.close()
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
