package com.nandohypesoft.comav.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EnsaioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(ensaio: EnsaioEntity)

    @Query("SELECT * FROM ensaios ORDER BY dataEnsaio ASC")
    suspend fun obterTodos(): List<EnsaioEntity>

    @Query("DELETE FROM ensaios WHERE dataEnsaio < :hojeStr")
    suspend fun excluirPassados(hojeStr: String)

    @Query("DELETE FROM ensaios WHERE id = :id")
    suspend fun excluir(id: String)
}
