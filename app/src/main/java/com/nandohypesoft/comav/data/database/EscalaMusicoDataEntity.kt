package com.nandohypesoft.comav.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nandohypesoft.comav.model.EscalaMusicoData

@Entity(tableName = "escala_musicos_datas")
data class EscalaMusicoDataEntity(
    @PrimaryKey
    val dataIso: String,
    val nomeMinistro: String = "",
    val vocais: String = "",
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = "",
    val baixista: String = ""
) {
    fun toEscalaMusicoData(): EscalaMusicoData {
        return EscalaMusicoData(
            dataIso = dataIso,
            nomeMinistro = nomeMinistro,
            vocais = vocais,
            tecladista = tecladista,
            violonista = violonista,
            baterista = baterista,
            baixista = baixista
        )
    }

    companion object {
        fun fromEscalaMusicoData(data: EscalaMusicoData): EscalaMusicoDataEntity {
            return EscalaMusicoDataEntity(
                dataIso = data.dataIso,
                nomeMinistro = data.nomeMinistro,
                vocais = data.vocais,
                tecladista = data.tecladista,
                violonista = data.violonista,
                baterista = data.baterista,
                baixista = data.baixista
            )
        }
    }
}
