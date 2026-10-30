package com.nandohypesoft.comav.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nandohypesoft.comav.data.database.EnsaioDao
import com.nandohypesoft.comav.data.database.EnsaioEntity
import com.nandohypesoft.comav.model.Ensaio
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EnsaioRepository(
    private val ensaioDao: EnsaioDao? = null
) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarEnsaio(ensaio: Ensaio): String = withContext(Dispatchers.IO) {
        val idFinal = ensaio.id.ifEmpty { java.util.UUID.randomUUID().toString() }
        val ensaioComId = ensaio.copy(id = idFinal)

        ensaioDao?.salvar(EnsaioEntity.fromEnsaio(ensaioComId))

        try {
            val dadosEnsaio = mapOf(
                "id" to ensaioComId.id,
                "dataEnsaio" to ensaioComId.dataEnsaio,
                "diaSemana" to ensaioComId.diaSemana,
                "linkYoutube1" to ensaioComId.linkYoutube1,
                "linkYoutube2" to ensaioComId.linkYoutube2,
                "status" to ensaioComId.status,
                "criadaEm" to ensaioComId.criadaEm
            )
            firestore?.collection("ensaios")?.document(idFinal)?.set(dadosEnsaio)?.await()
        } catch (e: Exception) {
            val msgErro = e.localizedMessage ?: e.message ?: ""
            if (msgErro.contains("PERMISSION_DENIED", ignoreCase = true)) {
                throw IllegalArgumentException("Permissão negada no Firebase (PERMISSION_DENIED). Altere as Regras no Console do Firebase.")
            }
        }

        idFinal
    }

    suspend fun atualizarStatusEnsaio(id: String, novoStatus: String) = withContext(Dispatchers.IO) {
        val locais = ensaioDao?.obterTodos() ?: emptyList()
        val local = locais.find { it.id == id }
        if (local != null) {
            val atualizado = local.copy(status = novoStatus)
            ensaioDao?.salvar(atualizado)
        }

        try {
            firestore?.collection("ensaios")?.document(id)?.update("status", novoStatus)?.await()
        } catch (_: Exception) {}
    }

    suspend fun listarEnsaios(): List<Ensaio> = withContext(Dispatchers.IO) {
        val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hojeStr = formatoData.format(Date())

        ensaioDao?.excluirPassados(hojeStr)

        try {
            val snapshot = firestore?.collection("ensaios")?.get()?.await()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    try {
                        if (!doc.exists() || doc.data.isNullOrEmpty()) continue
                        val id = doc.getString("id") ?: continue
                        val dataEnsaio = doc.getString("dataEnsaio") ?: ""
                        val diaSemana = doc.getString("diaSemana") ?: ""
                        val link1 = doc.getString("linkYoutube1") ?: ""
                        val link2 = doc.getString("linkYoutube2") ?: ""
                        val status = doc.getString("status") ?: "AGENDADO"
                        val criadaEm = doc.getLong("criadaEm") ?: System.currentTimeMillis()

                        val e = Ensaio(
                            id = id,
                            dataEnsaio = dataEnsaio,
                            diaSemana = diaSemana,
                            linkYoutube1 = link1,
                            linkYoutube2 = link2,
                            status = status,
                            criadaEm = criadaEm
                        )
                        ensaioDao?.salvar(EnsaioEntity.fromEnsaio(e))
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}

        val entidades = ensaioDao?.obterTodos() ?: emptyList()
        entidades.map { it.toEnsaio() }
    }

    suspend fun excluirEnsaio(id: String) = withContext(Dispatchers.IO) {
        if (id.isNotEmpty()) {
            ensaioDao?.excluir(id)
            try {
                firestore?.collection("ensaios")?.document(id)?.delete()?.await()
            } catch (_: Exception) {}
        }
    }
}
