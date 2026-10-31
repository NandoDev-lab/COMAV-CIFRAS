package com.nandohypesoft.comav.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nandohypesoft.comav.model.TipoFuncaoEquipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracoes_comav")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val TEMA_ESCURO = booleanPreferencesKey("tema_escuro")
        private val ESCALA_FONTE = floatPreferencesKey("escala_fonte")
    // Substituído removendo referências ao Gist
        private val MOSTRAR_ESTATISTICAS = booleanPreferencesKey("mostrar_estatisticas")
        private val MOSTRAR_PAINEL_CONECTADOS = booleanPreferencesKey("mostrar_painel_conectados")
        private val TIPO_FUNCAO_EQUIPE = stringPreferencesKey("tipo_funcao_equipe")
        private val NOME_USUARIO = stringPreferencesKey("nome_usuario")
        private val VERSAO_BANCO_LOCAL = androidx.datastore.preferences.core.longPreferencesKey("versao_banco_local")
    }

    val nomeUsuario: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NOME_USUARIO] ?: ""
    }

    val tipoFuncaoEquipe: Flow<TipoFuncaoEquipe> = context.dataStore.data.map { preferences ->
        val nome = preferences[TIPO_FUNCAO_EQUIPE] ?: TipoFuncaoEquipe.NAO_DEFINIDO.name
        try {
            TipoFuncaoEquipe.valueOf(nome)
        } catch (_: Exception) {
            TipoFuncaoEquipe.NAO_DEFINIDO
        }
    }

    val temaEscuro: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TEMA_ESCURO] ?: false
    }

    val escalaFonte: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[ESCALA_FONTE] ?: 1.0f
    }

// Url gist removida

    val mostrarEstatisticas: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MOSTRAR_ESTATISTICAS] ?: false
    }

    val mostrarPainelConectados: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MOSTRAR_PAINEL_CONECTADOS] ?: true
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

// salvarUrlGist removida

    suspend fun salvarMostrarEstatisticas(ativo: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MOSTRAR_ESTATISTICAS] = ativo
        }
    }

    suspend fun salvarMostrarPainelConectados(ativo: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MOSTRAR_PAINEL_CONECTADOS] = ativo
        }
    }

    suspend fun salvarTipoFuncaoEquipe(tipo: TipoFuncaoEquipe) {
        context.dataStore.edit { preferences ->
            preferences[TIPO_FUNCAO_EQUIPE] = tipo.name
        }
    }

    val versaoBancoLocal: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[VERSAO_BANCO_LOCAL] ?: 1L
    }

    suspend fun incrementarVersaoBancoLocal(): Long {
        var novaVersao = 1L
        context.dataStore.edit { preferences ->
            val atual = preferences[VERSAO_BANCO_LOCAL] ?: 1L
            novaVersao = atual + 1L
            preferences[VERSAO_BANCO_LOCAL] = novaVersao
        }
        return novaVersao
    }

    suspend fun atualizarVersaoBancoLocal(novaVersao: Long) {
        context.dataStore.edit { preferences ->
            val atual = preferences[VERSAO_BANCO_LOCAL] ?: 1L
            preferences[VERSAO_BANCO_LOCAL] = maxOf(atual, novaVersao)
        }
    }

    suspend fun salvarNomeUsuario(nome: String) {
        context.dataStore.edit { preferences ->
            preferences[NOME_USUARIO] = nome.trim().uppercase()
        }
    }
}
