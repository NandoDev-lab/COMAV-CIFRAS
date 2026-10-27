package com.nandohypesoft.comav.data.repository

import com.google.gson.Gson
import com.nandohypesoft.comav.data.database.MusicaDao
import com.nandohypesoft.comav.data.database.MusicaEntity
import com.nandohypesoft.comav.model.BackupMusicas
import com.nandohypesoft.comav.model.ModoDeBusca
import com.nandohypesoft.comav.model.Musica
import com.nandohypesoft.comav.model.MusicaNova
import com.nandohypesoft.comav.model.TipoDeConteudo
import com.nandohypesoft.comav.model.TipoDeOrigem
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicaRepository(
    private val musicaDao: MusicaDao,
    private val gson: Gson = Gson()
) {

    fun normalizarTitulo(titulo: String): String {
        val semAcento = Normalizer.normalize(titulo, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return semAcento.lowercase().trim()
    }

    private fun limparTokenDeAcorde(token: String): String {
        return token.replace(Regex("[()\\[\\]\\.,;:!?|]"), "")
    }

    private fun ehNomeDeAcorde(texto: String): Boolean {
        val regex = Regex("^[A-G](?:#|b)?(?:maj|min|dim|aug|sus2|sus4|sus|add9|m)?\\d*(?:\\/[A-G](?:#|b)?)?$")
        return regex.matches(texto)
    }

    private fun extrairAcordesDeCifra(conteudo: String): Set<String> {
        val acordes = mutableSetOf<String>()
        for (linha in conteudo.split(Regex("\\r?\\n"))) {
            val tokens = linha.trim()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .map { limparTokenDeAcorde(it) }

            if (tokens.isNotEmpty() && tokens.all { ehNomeDeAcorde(it) }) {
                acordes.addAll(tokens)
            }
        }
        return acordes
    }

    private fun correspondeAConsultaDeCifra(conteudo: String, consulta: String): Boolean {
        val acordesConsultados = consulta.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { limparTokenDeAcorde(it) }

        if (acordesConsultados.isEmpty() || !acordesConsultados.all { ehNomeDeAcorde(it) }) {
            return false
        }

        val acordesDaMusica = extrairAcordesDeCifra(conteudo)
        return acordesConsultados.all { acordesDaMusica.contains(it) }
    }

    suspend fun listarMusicas(consulta: String = "", modo: ModoDeBusca = ModoDeBusca.LETRA): List<Musica> {
        val consultaLimpa = consulta.trim()
        val termoSemHash = consultaLimpa.removePrefix("#").trim()
        val consultaNormalizada = "%${normalizarTitulo(if (termoSemHash.isEmpty()) consulta else termoSemHash)}%"

        val entidades = musicaDao.buscarMusicas(
            tipoDeConteudo = modo.name,
            consultaNormalizada = consultaNormalizada,
            consultaLimpa = "%$termoSemHash%"
        )

        val musicas = entidades.map { it.toMusica() }

        if (modo != ModoDeBusca.CIFRA || consultaLimpa.isEmpty()) {
            return musicas
        }

        val consultaTemApenasAcordes = consultaLimpa.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .all { ehNomeDeAcorde(limparTokenDeAcorde(it)) }

        if (!consultaTemApenasAcordes) {
            return musicas
        }

        return musicas.filter { musica ->
            musica.identificador.toString() == termoSemHash ||
            normalizarTitulo(musica.titulo).contains(normalizarTitulo(consultaLimpa)) ||
            correspondeAConsultaDeCifra(musica.conteudo, consultaLimpa)
        }
    }

    suspend fun salvarMusica(musicaNova: MusicaNova, identificador: Long? = null) {
        val agora = System.currentTimeMillis()
        val tituloLimpo = musicaNova.titulo.trim()
        val tituloNorm = normalizarTitulo(tituloLimpo)

        val todas = musicaDao.obterTodasParaBackup()

        val idFinal = if (identificador != null && identificador > 0L) {
            identificador
        } else {
            val existenteMesmoTitulo = todas.find { it.tituloNormalizado == tituloNorm }
            existenteMesmoTitulo?.identificador ?: ((todas.maxOfOrNull { it.identificador } ?: 0L) + 1L)
        }

        val entity = MusicaEntity(
            identificador = idFinal,
            titulo = tituloLimpo,
            tituloNormalizado = tituloNorm,
            conteudo = musicaNova.conteudo,
            conteudoOriginal = musicaNova.conteudoOriginal,
            tipoDeConteudo = musicaNova.tipoDeConteudo.name,
            tipoDeOrigem = musicaNova.tipoDeOrigem.name,
            nomeArquivoOrigem = musicaNova.nomeDoArquivoDeOrigem,
            criadaEm = seNaoDefinidoCriadaEm(todas, idFinal, musicaNova.tipoDeConteudo.name, agora),
            atualizadaEm = agora
        )

        musicaDao.salvar(entity)
    }

    private fun seNaoDefinidoCriadaEm(todas: List<MusicaEntity>, id: Long, tipo: String, fallback: Long): Long {
        return todas.find { it.identificador == id && it.tipoDeConteudo == tipo }?.criadaEm ?: fallback
    }

    suspend fun excluirMusica(identificador: Long, tipoDeConteudo: TipoDeConteudo) {
        musicaDao.excluir(identificador, tipoDeConteudo.name)
    }

    suspend fun exportarBanco(): BackupMusicas {
        val entidades = musicaDao.obterTodasParaBackup()
        val musicas = entidades.map { it.toMusica() }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        return BackupMusicas(
            exportadoEm = dateFormat.format(Date()),
            musicas = musicas
        )
    }

    suspend fun importarBanco(jsonTexto: String): Int {
        val backup = try {
            gson.fromJson(jsonTexto, BackupMusicas::class.java)
        } catch (e: Exception) {
            throw IllegalArgumentException("O arquivo selecionado não é um JSON de backup válido.")
        }

        if (backup == null || backup.formato != "COMAV_BACKUP" || backup.versao != 1 || backup.musicas == null) {
            throw IllegalArgumentException("O arquivo selecionado não é um backup COMAV válido.")
        }

        val tituloParaId = mutableMapOf<String, Long>()
        var proximoId = 1L

        backup.musicas.forEach { musica ->
            val norm = normalizarTitulo(musica.titulo)
            if (norm.isNotBlank() && musica.identificador > 0L && !tituloParaId.containsKey(norm)) {
                tituloParaId[norm] = musica.identificador
                if (musica.identificador >= proximoId) {
                    proximoId = musica.identificador + 1L
                }
            }
        }

        val locaisAtuais = musicaDao.obterTodasParaBackup().associateBy { Pair(it.identificador, it.tipoDeConteudo) }
        val entidadesParaSalvar = mutableListOf<MusicaEntity>()

        backup.musicas.forEach { musica ->
            val norm = normalizarTitulo(musica.titulo)
            val idFinal = tituloParaId.getOrPut(norm) { proximoId++ }
            val tipoStr = musica.tipoDeConteudo.name

            val chave = Pair(idFinal, tipoStr)
            val localExistente = locaisAtuais[chave]

            val atualizadaEmFinal = if (musica.atualizadaEm > 0) musica.atualizadaEm else System.currentTimeMillis()
            val criadaEmFinal = if (musica.criadaEm > 0) musica.criadaEm else System.currentTimeMillis()

            // Proteção contra sobrescrita: se a alteração local for mais recente que a do Gist remoto, preserva a local!
            if (localExistente != null && localExistente.atualizadaEm > atualizadaEmFinal) {
                entidadesParaSalvar.add(localExistente)
            } else {
                entidadesParaSalvar.add(
                    MusicaEntity(
                        identificador = idFinal,
                        titulo = musica.titulo.trim(),
                        tituloNormalizado = norm,
                        conteudo = musica.conteudo,
                        conteudoOriginal = musica.conteudoOriginal,
                        tipoDeConteudo = tipoStr,
                        tipoDeOrigem = musica.tipoDeOrigem.name,
                        nomeArquivoOrigem = musica.nomeDoArquivoDeOrigem,
                        criadaEm = if (localExistente != null) localExistente.criadaEm else criadaEmFinal,
                        atualizadaEm = atualizadaEmFinal
                    )
                )
            }
        }

        musicaDao.salvarTodas(entidadesParaSalvar)
        return entidadesParaSalvar.size
    }

    suspend fun sincronizarComGistRemoto(urlGist: String): Int = withContext(Dispatchers.IO) {
        val urlLimpa = urlGist.trim()
        if (urlLimpa.isEmpty()) {
            throw IllegalArgumentException("A URL do Gist não foi configurada.")
        }

        val jsonTexto = try {
            val url = URL(urlLimpa)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            throw IllegalArgumentException("Falha ao conectar ao Gist remoto: ${e.localizedMessage}")
        }

        importarBanco(jsonTexto)
    }
}
