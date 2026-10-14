package com.nandohypesoft.comav.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracoes_comav")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val TEMA_ESCURO = booleanPreferencesKey("tema_escuro")
        private val ESCALA_FONTE = floatPreferencesKey("escala_fonte")
        private val URL_GIST_REMOTA = stringPreferencesKey("url_gist_remota")
    }

    val temaEscuro: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TEMA_ESCURO] ?: false
    }

    val escalaFonte: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[ESCALA_FONTE] ?: 1.0f
    }

    val urlGistRemota: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[URL_GIST_REMOTA] ?: "https://gist.githubusercontent.com/NandoDev-lab/c1cc5216eebad1cecf47d04adfc4d41b/raw/c5adc1ee2232062f0e5a190b4391fcc60b74e65e/comav_banco.json"
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

    suspend fun salvarUrlGistRemota(url: String) {
        context.dataStore.edit { preferences ->
            preferences[URL_GIST_REMOTA] = url.trim()
        }
    }
}
