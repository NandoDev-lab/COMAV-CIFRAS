package com.nandohypesoft.comav.ui

import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nandohypesoft.comav.data.database.EnsaioDao
import com.nandohypesoft.comav.data.database.EscalaCultoDao
import com.nandohypesoft.comav.data.database.EscalaMusicoDataDao
import com.nandohypesoft.comav.data.importation.DocumentExtractor
import com.nandohypesoft.comav.data.preferences.UserPreferencesRepository
import com.nandohypesoft.comav.data.repository.CultoRepository
import com.nandohypesoft.comav.data.repository.EnsaioRepository
import com.nandohypesoft.comav.data.repository.MusicaRepository
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nandohypesoft.comav.model.Ensaio
import com.nandohypesoft.comav.model.EscalaCulto
import com.nandohypesoft.comav.model.EscalaMusicoData
import com.nandohypesoft.comav.model.EstatisticaMusica
import com.nandohypesoft.comav.model.ModoDeBusca
import com.nandohypesoft.comav.model.Musica
import com.nandohypesoft.comav.model.MusicaNova
import com.nandohypesoft.comav.model.TipoDeConteudo
import com.nandohypesoft.comav.model.TipoDeOrigem
import com.nandohypesoft.comav.model.TipoFuncaoEquipe
import com.nandohypesoft.comav.util.NotificacaoManager
import com.nandohypesoft.comav.util.NotificacaoWorker
import com.nandohypesoft.comav.util.SessaoManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TelaDestino {
    HOME,
    LISTA_LETRAS,
    LISTA_CIFRAS,
    CADASTRO_CULTO,
    CADASTRO_ENSAIO,
    ESTATISTICAS,
    CONFIGURACOES_ADMIN,
    RELATORIO_ACESSOS
}

fun normalizarQuebrasDeLinha(texto: String): String {
    return texto.replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("[\u2028\u2029]".toRegex(), "\n")
}

fun criarRascunho(musica: Musica? = null): MusicaNova {
    return if (musica != null) {
        MusicaNova(
            titulo = musica.titulo,
            conteudo = musica.conteudo,
            conteudoOriginal = musica.conteudoOriginal,
            tipoDeConteudo = musica.tipoDeConteudo,
            tipoDeOrigem = musica.tipoDeOrigem,
            nomeDoArquivoDeOrigem = musica.nomeDoArquivoDeOrigem
        )
    } else {
        MusicaNova(
            titulo = "",
            conteudo = "",
            conteudoOriginal = "",
            tipoDeConteudo = TipoDeConteudo.LETRA,
            tipoDeOrigem = TipoDeOrigem.MANUAL,
            nomeDoArquivoDeOrigem = null
        )
    }
}

class MainViewModel(
    private val repository: MusicaRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val escalaCultoDao: EscalaCultoDao? = null,
    private val escalaMusicoDataDao: EscalaMusicoDataDao? = null,
    private val ensaioDao: EnsaioDao? = null
) : ViewModel() {

    private val cultoRepository = CultoRepository(escalaCultoDao, escalaMusicoDataDao)
    private val ensaioRepository = EnsaioRepository(ensaioDao)

    private val _telaAtual = MutableStateFlow(TelaDestino.HOME)
    val telaAtual: StateFlow<TelaDestino> = _telaAtual.asStateFlow()

    private val _escalas = MutableStateFlow<List<EscalaCulto>>(emptyList())
    val escalas: StateFlow<List<EscalaCulto>> = _escalas.asStateFlow()

    private val _ensaios = MutableStateFlow<List<Ensaio>>(emptyList())
    val ensaios: StateFlow<List<Ensaio>> = _ensaios.asStateFlow()

    private val _escalaEmEdicao = MutableStateFlow<EscalaCulto?>(null)
    val escalaEmEdicao: StateFlow<EscalaCulto?> = _escalaEmEdicao.asStateFlow()

    private val _escalasMusicosDatas = MutableStateFlow<Map<String, EscalaMusicoData>>(emptyMap())
    val escalasMusicosDatas: StateFlow<Map<String, EscalaMusicoData>> = _escalasMusicosDatas.asStateFlow()

    private val _musicas = MutableStateFlow<List<Musica>>(emptyList())
    val musicas: StateFlow<List<Musica>> = _musicas.asStateFlow()

    private val _todasMusicas = MutableStateFlow<List<Musica>>(emptyList())
    val todasMusicas: StateFlow<List<Musica>> = _todasMusicas.asStateFlow()

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta.asStateFlow()

    private val _modoBusca = MutableStateFlow(ModoDeBusca.LETRA)
    val modoBusca: StateFlow<ModoDeBusca> = _modoBusca.asStateFlow()

    private val _musicaAberta = MutableStateFlow<Musica?>(null)
    val musicaAberta: StateFlow<Musica?> = _musicaAberta.asStateFlow()

    private val _visualizadorVisivel = MutableStateFlow(false)
    val visualizadorVisivel: StateFlow<Boolean> = _visualizadorVisivel.asStateFlow()

    private val _editorVisivel = MutableStateFlow(false)
    val editorVisivel: StateFlow<Boolean> = _editorVisivel.asStateFlow()

    private val _rascunho = MutableStateFlow(criarRascunho())
    val rascunho: StateFlow<MusicaNova> = _rascunho.asStateFlow()

    private val _mensagemAlerta = MutableStateFlow<String?>(null)
    val mensagemAlerta: StateFlow<String?> = _mensagemAlerta.asStateFlow()

    val temaEscuro: StateFlow<Boolean> = userPreferencesRepository.temaEscuro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val escalaFonte: StateFlow<Float> = userPreferencesRepository.escalaFonte
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

// urlGistRemota removido

    val mostrarEstatisticas: StateFlow<Boolean> = userPreferencesRepository.mostrarEstatisticas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val mostrarPainelConectados: StateFlow<Boolean> = userPreferencesRepository.mostrarPainelConectados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun alterarMostrarPainelConectados(ativo: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.salvarMostrarPainelConectados(ativo)
        }
    }

    val versaoBancoLocal: StateFlow<Long> = userPreferencesRepository.versaoBancoLocal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)

    private val _versaoAppRemota = MutableStateFlow<com.nandohypesoft.comav.model.VersaoApp?>(null)
    val versaoAppRemota: StateFlow<com.nandohypesoft.comav.model.VersaoApp?> = _versaoAppRemota.asStateFlow()

    fun verificarAtualizacaoAppRemoto(versionCodeAtual: Long = 1L) {
        viewModelScope.launch {
            try {
                val novaVersao = com.nandohypesoft.comav.util.UpdateManager.verificarNovaVersaoRemota(versionCodeAtual)
                _versaoAppRemota.value = novaVersao
            } catch (_: Exception) {}
        }
    }

    fun dispensarAlertaAtualizacaoApp() {
        _versaoAppRemota.value = null
    }

    fun baixarEInstalarAtualizacaoApp(
        context: Context,
        urlApk: String,
        onProgresso: (porcentagem: Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            com.nandohypesoft.comav.util.UpdateManager.baixarEInstalarApk(
                context = context,
                urlApk = urlApk,
                onProgresso = onProgresso,
                onSucesso = {
                    _versaoAppRemota.value = null
                },
                onError = onError
            )
        }
    }

    fun publicarNovaVersaoApp(versao: com.nandohypesoft.comav.model.VersaoApp, onConcluido: () -> Unit) {
        viewModelScope.launch {
            try {
                _mensagemAlerta.value = "Publicando nova versão do APK no Firebase..."
                com.nandohypesoft.comav.util.UpdateManager.publicarNovaVersaoApp(versao)
                _mensagemAlerta.value = "✅ Nova versão (${versao.versionName}) publicada com sucesso para toda a equipe!"
                onConcluido()
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao publicar versão: ${e.localizedMessage}"
            }
        }
    }

    val tipoFuncaoEquipe: StateFlow<TipoFuncaoEquipe> = userPreferencesRepository.tipoFuncaoEquipe
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TipoFuncaoEquipe.NAO_DEFINIDO)

    val nomeUsuario: StateFlow<String> = userPreferencesRepository.nomeUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private var saudacaoInicialExibida = false

    fun exibirSaudacaoInicialSeNecessario(nome: String) {
        if (!saudacaoInicialExibida && nome.isNotBlank()) {
            saudacaoInicialExibida = true
            _mensagemAlerta.value = com.nandohypesoft.comav.util.SaudacaoUtil.gerarSaudacao(nome)
        }
    }

    fun consultarEPersistirPerfilDispositivoFirebase(context: Context) {
        viewModelScope.launch {
            try {
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: return@launch
                val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("perfis_dispositivos")
                    .document(androidId)
                    .get()
                    .await()

                if (doc != null && doc.exists()) {
                    val funcaoStr = doc.getString("funcao")
                    if (!funcaoStr.isNullOrBlank()) {
                        try {
                            val funcaoRemota = TipoFuncaoEquipe.valueOf(funcaoStr)
                            userPreferencesRepository.salvarTipoFuncaoEquipe(funcaoRemota)
                        } catch (_: Exception) {}
                    }
                    val nomeStr = doc.getString("nomeUsuario")
                    if (!nomeStr.isNullOrBlank()) {
                        userPreferencesRepository.salvarNomeUsuario(nomeStr)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun salvarPerfilENomeUsuario(context: Context, tipo: TipoFuncaoEquipe, nome: String) {
        viewModelScope.launch {
            val nomeMaiusculo = nome.trim().uppercase()
            userPreferencesRepository.salvarTipoFuncaoEquipe(tipo)
            userPreferencesRepository.salvarNomeUsuario(nomeMaiusculo)
            NotificacaoManager.registrarPerfilDispositivoFirebase(context, tipo, nomeMaiusculo)
            SessaoManager.iniciarSessaoAtiva(context, nomeMaiusculo, tipo)
            SessaoManager.escutarAtividadesSessaoFirebase(context, tipo, nomeMaiusculo)
            iniciarAgendadorWorkManager(context)
            NotificacaoManager.verificarEAgendarAlertas(
                context = context,
                funcaoUsuario = tipo,
                escalasCulto = _escalas.value,
                escalasMusicosMap = _escalasMusicosDatas.value
            )
        }
    }

    fun iniciarMonitoramentoSessao(context: Context) {
        viewModelScope.launch {
            combine(nomeUsuario, tipoFuncaoEquipe) { nome, tipo ->
                val nomeFinal = if (nome.isBlank()) "FERNANDO" else nome.trim().uppercase()
                val tipoFinal = if (tipo == TipoFuncaoEquipe.NAO_DEFINIDO) TipoFuncaoEquipe.ADMIN_LIDER else tipo
                Pair(nomeFinal, tipoFinal)
            }.collect { (nomeFinal, tipoFinal) ->
                SessaoManager.iniciarSessaoAtiva(context, nomeFinal, tipoFinal)
                SessaoManager.escutarAtividadesSessaoFirebase(context, tipoFinal, nomeFinal)
            }
        }
    }

    fun iniciarAgendadorWorkManager(context: Context) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<NotificacaoWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "comav_notificacao_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (_: Exception) {}
    }

    fun alterarMostrarEstatisticas(ativo: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.salvarMostrarEstatisticas(ativo)
        }
    }

    val estatisticasMusicas: StateFlow<List<EstatisticaMusica>> = combine(_escalas, _musicas) { escalasList, musicasList ->
        val mapaUso = mutableMapOf<String, Int>()
        val mapaMesAno = mutableMapOf<String, MutableMap<String, Int>>()
        val mapaMinistros = mutableMapOf<String, MutableMap<String, Int>>()
        val mapaCantores = mutableMapOf<String, MutableMap<String, Int>>()

        for (escala in escalasList) {
            val mesAno = escala.dataCulto.takeIf { it.length >= 7 }?.let {
                val parts = it.split("-")
                if (parts.size == 3) "${parts[1]}/${parts[0]}" else "Geral"
            } ?: "Geral"

            for (item in escala.itens) {
                val titulo = item.tituloMusica.trim()
                if (titulo.isNotEmpty()) {
                    mapaUso[titulo] = (mapaUso[titulo] ?: 0) + 1
                    mapaMesAno.getOrPut(titulo) { mutableMapOf() }.let { m ->
                        m[mesAno] = (m[mesAno] ?: 0) + 1
                    }
                    if (escala.nomeMinistro.isNotBlank()) {
                        mapaMinistros.getOrPut(titulo) { mutableMapOf() }.let { m ->
                            m[escala.nomeMinistro] = (m[escala.nomeMinistro] ?: 0) + 1
                        }
                    }
                    if (item.cantor.isNotBlank()) {
                        mapaCantores.getOrPut(titulo) { mutableMapOf() }.let { m ->
                            m[item.cantor] = (m[item.cantor] ?: 0) + 1
                        }
                    }
                }
            }
        }

        musicasList.map { musica ->
            val titulo = musica.titulo
            val total = mapaUso[titulo] ?: 0
            val mesAnoMap = mapaMesAno[titulo] ?: emptyMap()
            val ministroTop = mapaMinistros[titulo]?.maxByOrNull { it.value }?.key ?: "N/D"
            val cantorTop = mapaCantores[titulo]?.maxByOrNull { it.value }?.key ?: "N/D"

            EstatisticaMusica(
                tituloMusica = titulo,
                totalUso = total,
                repeticoesPorMesAno = mesAnoMap,
                ministroMaisFrequente = ministroTop,
                cantorMaisFrequente = cantorTop
            )
        }.filter { it.totalUso > 0 }
         .sortedByDescending { it.totalUso }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        recarregar()
        sincronizarGistSilencioso()
        carregarEscalasCulto()
        carregarEscalasMusicosData()
        carregarEnsaios()
        carregarTodasMusicas()
    }

    fun carregarEnsaios() {
        viewModelScope.launch {
            try {
                _ensaios.value = ensaioRepository.listarEnsaios()
            } catch (_: Exception) {}
        }
    }

    fun salvarEnsaio(ensaio: Ensaio, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                ensaioRepository.salvarEnsaio(ensaio)
                carregarEnsaios()
                _mensagemAlerta.value = "Ensaio cadastrado com sucesso!"
                onSuccess()
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao salvar ensaio: ${e.localizedMessage}"
            }
        }
    }

    fun confirmarEnsaio(id: String) {
        viewModelScope.launch {
            try {
                ensaioRepository.atualizarStatusEnsaio(id, "CONFIRMADO")
                carregarEnsaios()
                _mensagemAlerta.value = "Ensaio CONFIRMADO com sucesso!"
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao confirmar ensaio: ${e.localizedMessage}"
            }
        }
    }

    fun cancelarEnsaio(ensaio: Ensaio, context: Context) {
        viewModelScope.launch {
            try {
                ensaioRepository.atualizarStatusEnsaio(ensaio.id, "CANCELADO")
                carregarEnsaios()

                NotificacaoManager.notificarEnsaioCancelado(
                    context = context,
                    dataEnsaioStr = ensaio.dataEnsaio,
                    diaSemanaStr = ensaio.diaSemana
                )

                _mensagemAlerta.value = "Ensaio CANCELADO! Todos os membros foram notificados."
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao cancelar ensaio: ${e.localizedMessage}"
            }
        }
    }

    fun carregarEscalasMusicosData() {
        viewModelScope.launch {
            try {
                val lista = cultoRepository.listarTodasEscalasMusicosData()
                _escalasMusicosDatas.value = lista.associateBy { it.dataIso }
            } catch (_: Exception) {}
        }
    }

    fun salvarEscalaMusicoData(escalaMusico: EscalaMusicoData, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Atualiza em memória imediatamente
                val mapaAtual = _escalasMusicosDatas.value.toMutableMap()
                mapaAtual[escalaMusico.dataIso] = escalaMusico
                _escalasMusicosDatas.value = mapaAtual

                cultoRepository.salvarEscalaMusicoData(escalaMusico)
                carregarEscalasMusicosData()
                _mensagemAlerta.value = "Escala de músicos salva com sucesso!"
                onSuccess()
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao salvar escala de músicos: ${e.localizedMessage}"
            }
        }
    }

    fun obterEscalaMusicoPorDataSync(dataIso: String): EscalaMusicoData? {
        return _escalasMusicosDatas.value[dataIso]
    }

    fun carregarTodasMusicas() {
        viewModelScope.launch {
            try {
                val letras = repository.listarMusicas("", ModoDeBusca.LETRA)
                val cifras = repository.listarMusicas("", ModoDeBusca.CIFRA)
                _todasMusicas.value = (letras + cifras).distinctBy { it.titulo.lowercase().trim() }
            } catch (_: Exception) {}
        }
    }

    fun navegarPara(tela: TelaDestino) {
        _telaAtual.value = tela
        if (tela == TelaDestino.LISTA_LETRAS) {
            atualizarModoBusca(ModoDeBusca.LETRA)
        } else if (tela == TelaDestino.LISTA_CIFRAS) {
            atualizarModoBusca(ModoDeBusca.CIFRA)
        }
    }

    fun carregarEscalasCulto() {
        viewModelScope.launch {
            try {
                _escalas.value = cultoRepository.listarEscalas()
            } catch (_: Exception) {}
        }
    }

    fun abrirCriacaoCulto() {
        _escalaEmEdicao.value = null
        _telaAtual.value = TelaDestino.CADASTRO_CULTO
    }

    fun abrirEdicaoCulto(escala: EscalaCulto) {
        _escalaEmEdicao.value = escala
        _telaAtual.value = TelaDestino.CADASTRO_CULTO
    }

    fun salvarEscalaCulto(escala: EscalaCulto, context: Context? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val escalaFinal = if (_escalaEmEdicao.value != null) {
                    escala.copy(id = _escalaEmEdicao.value!!.id)
                } else {
                    escala
                }
                cultoRepository.salvarEscala(escalaFinal)
                carregarEscalasCulto()
                _escalaEmEdicao.value = null
                _mensagemAlerta.value = "Louvores do culto salvos com sucesso!"

                if (context != null) {
                    NotificacaoManager.notificarAvisoMusicosNovoCulto(
                        context = context,
                        nomeMinistro = escalaFinal.nomeMinistro,
                        diaSemanaCulto = escalaFinal.diaSemana
                    )
                }

                onSuccess()
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao salvar no Firebase: ${e.localizedMessage}"
            }
        }
    }

    fun excluirCulto(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                cultoRepository.excluirEscala(id)
                carregarEscalasCulto()
                _mensagemAlerta.value = "Culto excluído com sucesso do banco de dados!"
                onSuccess()
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao excluir culto: ${e.localizedMessage}"
            }
        }
    }

    private fun sincronizarGistSilencioso() {
        viewModelScope.launch {
            try {
                // Sincroniza em background apenas pelo Firebase agora
                repository.sincronizarComFirebase()
                recarregar()
            } catch (e: Exception) {
                // Ignorar erro silencioso em background
            }
        }
    }

    fun recarregar() {
        viewModelScope.launch {
            _musicas.value = repository.listarMusicas(_consulta.value, _modoBusca.value)
        }
    }

    fun atualizarConsulta(novaConsulta: String) {
        _consulta.value = novaConsulta
        recarregar()
    }

    fun atualizarModoBusca(novoModo: ModoDeBusca) {
        _modoBusca.value = novoModo
        recarregar()
    }

    fun abrirCriacao() {
        _musicaAberta.value = null
        _rascunho.value = criarRascunho()
        _editorVisivel.value = true
    }

    fun abrirEdicao(musica: Musica) {
        _musicaAberta.value = musica
        _rascunho.value = criarRascunho(musica)
        _editorVisivel.value = true
    }

    fun abrirVisualizador(musica: Musica) {
        _musicaAberta.value = musica
        _visualizadorVisivel.value = true
    }

    fun fecharVisualizador() {
        _visualizadorVisivel.value = false
    }

    fun fecharEditor() {
        _editorVisivel.value = false
    }

    fun atualizarRascunho(novoRascunho: MusicaNova) {
        _rascunho.value = novoRascunho
    }

    fun salvarRascunho(onSucesso: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.salvarMusica(_rascunho.value, _musicaAberta.value?.identificador)
                recarregar()
                _editorVisivel.value = false
                _mensagemAlerta.value = "Música salva com sucesso!"
                onSucesso()
            } catch (e: Exception) {
                _mensagemAlerta.value = e.localizedMessage ?: "Erro ao salvar música."
            }
        }
    }

    fun importarDocumento(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val doc = DocumentExtractor.extrairDocumento(context, uri)
                val texto = doc.texto
                _musicaAberta.value = null
                _rascunho.value = MusicaNova(
                    titulo = doc.tituloSugerido,
                    conteudo = texto,
                    conteudoOriginal = texto,
                    tipoDeConteudo = TipoDeConteudo.CIFRA,
                    tipoDeOrigem = doc.tipoDeOrigem,
                    nomeDoArquivoDeOrigem = doc.nomeDoArquivo
                )
                _editorVisivel.value = true
            } catch (e: Exception) {
                _mensagemAlerta.value = e.localizedMessage ?: "Ocorreu um erro ao importar o arquivo."
            }
        }
    }

    fun excluirMusica(musica: Musica) {
        viewModelScope.launch {
            repository.excluirMusica(musica.identificador, musica.tipoDeConteudo)
            recarregar()
        }
    }

    fun alterarTema(ativo: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.salvarTemaEscuro(ativo)
        }
    }

    fun alterarEscala(novaEscala: Float) {
        val escalaClamp = novaEscala.coerceIn(0.7f, 1.5f)
        viewModelScope.launch {
            userPreferencesRepository.salvarEscalaFonte(escalaClamp)
        }
    }

    fun exportarBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val backup = repository.exportarBanco()
                val json = Gson().toJson(backup)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
                _mensagemAlerta.value = "Backup exportado com sucesso!"
            } catch (e: Exception) {
                _mensagemAlerta.value = "Não foi possível exportar: ${e.localizedMessage}"
            }
        }
    }

    fun importarBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonTexto = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                } ?: throw IllegalArgumentException("Não foi possível ler o arquivo de backup.")

                val qtd = repository.importarBanco(jsonTexto)
                recarregar()
                _mensagemAlerta.value = "Backup restaurado: $qtd música(s) adicionada(s)."
            } catch (e: Exception) {
                _mensagemAlerta.value = "Não foi possível importar o backup: ${e.localizedMessage}"
            }
        }
    }

    fun gerarPayloadQrCodeBackup(onPronto: (payloadJson: String, versao: Long) -> Unit) {
        viewModelScope.launch {
            try {
                val versaoAtual = userPreferencesRepository.versaoBancoLocal.first()
                val qtdMusicas = _musicas.value.size

                val jsonObject = org.json.JSONObject().apply {
                    put("comav_backup", true)
                    put("versao_banco", versaoAtual)
                    put("qtdMusicas", qtdMusicas)
                    put("gerado_em", SimpleDateFormat("dd/MM/yyyy — HH:mm", Locale.getDefault()).format(Date()))
                }
                onPronto(jsonObject.toString(), versaoAtual)
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao gerar QR Code: ${e.localizedMessage}"
            }
        }
    }

    fun importarBackupTexto(jsonTexto: String) {
        viewModelScope.launch {
            try {
                val jsonObject = org.json.JSONObject(jsonTexto)
                val isComavBackup = jsonObject.optBoolean("comav_backup", false) || jsonObject.optString("formato") == "COMAV_BACKUP"
                val versaoQr = jsonObject.optLong("versao_banco", jsonObject.optLong("versao", 1L))
                val versaoAtual = userPreferencesRepository.versaoBancoLocal.first()

                if (isComavBackup || jsonObject.has("musicas")) {
                    if (versaoQr <= versaoAtual && versaoAtual > 1L) {
                        _mensagemAlerta.value = "ℹ️ Seu banco de dados já está na versão mais recente (v$versaoAtual) ou igual."
                        return@launch
                    }

                    val qtd = repository.importarBanco(jsonTexto)

                    userPreferencesRepository.atualizarVersaoBancoLocal(versaoQr)
                    recarregar()
                    _mensagemAlerta.value = "✅ Banco atualizado com sucesso para a versão v$versaoQr! ($qtd músicas)"
                } else {
                    val qtd = repository.importarBanco(jsonTexto)
                    userPreferencesRepository.incrementarVersaoBancoLocal()
                    recarregar()
                    _mensagemAlerta.value = "✅ Backup QR importado com sucesso: $qtd música(s) adicionada(s)."
                }
            } catch (e: Exception) {
                _mensagemAlerta.value = "⚠️ QR Code inválido ou não é um backup COMAV: ${e.localizedMessage}"
            }
        }
    }

    fun verificarEAtualizarBancoVersaoRemota() {
        viewModelScope.launch {
            try {
                val versaoRemota = repository.obterVersaoBancoRemoto()
                val versaoAtual = userPreferencesRepository.versaoBancoLocal.first()

                if (versaoRemota > versaoAtual) {
                    _mensagemAlerta.value = "🔄 Nova versão do banco (v$versaoRemota) na nuvem! Atualizando..."
                    val qtd = repository.recarregarBancoDoZeroDoFirebase()
                    userPreferencesRepository.atualizarVersaoBancoLocal(versaoRemota)
                    recarregar()
                    _mensagemAlerta.value = "✅ Banco de dados atualizado do zero para a versão v$versaoRemota ($qtd músicas)!"
                }
            } catch (_: Exception) {}
        }
    }

    fun sincronizarComFirebase() {
        viewModelScope.launch {
            try {
                _mensagemAlerta.value = "Conectando ao Firebase..."
                val versaoRemota = repository.obterVersaoBancoRemoto()
                val versaoAtual = userPreferencesRepository.versaoBancoLocal.first()

                if (versaoRemota > versaoAtual) {
                    val qtd = repository.recarregarBancoDoZeroDoFirebase()
                    userPreferencesRepository.atualizarVersaoBancoLocal(versaoRemota)
                    recarregar()
                    _mensagemAlerta.value = "✅ Banco de dados atualizado para a versão v$versaoRemota ($qtd músicas)!"
                } else {
                    val qtdFirebase = repository.sincronizarComFirebase()
                    recarregar()
                    _mensagemAlerta.value = "Sincronização concluída: $qtdFirebase música(s) atualizada(s)!"
                }
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro na sincronização Firebase: ${e.localizedMessage}"
            }
        }
    }

    fun migrarParaFirebase() {
        viewModelScope.launch {
            try {
                _mensagemAlerta.value = "Iniciando migração para o Firebase..."
                val novaVersao = userPreferencesRepository.incrementarVersaoBancoLocal()
                val qtdMigrados = repository.migrarBancoParaFirebase()
                repository.publicarVersaoBancoRemoto(novaVersao)
                _mensagemAlerta.value = "Migração concluída com sucesso (v$novaVersao)! $qtdMigrados música(s) enviada(s)."
            } catch (e: Exception) {
                _mensagemAlerta.value = "Erro ao migrar para Firebase: ${e.localizedMessage}"
            }
        }
    }

    fun limparAlerta() {
        _mensagemAlerta.value = null
    }
}

class MainViewModelFactory(
    private val repository: MusicaRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val escalaCultoDao: EscalaCultoDao? = null,
    private val escalaMusicoDataDao: EscalaMusicoDataDao? = null,
    private val ensaioDao: EnsaioDao? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, userPreferencesRepository, escalaCultoDao, escalaMusicoDataDao, ensaioDao) as T
    }
}
