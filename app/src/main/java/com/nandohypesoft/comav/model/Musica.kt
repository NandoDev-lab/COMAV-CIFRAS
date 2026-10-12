package com.nandohypesoft.comav.model

enum class TipoDeConteudo {
    CIFRA,
    LETRA
}

enum class TipoDeOrigem {
    MANUAL,
    DOCX,
    PDF
}

enum class ModoDeBusca {
    CIFRA,
    LETRA
}

data class Musica(
    val identificador: Long,
    val titulo: String,
    val conteudo: String,
    val conteudoOriginal: String,
    val tipoDeConteudo: TipoDeConteudo,
    val tipoDeOrigem: TipoDeOrigem,
    val nomeDoArquivoDeOrigem: String?,
    val criadaEm: Long,
    val atualizadaEm: Long
)

data class MusicaNova(
    val titulo: String,
    val conteudo: String,
    val conteudoOriginal: String,
    val tipoDeConteudo: TipoDeConteudo,
    val tipoDeOrigem: TipoDeOrigem,
    val nomeDoArquivoDeOrigem: String?
)

data class BackupMusicas(
    val formato: String = "COMAV_BACKUP",
    val versao: Int = 1,
    val exportadoEm: String,
    val musicas: List<Musica>
)
