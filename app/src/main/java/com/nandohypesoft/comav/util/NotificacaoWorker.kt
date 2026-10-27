package com.nandohypesoft.comav.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nandohypesoft.comav.data.database.AppDatabase
import com.nandohypesoft.comav.data.preferences.UserPreferencesRepository
import com.nandohypesoft.comav.data.repository.CultoRepository
import kotlinx.coroutines.flow.first

class NotificacaoWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(appContext)
            val userPrefs = UserPreferencesRepository(appContext)
            val repository = CultoRepository(database.escalaCultoDao(), database.escalaMusicoDataDao())

            val funcao = userPrefs.tipoFuncaoEquipe.first()
            val escalasCulto = repository.listarEscalas()
            val escalasMusicosList = repository.listarTodasEscalasMusicosData()
            val escalasMusicosMap = escalasMusicosList.associateBy { it.dataIso }

            NotificacaoManager.verificarEAgendarAlertas(
                context = appContext,
                funcaoUsuario = funcao,
                escalasCulto = escalasCulto,
                escalasMusicosMap = escalasMusicosMap
            )

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
