package com.nandohypesoft.comav.ui

data class BlocoCifraLetra(
    val indice: Int,
    val cifra: String,
    val letra: String,
    val isHeader: Boolean = false
)

object QuebraAutomatica {

    enum class TipoLinha {
        ACORDE,
        LETRA,
        SECAO,
        VAZIA
    }

    private fun detectarTipo(linha: String): TipoLinha {
        val t = linha.trim()
        if (t.isEmpty()) return TipoLinha.VAZIA
        if ((t.startsWith("[") && t.endsWith("]")) ||
            t.contains("Intro", true) || 
            t.contains("Refrão", true) || 
            t.contains("Verso", true) || 
            t.contains("Ponte", true) || 
            t.contains("Solo", true) || 
            t.contains("Final", true) ||
            t.contains("1º vez", true) || 
            t.contains("2º vez", true) ||
            t.endsWith(":")) {
            return TipoLinha.SECAO
        }

        val tokens = t.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return TipoLinha.VAZIA

        val regexAcorde = Regex("^[A-G][#b]?(?:maj|min|dim|aug|sus2|sus4|sus|add9|m|7|4|6)?\\d*(?:/[A-G][#b]?)?$")
        val countAcordes = tokens.count { token ->
            regexAcorde.matches(token.replace(Regex("[()\\[\\].,;:!?|]"), ""))
        }

        return if ((countAcordes.toFloat() / tokens.size) >= 0.4f) {
            TipoLinha.ACORDE
        } else {
            TipoLinha.LETRA
        }
    }

    fun quebraInteligente(conteudo: String, maxChars: Int = 35): List<BlocoCifraLetra> {
        val linhasBrutas = conteudo.split("\n")
        
        data class RawPair(val cifra: String, val letra: String, val isHeader: Boolean)
        val rawPairs = mutableListOf<RawPair>()

        var i = 0
        while (i < linhasBrutas.size) {
            val linha = linhasBrutas[i]
            val tipo = detectarTipo(linha)

            if (tipo == TipoLinha.SECAO) {
                rawPairs.add(RawPair(linha, "", true))
                i++
            } else if (tipo == TipoLinha.VAZIA) {
                rawPairs.add(RawPair("", "", false))
                i++
            } else if (tipo == TipoLinha.ACORDE) {
                val proximaLinha = if (i + 1 < linhasBrutas.size) linhasBrutas[i + 1] else ""
                val tipoProxima = if (proximaLinha.isNotBlank()) detectarTipo(proximaLinha) else TipoLinha.VAZIA

                if (tipoProxima == TipoLinha.LETRA) {
                    rawPairs.add(RawPair(linha, proximaLinha, false))
                    i += 2
                } else {
                    rawPairs.add(RawPair(linha, "", false))
                    i++
                }
            } else {
                rawPairs.add(RawPair("", linha, false))
                i++
            }
        }

        val blocos = mutableListOf<BlocoCifraLetra>()
        var currentIdx = 1

        for (pair in rawPairs) {
            if (pair.isHeader) {
                blocos.add(BlocoCifraLetra(currentIdx, pair.cifra, "", true))
                currentIdx += 2
                continue
            }

            if (pair.cifra.isBlank() && pair.letra.isBlank()) {
                blocos.add(BlocoCifraLetra(currentIdx, "", ""))
                currentIdx += 2
                continue
            }

            val maxLen = maxOf(pair.cifra.length, pair.letra.length)
            if (maxLen > maxChars) {
                val chunksCifra = mutableListOf<String>()
                val chunksLetra = mutableListOf<String>()

                val baseText = if (pair.letra.isNotBlank()) pair.letra else pair.cifra
                var currentIndex = 0
                while (currentIndex < maxLen) {
                    val endIdx = minOf(currentIndex + maxChars, maxLen)
                    var breakIdx = endIdx
                    if (endIdx < maxLen) {
                        val lastSpace = baseText.lastIndexOf(' ', endIdx)
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

                for (chunkIdx in chunksCifra.indices) {
                    val cText = chunksCifra[chunkIdx]
                    val lText = if (chunkIdx < chunksLetra.size) chunksLetra[chunkIdx] else ""
                    if (cText.isNotBlank() || lText.isNotBlank()) {
                        blocos.add(BlocoCifraLetra(currentIdx, cText, lText))
                        currentIdx += 2
                    }
                }
            } else {
                blocos.add(BlocoCifraLetra(currentIdx, pair.cifra, pair.letra))
                currentIdx += 2
            }
        }

        return blocos
    }
}
