package com.nandohypesoft.comav.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nandohypesoft.comav.model.EscalaCulto
import com.nandohypesoft.comav.model.ItemEscala
import java.util.UUID

@Entity(tableName = "escala_cultos")
data class EscalaCultoEntity(
    @PrimaryKey
    val id: String,
    val nomeMinistro: String,
    val dataCulto: String,
    val diaSemana: String,
    val itensJson: String,
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = "",
    val criadaEm: Long
) {
    fun toEscalaCulto(gson: Gson = Gson()): EscalaCulto {
        val type = object : TypeToken<List<ItemEscala>>() {}.type
        val itensList: List<ItemEscala> = try { gson.fromJson(itensJson, type) } catch (_: Exception) { emptyList() }
        return EscalaCulto(
            id = id,
            nomeMinistro = nomeMinistro,
            dataCulto = dataCulto,
            diaSemana = diaSemana,
            itens = itensList,
            tecladista = tecladista,
            violonista = violonista,
            baterista = baterista,
            criadaEm = criadaEm
        )
    }

    companion object {
        fun fromEscalaCulto(escala: EscalaCulto, gson: Gson = Gson()): EscalaCultoEntity {
            return EscalaCultoEntity(
                id = escala.id.ifEmpty { UUID.randomUUID().toString() },
                nomeMinistro = escala.nomeMinistro,
                dataCulto = escala.dataCulto,
                diaSemana = escala.diaSemana,
                itensJson = gson.toJson(escala.itens),
                tecladista = escala.tecladista,
                violonista = escala.violonista,
                baterista = escala.baterista,
                criadaEm = escala.criadaEm
            )
        }
    }
}
