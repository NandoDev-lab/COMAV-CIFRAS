package com.nandohypesoft.comav.data.importation

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.nandohypesoft.comav.model.TipoDeOrigem
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import java.util.zip.ZipInputStream

data class DocumentoExtraido(
    val tituloSugerido: String,
    val texto: String,
    val tipoDeOrigem: TipoDeOrigem,
    val nomeDoArquivo: String
)

object DocumentExtractor {

    private fun decodificarEntidades(texto: String): String {
        return texto
            .replace(Regex("&#(\\d+);")) { match ->
                val code = match.groupValues[1].toIntOrNull()
                if (code != null) String(Character.toChars(code)) else match.value
            }
            .replace(Regex("&#x([\\da-fA-F]+);")) { match ->
                val code = match.groupValues[1].toIntOrNull(16)
                if (code != null) String(Character.toChars(code)) else match.value
            }
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }

    private fun extrairTextoDocx(inputStream: InputStream): String {
        val zip = ZipInputStream(inputStream)
        var xml: String? = null
        var entry = zip.nextEntry
        while (entry != null) {
            if (entry.name == "word/document.xml") {
                xml = zip.bufferedReader().readText()
                break
            }
            entry = zip.nextEntry
        }
        zip.close()

        if (xml == null) {
            throw IllegalArgumentException("O arquivo DOCX não contém um documento válido.")
        }

        val texto = xml
            .replace(Regex("<w:p(?:\\s[^>]*)?>"), "\n")
            .replace(Regex("<w:(?:tab|tabs)\\s*/?>"), "\t")
            .replace(Regex("<w:(?:br|cr)(?:\\s[^>]*)?\\s*/?>"), "\n")
            .replace(Regex("<w:t(?:\\s[^>]*)?>([\\s\\S]*?)</w:t>")) { match -> match.groupValues[1] }
            .replace(Regex("<[^>]+>"), "")

        return decodificarEntidades(texto)
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun extrairTextoPdf(context: Context, inputStream: InputStream): String {
        try {
            PDFBoxResourceLoader.init(context)
            PDDocument.load(inputStream).use { document ->
                val stripper = PDFTextStripper()
                return stripper.getText(document).trim()
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Erro ao ler o PDF: ${e.localizedMessage}")
        }
    }

    fun extrairDocumento(context: Context, uri: Uri): DocumentoExtraido {
        val nomeArquivo = obterNomeArquivo(context, uri) ?: "documento"
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val ehPdf = mimeType.contains("pdf", ignoreCase = true) || nomeArquivo.endsWith(".pdf", ignoreCase = true)

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Não foi possível abrir o arquivo selecionado.")

        val texto = if (ehPdf) {
            extrairTextoPdf(context, inputStream)
        } else {
            extrairTextoDocx(inputStream)
        }

        if (texto.isBlank()) {
            throw IllegalArgumentException("Não foi encontrado texto no arquivo. PDFs escaneados (imagens) exigem OCR.")
        }

        val tituloSugerido = nomeArquivo.replace(Regex("\\.(docx|pdf)$", RegexOption.IGNORE_CASE), "")

        return DocumentoExtraido(
            tituloSugerido = tituloSugerido,
            texto = texto,
            tipoDeOrigem = if (ehPdf) TipoDeOrigem.PDF else TipoDeOrigem.DOCX,
            nomeDoArquivo = nomeArquivo
        )
    }

    private fun obterNomeArquivo(context: Context, uri: Uri): String? {
        var nome: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    nome = cursor.getString(index)
                }
            }
        }
        return nome
    }
}
