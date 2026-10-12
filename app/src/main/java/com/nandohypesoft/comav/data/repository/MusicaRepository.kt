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
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val consultaNormalizada = "%${normalizarTitulo(if (consultaLimpa.isEmpty()) consulta else consultaLimpa)}%"

        val entidades = musicaDao.buscarMusicas(
            tipoDeConteudo = modo.name,
            consultaNormalizada = consultaNormalizada,
            consultaLimpa = "%$consultaLimpa%"
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
            normalizarTitulo(musica.titulo).contains(normalizarTitulo(consultaLimpa)) ||
                    correspondeAConsultaDeCifra(musica.conteudo, consultaLimpa)
        }
    }

    suspend fun salvarMusica(musicaNova: MusicaNova, identificador: Long? = null) {
        val agora = System.currentTimeMillis()
        val tituloLimpo = musicaNova.titulo.trim()
        val tituloNorm = normalizarTitulo(tituloLimpo)

        val entity = MusicaEntity(
            identificador = identificador ?: 0,
            titulo = tituloLimpo,
            tituloNormalizado = tituloNorm,
            conteudo = musicaNova.conteudo,
            conteudoOriginal = musicaNova.conteudoOriginal,
            tipoDeConteudo = musicaNova.tipoDeConteudo.name,
            tipoDeOrigem = musicaNova.tipoDeOrigem.name,
            nomeArquivoOrigem = musicaNova.nomeDoArquivoDeOrigem,
            criadaEm = seNaoDefinidoCriadaEm(identificador, agora),
            atualizadaEm = agora
        )

        musicaDao.salvar(entity)
    }

    private suspend fun seNaoDefinidoCriadaEm(identificador: Long?, fallback: Long): Long {
        if (identificador == null || identificador == 0L) return fallback
        // Em atualizacao, mantem o timestamp existente se achar
        val todas = musicaDao.obterTodasParaBackup()
        return todas.find { it.identificador == identificador }?.criadaEm ?: fallback
    }

    suspend fun excluirMusica(identificador: Long) {
        musicaDao.excluir(identificador)
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

        val entidades = backup.musicas.map { musica ->
            MusicaEntity.fromMusica(
                musica = musica,
                tituloNormalizado = normalizarTitulo(musica.titulo)
            )
        }

        musicaDao.salvarTodas(entidades)
        return entidades.size
    }
}
