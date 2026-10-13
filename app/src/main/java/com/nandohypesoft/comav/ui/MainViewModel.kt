package com.nandohypesoft.comav.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nandohypesoft.comav.data.importation.DocumentExtractor
import com.nandohypesoft.comav.data.preferences.UserPreferencesRepository
import com.nandohypesoft.comav.data.repository.MusicaRepository
import com.nandohypesoft.comav.model.ModoDeBusca
import com.nandohypesoft.comav.model.Musica
import com.nandohypesoft.comav.model.MusicaNova
import com.nandohypesoft.comav.model.TipoDeConteudo
import com.nandohypesoft.comav.model.TipoDeOrigem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val temaEscuro: StateFlow<Boolean> = userPreferencesRepository.temaEscuro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val escalaFonte: StateFlow<Float> = userPreferencesRepository.escalaFonte
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    private val _musicas = MutableStateFlow<List<Musica>>(emptyList())
    val musicas: StateFlow<List<Musica>> = _musicas.asStateFlow()

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

    init {
        recarregar()
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
        val r = _rascunho.value
        if (r.titulo.trim().isEmpty() || r.conteudo.trim().isEmpty()) {
            _mensagemAlerta.value = "Informe o título e o conteúdo da música."
            return
        }

        viewModelScope.launch {
            repository.salvarMusica(r, _musicaAberta.value?.identificador)
            _editorVisivel.value = false
            recarregar()
            onSucesso()
        }
    }

    fun importarDocumento(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val doc = DocumentExtractor.extrairDocumento(context, uri)
                val texto = normalizarQuebrasDeLinha(doc.texto)
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

    fun excluirMusica(identificador: Long) {
        viewModelScope.launch {
            repository.excluirMusica(identificador)
            recarregar()
        }
    }

    fun alterarTema(ativo: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.salvarTemaEscuro(ativo)
        }
    }

    fun alterarEscala(novaEscala: Float) {
        val escalaClamp = novaEscala.coerceIn(0.8f, 1.5f)
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

    fun importarBackupTexto(jsonTexto: String) {
        viewModelScope.launch {
            try {
                val qtd = repository.importarBanco(jsonTexto)
                recarregar()
                _mensagemAlerta.value = "Backup restaurado via QR Code: $qtd música(s) adicionada(s)."
            } catch (e: Exception) {
                _mensagemAlerta.value = "QR Code inválido ou não é um backup COMAV: ${e.localizedMessage}"
            }
        }
    }

    fun limparAlerta() {
        _mensagemAlerta.value = null
    }
}

class MainViewModelFactory(
    private val repository: MusicaRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, userPreferencesRepository) as T
    }
}
