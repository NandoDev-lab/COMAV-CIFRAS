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
    val vocal1: String = "",
    val vocal2: String = "",
    val vocal3: String = "",
    val tecladista: String = "",
    val violonista: String = "",
    val baterista: String = "",
    val baixista: String = "",
    val itensJson: String,
    val statusPreenchimento: String = "PENDENTE_PREENCHIMENTO",
    val criadaEm: Long,
    val preenchidaEm: Long = 0L
) {
    fun toEscalaCulto(gson: Gson = Gson()): EscalaCulto {
        val type = object : TypeToken<List<ItemEscala>>() {}.type
        val itensList: List<ItemEscala> = try { gson.fromJson(itensJson, type) } catch (_: Exception) { emptyList() }
        return EscalaCulto(
            id = id,
            nomeMinistro = nomeMinistro,
            dataCulto = dataCulto,
            diaSemana = diaSemana,
            vocal1 = vocal1,
            vocal2 = vocal2,
            vocal3 = vocal3,
            tecladista = tecladista,
            violonista = violonista,
            baterista = baterista,
            baixista = baixista,
            itens = itensList,
            statusPreenchimento = statusPreenchimento,
            criadaEm = criadaEm,
            preenchidaEm = preenchidaEm
        )
    }

    companion object {
        fun fromEscalaCulto(escala: EscalaCulto, gson: Gson = Gson()): EscalaCultoEntity {
            return EscalaCultoEntity(
                id = escala.id.ifEmpty { UUID.randomUUID().toString() },
                nomeMinistro = escala.nomeMinistro,
                dataCulto = escala.dataCulto,
                diaSemana = escala.diaSemana,
                vocal1 = escala.vocal1,
                vocal2 = escala.vocal2,
                vocal3 = escala.vocal3,
                tecladista = escala.tecladista,
                violonista = escala.violonista,
                baterista = escala.baterista,
                baixista = escala.baixista,
                itensJson = gson.toJson(escala.itens),
                statusPreenchimento = escala.statusPreenchimento,
                criadaEm = escala.criadaEm,
                preenchidaEm = escala.preenchidaEm
            )
        }
    }
}
