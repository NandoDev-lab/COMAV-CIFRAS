package com.nandohypesoft.comav.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nandohypesoft.comav.model.Ensaio

@Entity(tableName = "ensaios")
data class EnsaioEntity(
    @PrimaryKey
    val id: String,
    val dataEnsaio: String,
    val diaSemana: String,
    val linkYoutube1: String,
    val linkYoutube2: String,
    val status: String,
    val criadaEm: Long
) {
    fun toEnsaio(): Ensaio {
        return Ensaio(
            id = id,
            dataEnsaio = dataEnsaio,
            diaSemana = diaSemana,
            linkYoutube1 = linkYoutube1,
            linkYoutube2 = linkYoutube2,
            status = status,
            criadaEm = criadaEm
        )
    }

    companion object {
        fun fromEnsaio(e: Ensaio): EnsaioEntity {
            return EnsaioEntity(
                id = e.id,
                dataEnsaio = e.dataEnsaio,
                diaSemana = e.diaSemana,
                linkYoutube1 = e.linkYoutube1,
                linkYoutube2 = e.linkYoutube2,
                status = e.status,
                criadaEm = e.criadaEm
            )
        }
    }
}
