package com.nandohypesoft.comav.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nandohypesoft.comav.model.Musica
import com.nandohypesoft.comav.model.TipoDeConteudo
import com.nandohypesoft.comav.model.TipoDeOrigem

@Entity(tableName = "musicas")
data class MusicaEntity(
    @PrimaryKey(autoGenerate = true)
    val identificador: Long = 0,
    val titulo: String,
    @ColumnInfo(name = "titulo_normalizado")
    val tituloNormalizado: String,
    val conteudo: String,
    @ColumnInfo(name = "conteudo_original")
    val conteudoOriginal: String,
    @ColumnInfo(name = "tipo_de_conteudo")
    val tipoDeConteudo: String,
    @ColumnInfo(name = "tipo_de_origem")
    val tipoDeOrigem: String,
    @ColumnInfo(name = "nome_arquivo_origem")
    val nomeArquivoOrigem: String?,
    @ColumnInfo(name = "criada_em")
    val criadaEm: Long,
    @ColumnInfo(name = "atualizada_em")
    val atualizadaEm: Long
) {
    fun toMusica(): Musica {
        return Musica(
            identificador = identificador,
            titulo = titulo,
            conteudo = conteudo,
            conteudoOriginal = conteudoOriginal,
            tipoDeConteudo = try { TipoDeConteudo.valueOf(tipoDeConteudo) } catch (_: Exception) { TipoDeConteudo.LETRA },
            tipoDeOrigem = try { TipoDeOrigem.valueOf(tipoDeOrigem) } catch (_: Exception) { TipoDeOrigem.MANUAL },
            nomeDoArquivoDeOrigem = nomeArquivoOrigem,
            criadaEm = criadaEm,
            atualizadaEm = atualizadaEm
        )
    }

    companion object {
        fun fromMusica(musica: Musica, tituloNormalizado: String): MusicaEntity {
            return MusicaEntity(
                identificador = musica.identificador,
                titulo = musica.titulo,
                tituloNormalizado = tituloNormalizado,
                conteudo = musica.conteudo,
                conteudoOriginal = musica.conteudoOriginal,
                tipoDeConteudo = musica.tipoDeConteudo.name,
                tipoDeOrigem = musica.tipoDeOrigem.name,
                nomeArquivoOrigem = musica.nomeDoArquivoDeOrigem,
                criadaEm = musica.criadaEm,
                atualizadaEm = musica.atualizadaEm
            )
        }
    }
}
