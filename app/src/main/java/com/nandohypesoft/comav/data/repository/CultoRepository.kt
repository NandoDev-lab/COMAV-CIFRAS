package com.nandohypesoft.comav.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nandohypesoft.comav.data.database.EscalaCultoDao
import com.nandohypesoft.comav.data.database.EscalaCultoEntity
import com.nandohypesoft.comav.model.EscalaCulto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CultoRepository(private val escalaCultoDao: EscalaCultoDao? = null) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarEscala(escala: EscalaCulto): String = withContext(Dispatchers.IO) {
        val idFinal = escala.id.ifEmpty { java.util.UUID.randomUUID().toString() }
        val escalaComId = escala.copy(id = idFinal)

        // 1. Salvar localmente no Room (BD local)
        escalaCultoDao?.salvar(EscalaCultoEntity.fromEscalaCulto(escalaComId))

        // 2. Sincronizar com Firebase se online
        try {
            firestore?.collection("escala_cultos")?.document(idFinal)?.set(escalaComId)?.await()
        } catch (_: Exception) {}

        idFinal
    }

    suspend fun listarEscalas(): List<EscalaCulto> = withContext(Dispatchers.IO) {
        val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hojeStr = formatoData.format(Date())

        // Limpar escalas passadas do BD local (já passaram da data do culto)
        escalaCultoDao?.excluirPassadas(hojeStr)

        // Tentar buscar do Firebase e atualizar local, se falhar ou estiver offline, usa o Room local
        try {
            val snapshot = firestore?.collection("escala_cultos")?.get()?.await()
            if (snapshot != null) {
                val remoteList = snapshot.toObjects(EscalaCulto::class.java)
                remoteList.forEach { esc ->
                    escalaCultoDao?.salvar(EscalaCultoEntity.fromEscalaCulto(esc))
                }
            }
        } catch (_: Exception) {}

        val entidades = escalaCultoDao?.obterTodas() ?: emptyList()
        entidades.map { it.toEscalaCulto() }
    }

    suspend fun excluirEscala(id: String) = withContext(Dispatchers.IO) {
        if (id.isNotEmpty()) {
            escalaCultoDao?.excluir(id)
            try {
                firestore?.collection("escala_cultos")?.document(id)?.delete()?.await()
            } catch (_: Exception) {}
        }
    }
}
