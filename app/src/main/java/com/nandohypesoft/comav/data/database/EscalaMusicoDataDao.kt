package com.nandohypesoft.comav.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EscalaMusicoDataDao {
    @Query("SELECT * FROM escala_musicos_datas WHERE dataIso = :dataIso LIMIT 1")
    suspend fun obterPorData(dataIso: String): EscalaMusicoDataEntity?

    @Query("SELECT * FROM escala_musicos_datas")
    suspend fun obterTodas(): List<EscalaMusicoDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(entidade: EscalaMusicoDataEntity)

    @Query("DELETE FROM escala_musicos_datas WHERE dataIso = :dataIso")
    suspend fun excluir(dataIso: String)
}
