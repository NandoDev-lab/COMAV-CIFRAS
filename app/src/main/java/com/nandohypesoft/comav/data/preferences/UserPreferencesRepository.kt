package com.nandohypesoft.comav.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracoes_comav")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val TEMA_ESCURO = booleanPreferencesKey("tema_escuro")
        private val ESCALA_FONTE = floatPreferencesKey("escala_fonte")
    }

    val temaEscuro: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TEMA_ESCURO] ?: false
    }

    val escalaFonte: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[ESCALA_FONTE] ?: 1.0f
    }

    suspend fun salvarTemaEscuro(ativo: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TEMA_ESCURO] = ativo
        }
    }

    suspend fun salvarEscalaFonte(escala: Float) {
        context.dataStore.edit { preferences ->
            preferences[ESCALA_FONTE] = escala
        }
    }
}
