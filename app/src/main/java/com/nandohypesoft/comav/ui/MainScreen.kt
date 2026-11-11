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
import com.nandohypesoft.comav.ui.culto.CadastroCultoScreen
import com.nandohypesoft.comav.ui.estatisticas.EstatisticasScreen
import com.nandohypesoft.comav.ui.home.HomeScreen
import com.nandohypesoft.comav.ui.TelaDestino
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import com.nandohypesoft.comav.util.TranspositorUtil
import com.nandohypesoft.comav.ui.components.ChordDiagramDialog
import com.nandohypesoft.comav.util.MetronomoManager
import com.nandohypesoft.comav.util.CompassoMetronomo
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalConfiguration
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
    val telaAtual by viewModel.telaAtual.collectAsState()

    if (telaAtual == TelaDestino.HOME) {
        HomeScreen(viewModel = viewModel)
        return
    }

    if (telaAtual == TelaDestino.CADASTRO_CULTO) {
        CadastroCultoScreen(viewModel = viewModel, onVoltar = { viewModel.navegarPara(TelaDestino.HOME) })
        return
    }

    if (telaAtual == TelaDestino.CADASTRO_ENSAIO) {
        com.nandohypesoft.comav.ui.ensaio.CadastroEnsaioScreen(viewModel = viewModel, onVoltar = { viewModel.navegarPara(TelaDestino.HOME) })
        return
    }

    if (telaAtual == TelaDestino.ESTATISTICAS) {
        EstatisticasScreen(viewModel = viewModel, onVoltar = { viewModel.navegarPara(TelaDestino.HOME) })
        return
    }

    if (telaAtual == TelaDestino.CONFIGURACOES_ADMIN) {
        com.nandohypesoft.comav.ui.admin.ConfiguracoesAdminScreen(viewModel = viewModel, onVoltar = { viewModel.navegarPara(TelaDestino.HOME) })
        return
    }

    if (telaAtual == TelaDestino.RELATORIO_ACESSOS) {
        com.nandohypesoft.comav.ui.admin.RelatorioAcessosScreen(viewModel = viewModel, onVoltar = { viewModel.navegarPara(TelaDestino.CONFIGURACOES_ADMIN) })
        return
    }

    LaunchedEffect(Unit) {
        viewModel.iniciarMonitoramentoSessao(context)
        viewModel.verificarEAtualizarBancoVersaoRemota()
        viewModel.mensagemAlerta.collect { msg ->
            if (msg != null) {
                snackbarHostState.showSnackbar(msg)
                viewModel.limparAlerta()
            }
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
            TextButton(
                onClick = { viewModel.navegarPara(TelaDestino.HOME) },
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text("← Voltar ao Início", fontWeight = FontWeight.Bold)
            }

            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (viewModel.modoBusca.collectAsState().value == ModoDeBusca.CIFRA) "Lista de Cifras" else "Lista de Letras",
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

                Switch(
                    checked = temaEscuro,
                    onCheckedChange = { viewModel.alterarTema(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Busca
            val placeholderTexto = if (modoBusca == ModoDeBusca.CIFRA) {
                "Buscar nº (#12), acorde (Am G) ou título"
            } else {
                "Buscar nº (#12), trecho ou título"
            }

            OutlinedTextField(
                value = consulta,
                onValueChange = { viewModel.atualizarConsulta(it) },
                placeholder = { 
                    Text(
                        text = placeholderTexto,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (telaAtual == TelaDestino.LISTA_CIFRAS) {
                OutlinedButton(
                    onClick = { viewModel.navegarPara(TelaDestino.LISTA_LETRAS) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🎵 Ir para Lista de Letras")
                }
                Spacer(modifier = Modifier.height(8.dp))
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "Nº ${musica.identificador}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = musica.titulo,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
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
                        viewModel.excluirMusica(musica)
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
        val coroutineScope = rememberCoroutineScope()
        val playlistCulto by viewModel.playlistCulto.collectAsState()
        val indicePlaylistCulto by viewModel.indicePlaylistCulto.collectAsState()

        val metronomoAtivo by MetronomoManager.ativo.collectAsState()
        val metronomoBpm by MetronomoManager.bpm.collectAsState()
        val metronomoPulso by MetronomoManager.pulsoVisual.collectAsState()
        val metronomoCompasso by MetronomoManager.compasso.collectAsState()
        val metronomoBatida by MetronomoManager.batidaAtual.collectAsState()

        var acordeParaDiagrama by remember { mutableStateOf<String?>(null) }
        var painelFerramentasExpandido by remember { mutableStateOf(false) }
        var ativarQuebraAutomatica by remember { mutableStateOf(false) }
        var modoTelaInteira by remember { mutableStateOf(false) }
        var semitonsShift by remember(musicaAberta!!.identificador) { mutableIntStateOf(0) }
        var autoScrollAtivo by remember { mutableStateOf(false) }
        var autoScrollVelocidade by remember { mutableFloatStateOf(30f) }
        var modoPalcoAtivo by remember { mutableStateOf(false) }
        var usoDuplaColunaTablet by remember { mutableStateOf(true) }

        val currentContext = LocalContext.current
        DisposableEffect(modoPalcoAtivo) {
            var ctx = currentContext
            var activityContext: Activity? = null
            while (ctx is android.content.ContextWrapper) {
                if (ctx is Activity) {
                    activityContext = ctx
                    break
                }
                ctx = ctx.baseContext
            }

            if (modoPalcoAtivo) {
                activityContext?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                activityContext?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            onDispose {
                activityContext?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        val conteudoProcessado = remember(musicaAberta!!.conteudo, semitonsShift) {
            if (semitonsShift == 0) musicaAberta!!.conteudo
            else TranspositorUtil.transporConteudo(musicaAberta!!.conteudo, semitonsShift)
        }

        if (modoTelaInteira) {
            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp
            val ehTablet = screenWidthDp >= 600
            val maxLineLength = remember(conteudoProcessado) {
                conteudoProcessado.split("\n").maxOfOrNull { it.length } ?: 30
            }
            val autoFitScale = remember(maxLineLength, screenWidthDp) {
                if (maxLineLength > 0) {
                    (screenWidthDp.toFloat() / (maxLineLength * 9.5f)).coerceIn(0.5f, 2.0f)
                } else {
                    1.0f
                }
            }

            val fullscreenScrollState = rememberScrollState()
            LaunchedEffect(autoScrollAtivo, autoScrollVelocidade) {
                if (autoScrollAtivo) {
                    while (true) {
                        kotlinx.coroutines.delay(40L)
                        val delta = (autoScrollVelocidade * 0.04f).coerceAtLeast(0.5f)
                        fullscreenScrollState.scrollBy(delta)
                    }
                }
            }

            Dialog(
                onDismissRequest = { modoTelaInteira = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF000000) else MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = musicaAberta!!.titulo,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.onBackground
                                    )
                                    if (semitonsShift != 0) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF332B00) else MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = " ${if (semitonsShift > 0) "+$semitonsShift" else "$semitonsShift"} semitons ",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Controls inside Fullscreen
                                    IconButton(onClick = { autoScrollAtivo = !autoScrollAtivo }) {
                                        Icon(
                                            imageVector = if (autoScrollAtivo) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Auto Scroll",
                                            tint = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(onClick = { modoPalcoAtivo = !modoPalcoAtivo }) {
                                        Icon(
                                            imageVector = if (modoPalcoAtivo) Icons.Default.WbSunny else Icons.Default.Nightlight,
                                            contentDescription = "Modo Palco",
                                            tint = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { modoTelaInteira = false },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Sair Tela Cheia", fontSize = 11.sp, color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            if (playlistCulto.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF222222) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.musicaAnteriorPlaylist() },
                                            enabled = indicePlaylistCulto > 0,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("⏮ Anterior", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                                        }
                                        Text(
                                            text = "Setlist Culto: Música ${indicePlaylistCulto + 1} de ${playlistCulto.size}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                        )
                                        OutlinedButton(
                                            onClick = { viewModel.proximaMusicaPlaylist() },
                                            enabled = indicePlaylistCulto < playlistCulto.size - 1,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Próxima ⏭", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val ehCifra = musicaAberta!!.tipoDeConteudo == TipoDeConteudo.CIFRA

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(fullscreenScrollState),
                                contentAlignment = Alignment.TopStart
                            ) {
                                if (ehTablet && usoDuplaColunaTablet && ehCifra) {
                                    val linhas = conteudoProcessado.split("\n")
                                    val metade = (linhas.size + 1) / 2
                                    val col1 = linhas.take(metade).joinToString("\n")
                                    val col2 = linhas.drop(metade).joinToString("\n")

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = col1,
                                            fontSize = (16 * autoFitScale).sp,
                                            lineHeight = (24 * autoFitScale).sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = col2,
                                            fontSize = (16 * autoFitScale).sp,
                                            lineHeight = (24 * autoFitScale).sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = conteudoProcessado,
                                        fontSize = (18 * autoFitScale).sp,
                                        lineHeight = (26 * autoFitScale).sp,
                                        fontFamily = if (ehCifra) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default,
                                        color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Dialog(
            onDismissRequest = { viewModel.fecharVisualizador() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF000000) else MaterialTheme.colorScheme.background
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
                                text = "Nº ${musicaAberta!!.identificador} · ${musicaAberta!!.titulo}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.onBackground
                            )
                            val tipoTexto = if (musicaAberta!!.tipoDeConteudo == TipoDeConteudo.CIFRA) "Cifra" else "Letra"
                            Text(
                                text = tipoTexto,
                                fontSize = 14.sp,
                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFCCCCCC) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { modoPalcoAtivo = !modoPalcoAtivo },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (modoPalcoAtivo) "Palco ON" else "Palco OFF",
                                    fontSize = 12.sp,
                                    color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedButton(
                                onClick = { modoTelaInteira = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Tela Cheia", fontSize = 12.sp, color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                            }
                            OutlinedButton(
                                onClick = { mostrarModoProjetor = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Transmitir", fontSize = 12.sp, color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                            }

                            TextButton(onClick = { viewModel.fecharVisualizador() }) {
                                Text("Fechar", color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (playlistCulto.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF222222) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.musicaAnteriorPlaylist() },
                                    enabled = indicePlaylistCulto > 0,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("⏮ Anterior", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    text = "Setlist: Música ${indicePlaylistCulto + 1} de ${playlistCulto.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                )
                                OutlinedButton(
                                    onClick = { viewModel.proximaMusicaPlaylist() },
                                    enabled = indicePlaylistCulto < playlistCulto.size - 1,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Próxima ⏭", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barra / Painel de Ferramentas de Leitura (Expansível para não poluir a tela de leitura)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF181818) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Linha Resumo Sempre Visível (Compacta)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF332B00) else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = " Tom: ${if (semitonsShift == 0) "Orig" else if (semitonsShift > 0) "+$semitonsShift" else semitonsShift} ",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { autoScrollAtivo = !autoScrollAtivo },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (autoScrollAtivo) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Auto Scroll",
                                            tint = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = { MetronomoManager.alternarMetronomo(coroutineScope) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (metronomoAtivo) Icons.Default.Pause else Icons.Default.MusicNote,
                                            contentDescription = "Metrônomo",
                                            tint = if (metronomoAtivo) androidx.compose.ui.graphics.Color(0xFF4CAF50) else (if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary)
                                        )
                                    }

                                    // Display de 4 LEDs do Metrônomo (Compacto)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val maxLeds = metronomoCompasso.maxBatidas
                                        for (ledIdx in 1..4) {
                                            val ledIncluso = (ledIdx <= maxLeds)
                                            val ehBatidaAtual = (metronomoAtivo && metronomoBatida == ledIdx)
                                            val corLed = when {
                                                !ledIncluso -> androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.15f)
                                                ehBatidaAtual && ledIdx == 1 -> androidx.compose.ui.graphics.Color(0xFFFF3D00)
                                                ehBatidaAtual -> androidx.compose.ui.graphics.Color(0xFFFFC107)
                                                else -> androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.4f)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(corLed, CircleShape)
                                            )
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = { painelFerramentasExpandido = !painelFerramentasExpandido },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (painelFerramentasExpandido) "⚙️ Ocultar ▼" else "⚙️ Ferramentas ▲",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Conteúdo Expandido do Painel de Ferramentas
                            AnimatedVisibility(visible = painelFerramentasExpandido) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Bloco 1: Tom & Tamanho da Fonte
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Tom:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            OutlinedButton(
                                                onClick = { semitonsShift-- },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("-1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = if (semitonsShift == 0) "Original" else "${if (semitonsShift > 0) "+$semitonsShift" else semitonsShift}st",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                            )
                                            OutlinedButton(
                                                onClick = { semitonsShift++ },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("+1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            if (semitonsShift != 0) {
                                                TextButton(onClick = { semitonsShift = 0 }) {
                                                    Text("Reset", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Fonte:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            OutlinedButton(
                                                onClick = { viewModel.alterarEscala(escalaFonte - 0.1f) },
                                                enabled = escalaFonte > 0.71f,
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("A−", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = "${(escalaFonte * 100).toInt()}%",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary
                                            )
                                            OutlinedButton(
                                                onClick = { viewModel.alterarEscala(escalaFonte + 0.1f) },
                                                enabled = escalaFonte < 1.81f,
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Bloco 2: Rolagem Automática
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Velocidade Rolagem:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Slider(
                                            value = autoScrollVelocidade,
                                            onValueChange = { autoScrollVelocidade = it },
                                            valueRange = 10f..100f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary,
                                                activeTrackColor = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = "${autoScrollVelocidade.toInt()} px/s",
                                            fontSize = 11.sp,
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFCCCCCC) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Bloco 3: Metrônomo Detalhes (BPM, Compasso, Tap Tempo)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "BPM: ${metronomoBpm}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                OutlinedButton(
                                                    onClick = { MetronomoManager.definirBpm(metronomoBpm - 5) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("-5", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { MetronomoManager.definirBpm(metronomoBpm + 5) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("+5", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { MetronomoManager.registrarTapTempo() },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("TAP TEMPO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Compasso:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                OutlinedButton(
                                                    onClick = { MetronomoManager.definirCompasso(CompassoMetronomo.TERNARIO) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    colors = if (metronomoCompasso == CompassoMetronomo.TERNARIO)
                                                        ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                                    else ButtonDefaults.outlinedButtonColors()
                                                ) {
                                                    Text("3/4 Ternário", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { MetronomoManager.definirCompasso(CompassoMetronomo.QUATERNARIO) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    colors = if (metronomoCompasso == CompassoMetronomo.QUATERNARIO)
                                                        ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                                    else ButtonDefaults.outlinedButtonColors()
                                                ) {
                                                    Text("4/4 Quaternário", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle Quebra Automática C(n)/L(n)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Modo Quebra Automática C(n)/L(n):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = ativarQuebraAutomatica,
                            onCheckedChange = { ativarQuebraAutomatica = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val configuration = LocalConfiguration.current
                    val screenWidthDp = configuration.screenWidthDp

                    val blocos = remember(conteudoProcessado, ativarQuebraAutomatica, escalaFonte, screenWidthDp) {
                        if (ativarQuebraAutomatica) {
                            val maxC = (screenWidthDp / (11 * escalaFonte)).toInt().coerceIn(18, 45)
                            QuebraAutomatica.quebraInteligente(conteudoProcessado, maxChars = maxC)
                        } else {
                            emptyList()
                        }
                    }
                    val linhasNormais = remember(conteudoProcessado) { conteudoProcessado.split("\n") }
                    val secoes = remember(conteudoProcessado) { detectarSecoesCifra(conteudoProcessado) }
                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(autoScrollAtivo, autoScrollVelocidade) {
                        if (autoScrollAtivo) {
                            while (true) {
                                kotlinx.coroutines.delay(40L)
                                val delta = (autoScrollVelocidade * 0.04f).coerceAtLeast(0.5f)
                                listState.scrollBy(delta)
                            }
                        }
                    }

                    if (secoes.isNotEmpty()) {
                        Text(
                            text = "Atalhos rápidos:",
                            fontSize = 12.sp,
                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            secoes.forEach { secao ->
                                Surface(
                                    onClick = {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(secao.indiceLinha)
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF332B00) else MaterialTheme.colorScheme.primaryContainer,
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
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    val acordesDaMusica = remember(conteudoProcessado) {
                        conteudoProcessado.split(Regex("\\s+"))
                            .map { it.replace(Regex("[()\\[\\].,;:!?|]"), "").trim() }
                            .filter { it.isNotBlank() && TranspositorUtil.ehAcorde(it) }
                            .distinct()
                    }

                    if (acordesDaMusica.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎸 Acordes (Toque para Diagrama):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            acordesDaMusica.forEach { ac ->
                                Surface(
                                    onClick = { acordeParaDiagrama = ac },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF332B00) else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = ac,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    val ehCifra = musicaAberta!!.tipoDeConteudo == TipoDeConteudo.CIFRA

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .then(if (ehCifra) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (ativarQuebraAutomatica) {
                                items(blocos) { bloco ->
                                    if (bloco.isHeader) {
                                        Text(
                                            text = bloco.cifra,
                                            fontSize = (20 * escalaFonte).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    } else if (bloco.cifra.isBlank() && bloco.letra.isBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (bloco.cifra.isNotBlank()) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF332B00) else MaterialTheme.colorScheme.primaryContainer
                                                        ) {
                                                            Text(
                                                                text = " C(${bloco.indice}) ",
                                                                fontSize = (11 * escalaFonte).sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.onPrimaryContainer
                                                            )
                                                        }
                                                        LinhaCifraInteractive(
                                                            linha = bloco.cifra,
                                                            fontSizeSp = 18 * escalaFonte,
                                                            isBold = true,
                                                            modoPalco = modoPalcoAtivo,
                                                            onAcordeClick = { acordeParaDiagrama = it }
                                                        )
                                                    }
                                                }

                                                if (bloco.letra.isNotBlank()) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFF222222) else MaterialTheme.colorScheme.secondaryContainer
                                                        ) {
                                                            Text(
                                                                text = " L(${bloco.indice}) ",
                                                                fontSize = (11 * escalaFonte).sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFE0E0E0) else MaterialTheme.colorScheme.onSecondaryContainer
                                                            )
                                                        }
                                                        SelectionContainer {
                                                            Text(
                                                                text = bloco.letra,
                                                                fontSize = (18 * escalaFonte).sp,
                                                                lineHeight = (26 * escalaFonte).sp,
                                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onBackground
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                itemsIndexed(linhasNormais) { index, linha ->
                                    val isHeader = secoes.any { it.indiceLinha == index }
                                    if (isHeader) {
                                        Text(
                                            text = if (linha.isEmpty()) " " else linha,
                                            fontSize = (18 * escalaFonte).sp,
                                            lineHeight = (28 * escalaFonte).sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = if (ehCifra) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default,
                                            color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    } else if (ehCifra) {
                                        LinhaCifraInteractive(
                                            linha = linha,
                                            fontSizeSp = 18 * escalaFonte,
                                            isBold = false,
                                            modoPalco = modoPalcoAtivo,
                                            onAcordeClick = { acordeParaDiagrama = it }
                                        )
                                    } else {
                                        SelectionContainer {
                                            Text(
                                                text = if (linha.isEmpty()) " " else linha,
                                                fontSize = (18 * escalaFonte).sp,
                                                lineHeight = (28 * escalaFonte).sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                                color = if (modoPalcoAtivo) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
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

        if (acordeParaDiagrama != null) {
            ChordDiagramDialog(
                nomeAcorde = acordeParaDiagrama!!,
                onDismissRequest = { acordeParaDiagrama = null }
            )
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

    // Diálogo de Senha da Liderança
    if (mostrarDialogoSenhaAdmin) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSenhaAdmin = false },
            title = { Text("Acesso da Liderança") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Informe a senha para acessar a gestão:", fontSize = 14.sp)
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

    // Painel de Gestão da Liderança
    if (mostrarPainelAdmin) {
        AlertDialog(
            onDismissRequest = { mostrarPainelAdmin = false },
            title = { Text("Gestão da Liderança") },
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
                        text = "Sincronização Remota:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = {
                            mostrarPainelAdmin = false
                            viewModel.sincronizarComFirebase()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🔄 Sincronizar Banco Remoto")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            mostrarPainelAdmin = false
                            viewModel.migrarParaFirebase()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("☁️ Migrar Base Local para Firebase")
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

@Composable
fun LinhaCifraInteractive(
    linha: String,
    fontSizeSp: Float,
    isBold: Boolean,
    modoPalco: Boolean,
    onAcordeClick: (String) -> Unit
) {
    if (linha.isEmpty()) {
        Text(text = " ", fontSize = fontSizeSp.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        return
    }

    val tokens = remember(linha) {
        val list = mutableListOf<Pair<String, Boolean>>()
        var i = 0
        val n = linha.length
        while (i < n) {
            if (linha[i].isWhitespace()) {
                val start = i
                while (i < n && linha[i].isWhitespace()) i++
                list.add(Pair(linha.substring(start, i), false))
            } else {
                val start = i
                while (i < n && !linha[i].isWhitespace()) i++
                val word = linha.substring(start, i)
                val isChord = TranspositorUtil.ehAcorde(word.replace(Regex("[()\\[\\].,;:!?|]"), ""))
                list.add(Pair(word, isChord))
            }
        }
        list
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        tokens.forEach { (token, isChord) ->
            if (isChord) {
                Surface(
                    onClick = { onAcordeClick(token) },
                    shape = RoundedCornerShape(4.dp),
                    color = if (modoPalco) androidx.compose.ui.graphics.Color(0xFF332B00) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 0.5.dp)
                ) {
                    Text(
                        text = token,
                        fontSize = fontSizeSp.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (modoPalco) androidx.compose.ui.graphics.Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 0.5.dp)
                    )
                }
            } else {
                Text(
                    text = token,
                    fontSize = fontSizeSp.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    color = if (modoPalco) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
