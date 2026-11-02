package com.nandohypesoft.comav.util

import android.app.DownloadManager
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

    fun obterVersionCodeInstalado(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (_: Exception) {
            1L
        }
    }

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

    private fun abrirConexaoComRedirects(urlInitialStr: String): HttpURLConnection {
        var url = URL(urlInitialStr)
        var connection = url.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.requestMethod = "GET"

        var status = connection.responseCode
        var redirects = 0

        while ((status == HttpURLConnection.HTTP_MOVED_TEMP ||
                status == HttpURLConnection.HTTP_MOVED_PERM ||
                status == HttpURLConnection.HTTP_SEE_OTHER ||
                status == 307 || status == 308) && redirects < 5) {

            val newUrlStr = connection.getHeaderField("Location") ?: break
            connection.disconnect()

            url = URL(newUrlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.requestMethod = "GET"
            status = connection.responseCode
            redirects++
        }

        if (status != HttpURLConnection.HTTP_OK) {
            throw IllegalArgumentException("Servidor respondeu com status $status ao baixar APK.")
        }

        return connection
    }

    suspend fun baixarEInstalarApk(
        context: Context,
        urlApk: String,
        onProgresso: (porcentagem: Int) -> Unit,
        onSucesso: (File) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val connection = abrirConexaoComRedirects(urlApk)
            val fileLength = connection.contentLength

            val downloadDirApp = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val apkFileApp = File(downloadDirApp, "comav_update.apk")

            connection.inputStream.use { input ->
                FileOutputStream(apkFileApp).use { output ->
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

            // Copiar também para a pasta de Downloads pública do celular
            try {
                val publicDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (publicDownloadDir != null && publicDownloadDir.exists()) {
                    val publicApk = File(publicDownloadDir, "COMAV_Cifras_atualizacao.apk")
                    apkFileApp.copyTo(publicApk, overwrite = true)
                }
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                onSucesso(apkFileApp)
                instalarApk(context, apkFileApp)
            }
        } catch (e: Exception) {
            // Em caso de qualquer falha na conexão direta, aciona o DownloadManager nativo do Android
            try {
                enfileirarDownloadManagerNativo(context, urlApk)
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) { onError(e) }
        }
    }

    private fun enfileirarDownloadManagerNativo(context: Context, urlApk: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return
        val uri = Uri.parse(urlApk)

        val request = DownloadManager.Request(uri).apply {
            setTitle("COMAV Cifras - Nova Versão")
            setDescription("Baixando atualização do aplicativo...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "COMAV_Cifras_atualizacao.apk")
            setMimeType("application/vnd.android.package-archive")
        }

        downloadManager.enqueue(request)
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
