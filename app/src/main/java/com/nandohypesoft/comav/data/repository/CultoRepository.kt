package com.nandohypesoft.comav.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nandohypesoft.comav.data.database.EscalaCultoDao
import com.nandohypesoft.comav.data.database.EscalaCultoEntity
import com.nandohypesoft.comav.data.database.EscalaMusicoDataDao
import com.nandohypesoft.comav.data.database.EscalaMusicoDataEntity
import com.nandohypesoft.comav.model.EscalaCulto
import com.nandohypesoft.comav.model.EscalaMusicoData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CultoRepository(
    private val escalaCultoDao: EscalaCultoDao? = null,
    private val escalaMusicoDataDao: EscalaMusicoDataDao? = null
) {
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
            val itensList = escalaComId.itens.map { item ->
                mapOf(
                    "tituloMusica" to item.tituloMusica,
                    "cantor" to item.cantor,
                    "ehExtra" to item.ehExtra,
                    "identificadorMusica" to item.identificadorMusica
                )
            }
            val dadosEscala = mapOf(
                "id" to escalaComId.id,
                "nomeMinistro" to escalaComId.nomeMinistro,
                "dataCulto" to escalaComId.dataCulto,
                "diaSemana" to escalaComId.diaSemana,
                "vocal1" to escalaComId.vocal1,
                "vocal2" to escalaComId.vocal2,
                "vocal3" to escalaComId.vocal3,
                "vocais" to escalaComId.vocais,
                "tecladista" to escalaComId.tecladista,
                "violonista" to escalaComId.violonista,
                "baterista" to escalaComId.baterista,
                "baixista" to escalaComId.baixista,
                "itens" to itensList,
                "statusPreenchimento" to escalaComId.statusPreenchimento,
                "criadaEm" to escalaComId.criadaEm,
                "preenchidaEm" to escalaComId.preenchidaEm
            )
            firestore?.collection("escala_cultos")?.document(idFinal)?.set(dadosEscala)?.await()
        } catch (e: Exception) {
            val msgErro = e.localizedMessage ?: e.message ?: ""
            if (msgErro.contains("PERMISSION_DENIED", ignoreCase = true)) {
                throw IllegalArgumentException("Permissão negada no Firebase (PERMISSION_DENIED). Altere as Regras de Segurança no Console do Firebase (Firestore Database -> Regras) para: allow read, write: if true;")
            }
        }

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
                for (doc in snapshot.documents) {
                    try {
                        if (!doc.exists() || doc.data.isNullOrEmpty()) continue
                        val id = doc.getString("id") ?: continue
                        val nomeMinistro = doc.getString("nomeMinistro") ?: ""
                        val dataCulto = doc.getString("dataCulto") ?: ""
                        val diaSemana = doc.getString("diaSemana") ?: ""
                        val vocal1 = doc.getString("vocal1") ?: ""
                        val vocal2 = doc.getString("vocal2") ?: ""
                        val vocal3 = doc.getString("vocal3") ?: ""
                        val vocaisRaw = doc.getString("vocais") ?: ""
                        val tecladista = doc.getString("tecladista") ?: ""
                        val violonista = doc.getString("violonista") ?: ""
                        val baterista = doc.getString("baterista") ?: ""
                        val baixista = doc.getString("baixista") ?: ""
                        val statusPreenchimento = doc.getString("statusPreenchimento") ?: "PENDENTE_PREENCHIMENTO"
                        val criadaEm = doc.getLong("criadaEm") ?: System.currentTimeMillis()
                        val preenchidaEm = doc.getLong("preenchidaEm") ?: 0L

                        // Se vocal1 estiver vazio mas vocaisRaw possuir nomes separados por virgula
                        val partesVocais = if (vocaisRaw.isNotBlank() && vocal1.isBlank()) vocaisRaw.split(",").map { it.trim() } else emptyList()
                        val v1Final = if (vocal1.isNotBlank()) vocal1 else partesVocais.getOrNull(0) ?: ""
                        val v2Final = if (vocal2.isNotBlank()) vocal2 else partesVocais.getOrNull(1) ?: ""
                        val v3Final = if (vocal3.isNotBlank()) vocal3 else partesVocais.getOrNull(2) ?: ""

                        val itensRaw = doc.get("itens") as? List<Map<String, Any>> ?: emptyList()
                        val itensConvertidos = itensRaw.map { mapItem ->
                            com.nandohypesoft.comav.model.ItemEscala(
                                tituloMusica = mapItem["tituloMusica"] as? String ?: "",
                                cantor = mapItem["cantor"] as? String ?: "",
                                ehExtra = mapItem["ehExtra"] as? Boolean ?: false,
                                identificadorMusica = (mapItem["identificadorMusica"] as? Number)?.toLong() ?: 0L
                            )
                        }

                        val escala = EscalaCulto(
                            id = id,
                            nomeMinistro = nomeMinistro,
                            dataCulto = dataCulto,
                            diaSemana = diaSemana,
                            vocal1 = v1Final,
                            vocal2 = v2Final,
                            vocal3 = v3Final,
                            tecladista = tecladista,
                            violonista = violonista,
                            baterista = baterista,
                            baixista = baixista,
                            itens = itensConvertidos,
                            statusPreenchimento = statusPreenchimento,
                            criadaEm = criadaEm,
                            preenchidaEm = preenchidaEm
                        )
                        escalaCultoDao?.salvar(EscalaCultoEntity.fromEscalaCulto(escala))
                    } catch (_: Exception) {}
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

    // --- MÚSICOS POR DATA (CALENDÁRIO) ---

    suspend fun salvarEscalaMusicoData(escalaMusico: EscalaMusicoData) = withContext(Dispatchers.IO) {
        if (escalaMusico.dataIso.isBlank()) return@withContext
        escalaMusicoDataDao?.salvar(EscalaMusicoDataEntity.fromEscalaMusicoData(escalaMusico))
        try {
            firestore?.collection("escala_musicos_datas")
                ?.document(escalaMusico.dataIso)
                ?.set(escalaMusico)
                ?.await()
        } catch (e: Exception) {
            val msgErro = e.localizedMessage ?: e.message ?: ""
            if (msgErro.contains("PERMISSION_DENIED", ignoreCase = true)) {
                throw IllegalArgumentException("Permissão negada no Firebase (PERMISSION_DENIED). Altere as Regras de Segurança no Console do Firebase (Firestore Database -> Regras) para: allow read, write: if true;")
            }
        }
    }

    suspend fun obterEscalaMusicoPorData(dataIso: String): EscalaMusicoData? = withContext(Dispatchers.IO) {
        if (dataIso.isBlank()) return@withContext null
        
        // 1. Tentar Firebase para obter os dados mais recentes da nuvem
        try {
            val doc = firestore?.collection("escala_musicos_datas")?.document(dataIso)?.get()?.await()
            if (doc != null && doc.exists()) {
                val remote = doc.toObject(EscalaMusicoData::class.java)
                if (remote != null) {
                    escalaMusicoDataDao?.salvar(EscalaMusicoDataEntity.fromEscalaMusicoData(remote))
                    return@withContext remote
                }
            }
        } catch (_: Exception) {}

        // 2. Se offline ou falhar, fallback para o BD local Room
        val local = escalaMusicoDataDao?.obterPorData(dataIso)?.toEscalaMusicoData()
        local
    }

    suspend fun listarTodasEscalasMusicosData(): List<EscalaMusicoData> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore?.collection("escala_musicos_datas")?.get()?.await()
            if (snapshot != null) {
                val remoteList = snapshot.toObjects(EscalaMusicoData::class.java)
                remoteList.forEach { item ->
                    escalaMusicoDataDao?.salvar(EscalaMusicoDataEntity.fromEscalaMusicoData(item))
                }
            }
        } catch (_: Exception) {}

        val locais = escalaMusicoDataDao?.obterTodas() ?: emptyList()
        locais.map { it.toEscalaMusicoData() }
    }
}
