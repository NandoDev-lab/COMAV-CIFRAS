package com.nandohypesoft.comav.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.nandohypesoft.comav.model.VersaoApp
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object UpdateManager {

    suspend fun verificarNovaVersaoRemota(versionCodeAtual: Long): VersaoApp? = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val doc = firestore.collection("configuracao_app").document("versao_apk").get().await()

            if (doc != null && doc.exists()) {
                val versionCodeRemoto = doc.getLong("versionCode") ?: 1L
                val versionName = doc.getString("versionName") ?: "v1.0"
                val urlApk = doc.getString("urlApk") ?: ""
                val obrigatoria = doc.getBoolean("obrigatoria") ?: false
                val notas = doc.getString("notasAtualizacao") ?: ""
                val publicadoEm = doc.getString("publicadoEm") ?: ""

                if (versionCodeRemoto > versionCodeAtual && urlApk.isNotBlank()) {
                    return@withContext VersaoApp(
                        versionCode = versionCodeRemoto,
                        versionName = versionName,
                        urlApk = urlApk,
                        obrigatoria = obrigatoria,
                        notasAtualizacao = notas,
                        publicadoEm = publicadoEm
                    )
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun publicarNovaVersaoApp(versao: VersaoApp) = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val dados = mapOf(
                "versionCode" to versao.versionCode,
                "versionName" to versao.versionName,
                "urlApk" to versao.urlApk.trim(),
                "obrigatoria" to versao.obrigatoria,
                "notasAtualizacao" to versao.notasAtualizacao.trim(),
                "publicadoEm" to versao.publicadoEm
            )
            firestore.collection("configuracao_app").document("versao_apk").set(dados).await()
        } catch (e: Exception) {
            throw IllegalArgumentException("Falha ao publicar versão do APK no Firebase: ${e.localizedMessage}")
        }
    }

    suspend fun baixarEInstalarApk(
        context: Context,
        urlApk: String,
        onProgresso: (porcentagem: Int) -> Unit,
        onSucesso: (File) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlApk)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalArgumentException("Erro de servidor HTTP ao baixar APK: ${connection.responseCode}")
            }

            val fileLength = connection.contentLength
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val apkFile = File(downloadDir, "comav_update.apk")

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val data = ByteArray(4096)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count.toLong()
                        if (fileLength > 0) {
                            val pct = ((total * 100) / fileLength).toInt()
                            withContext(Dispatchers.Main) { onProgresso(pct) }
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onSucesso(apkFile)
                instalarApk(context, apkFile)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onError(e) }
        }
    }

    fun instalarApk(context: Context, apkFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
