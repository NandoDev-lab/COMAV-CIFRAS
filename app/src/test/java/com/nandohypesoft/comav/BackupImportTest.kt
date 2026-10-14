package com.nandohypesoft.comav

import com.google.gson.Gson
import com.nandohypesoft.comav.model.BackupMusicas
import com.nandohypesoft.comav.model.TipoDeConteudo
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BackupImportTest {

    @Test
    fun testarSincronizacaoBackupGist() {
        val jsonFile = File("C:/Users/FERNANDO/Desktop/Projetos Pessoais/CIFRAS COMAV/letras_e_cifras_backup.json")
        if (!jsonFile.exists()) return

        val jsonTexto = jsonFile.readText(Charsets.UTF_8)
        val gson = Gson()
        val backup = gson.fromJson(jsonTexto, BackupMusicas::class.java)

        assertNotNull(backup)
        assertEquals("COMAV_BACKUP", backup.formato)
        assertEquals(1, backup.versao)
        assertNotNull(backup.musicas)

        val musicas = backup.musicas
        assertEquals(183, musicas.size)

        val letras = musicas.filter { it.tipoDeConteudo == TipoDeConteudo.LETRA }
        val cifras = musicas.filter { it.tipoDeConteudo == TipoDeConteudo.CIFRA }

        assertEquals(89, letras.size)
        assertEquals(94, cifras.size)

        // Verificar que titulos identicos compartilham o mesmo numero de busca (identificador)
        val numerosDeBuscaUnicos = musicas.map { it.identificador }.toSet()
        assertEquals(94, numerosDeBuscaUnicos.size)

        // Verificar que Acalma o meu Coracao em Letra e Cifra compartilham o mesmo numero (ex: 1)
        val acalmaLetra = letras.find { it.titulo.contains("Acalma o meu Coração", ignoreCase = true) }
        val acalmaCifra = cifras.find { it.titulo.contains("Acalma o meu Coração", ignoreCase = true) }

        assertNotNull(acalmaLetra)
        assertNotNull(acalmaCifra)
        assertEquals(acalmaLetra!!.identificador, acalmaCifra!!.identificador)
    }
}
