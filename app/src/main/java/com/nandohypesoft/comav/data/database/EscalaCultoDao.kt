package com.nandohypesoft.comav.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EscalaCultoDao {
    @Query("SELECT * FROM escala_cultos ORDER BY dataCulto ASC")
    suspend fun obterTodas(): List<EscalaCultoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(entity: EscalaCultoEntity)

    @Query("DELETE FROM escala_cultos WHERE dataCulto < :hojeStr")
    suspend fun excluirPassadas(hojeStr: String): Int

    @Query("DELETE FROM escala_cultos WHERE id = :id")
    suspend fun excluir(id: String): Int
}
