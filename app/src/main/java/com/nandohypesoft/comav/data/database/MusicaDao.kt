package com.nandohypesoft.comav.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MusicaDao {

    @Query("""
        SELECT * FROM musicas 
        WHERE tipo_de_conteudo = :tipoDeConteudo 
        AND (titulo_normalizado LIKE :consultaNormalizada OR LOWER(conteudo) LIKE LOWER(:consultaLimpa)) 
        ORDER BY titulo_normalizado ASC
    """)
    suspend fun buscarMusicas(
        tipoDeConteudo: String,
        consultaNormalizada: String,
        consultaLimpa: String
    ): List<MusicaEntity>

    @Query("SELECT * FROM musicas ORDER BY titulo_normalizado ASC")
    suspend fun obterTodasParaBackup(): List<MusicaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(entity: MusicaEntity): Long

    @Query("DELETE FROM musicas WHERE identificador = :identificador")
    suspend fun excluir(identificador: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarTodas(entities: List<MusicaEntity>): List<Long>
}
