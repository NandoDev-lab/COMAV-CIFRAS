package com.nandohypesoft.comav.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nandohypesoft.comav.model.EscalaMusicoData

@Entity(tableName = "escala_musicos_datas")
data class EscalaMusicoDataEntity(
    @PrimaryKey
    val dataIso: String,
    val nomeMinistro: String = "",
    val vocal1: String = "",
    val vocal2: String = "",
    val vocal3: String = "",
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = "",
    val baixista: String = ""
) {
    fun toEscalaMusicoData(): EscalaMusicoData {
        return EscalaMusicoData(
            dataIso = dataIso,
            nomeMinistro = nomeMinistro,
            vocal1 = vocal1,
            vocal2 = vocal2,
            vocal3 = vocal3,
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
                vocal1 = data.vocal1,
                vocal2 = data.vocal2,
                vocal3 = data.vocal3,
                tecladista = data.tecladista,
                violonista = data.violonista,
                baterista = data.baterista,
                baixista = data.baixista
            )
        }
    }
}
