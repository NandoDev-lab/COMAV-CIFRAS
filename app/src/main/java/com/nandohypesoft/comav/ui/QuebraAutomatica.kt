package com.nandohypesoft.comav.ui

object QuebraAutomatica {

    fun quebraInteligente(conteudo: String, maxChars: Int = 42): List<String> {
        val linhasBrutas = conteudo.split("\n")
        
        data class RawPair(val cifra: String, val letra: String, val isHeader: Boolean)
        val rawPairs = mutableListOf<RawPair>()

        var i = 0
        while (i < linhasBrutas.size) {
            val linhaCifra = linhasBrutas[i]
            val linhaLetra = if (i + 1 < linhasBrutas.size) linhasBrutas[i + 1] else ""

            if (linhaCifra.trim().startsWith("[") && linhaCifra.trim().endsWith("]")) {
                rawPairs.add(RawPair(linhaCifra, "", true))
                i++
            } else {
                rawPairs.add(RawPair(linhaCifra, linhaLetra, false))
                i += 2
            }
        }

        val linhasFormatadas = mutableListOf<String>()
        var currentIdx = 1

        for (pair in rawPairs) {
            if (pair.isHeader) {
                linhasFormatadas.add(pair.cifra)
                currentIdx += 2
                continue
            }

            val maxLen = maxOf(pair.cifra.length, pair.letra.length)
            if (maxLen > maxChars) {
                val chunksCifra = mutableListOf<String>()
                val chunksLetra = mutableListOf<String>()

                var currentIndex = 0
                while (currentIndex < maxLen) {
                    val endIdx = minOf(currentIndex + maxChars, maxLen)
                    var breakIdx = endIdx
                    if (endIdx < maxLen) {
                        val lastSpace = pair.letra.lastIndexOf(' ', endIdx)
                        if (lastSpace > currentIndex) {
                            breakIdx = lastSpace + 1
                        }
                    }

                    val subCifra = if (currentIndex < pair.cifra.length) pair.cifra.substring(currentIndex, minOf(breakIdx, pair.cifra.length)) else ""
                    val subLetra = if (currentIndex < pair.letra.length) pair.letra.substring(currentIndex, minOf(breakIdx, pair.letra.length)) else ""

                    chunksCifra.add(subCifra)
                    chunksLetra.add(subLetra)
                    currentIndex = breakIdx
                }

                // Atribuir índices saltando de 2 em 2: C1/L1, C3/L3, C5/L5...
                for (chunkIdx in chunksCifra.indices) {
                    val cText = chunksCifra[chunkIdx]
                    val lText = if (chunkIdx < chunksLetra.size) chunksLetra[chunkIdx] else ""
                    if (cText.isNotBlank() || lText.isNotBlank()) {
                        linhasFormatadas.add("C($currentIdx): $cText")
                        linhasFormatadas.add("L($currentIdx): $lText")
                        currentIdx += 2
                    }
                }
            } else {
                if (pair.cifra.isNotBlank()) {
                    linhasFormatadas.add("C($currentIdx): ${pair.cifra}")
                }
                if (pair.letra.isNotBlank()) {
                    linhasFormatadas.add("L($currentIdx): ${pair.letra}")
                }
                currentIdx += 2
            }
        }

        return linhasFormatadas
    }
}
