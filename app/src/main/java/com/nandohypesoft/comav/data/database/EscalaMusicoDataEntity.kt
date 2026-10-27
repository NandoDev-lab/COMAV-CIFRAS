package com.nandohypesoft.comav.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nandohypesoft.comav.model.EscalaMusicoData

@Entity(tableName = "escala_musicos_datas")
data class EscalaMusicoDataEntity(
    @PrimaryKey
    val dataIso: String,
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = ""
) {
    fun toEscalaMusicoData(): EscalaMusicoData {
        return EscalaMusicoData(
            dataIso = dataIso,
            tecladista = tecladista,
            violonista = violonista,
            baterista = baterista
        )
    }

    companion object {
        fun fromEscalaMusicoData(data: EscalaMusicoData): EscalaMusicoDataEntity {
            return EscalaMusicoDataEntity(
                dataIso = data.dataIso,
                tecladista = data.tecladista,
                violonista = data.violonista,
                baterista = data.baterista
            )
        }
    }
}
