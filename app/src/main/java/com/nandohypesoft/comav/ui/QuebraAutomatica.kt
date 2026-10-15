package com.nandohypesoft.comav.ui

data class BlocoIndexado(
    val indice: Int,
    val cifra: String,
    val letra: String
)

object QuebraAutomatica {

    fun quebraInteligente(conteudo: String, maxChars: Int = 42): List<String> {
        val linhasBrutas = conteudo.split("\n")
        val linhasFormatadas = mutableListOf<String>()
        var n = 1

        var i = 0
        while (i < linhasBrutas.size) {
            val linhaCifra = linhasBrutas[i]
            val linhaLetra = if (i + 1 < linhasBrutas.size) {
                linhasBrutas[i + 1]
            } else {
                ""
            }

            // Se for cabeçalho ou seção (ex: [Refrão], [Intro]), mantém sem quebra C(n)/L(n) rígida
            if (linhaCifra.trim().startsWith("[") && linhaCifra.trim().endsWith("]")) {
                linhasFormatadas.add(linhaCifra)
                if (linhaLetra.isNotBlank() && !linhaLetra.trim().startsWith("[")) {
                    linhasFormatadas.add(linhaLetra)
                    i++
                }
                n += 2
                i++
                continue
            }

            val maxLen = maxOf(linhaCifra.length, linhaLetra.length)
            if (maxLen > maxChars) {
                var currentIndex = 0
                while (currentIndex < maxLen) {
                    val endIdx = minOf(currentIndex + maxChars, maxLen)
                    
                    // Encontrar o último espaço na linha da letra para não cortar a palavra no meio
                    var breakIdx = endIdx
                    if (endIdx < maxLen) {
                        val lastSpace = linhaLetra.lastIndexOf(' ', endIdx)
                        if (lastSpace > currentIndex) {
                            breakIdx = lastSpace + 1
                        }
                    }

                    val subCifra = if (currentIndex < linhaCifra.length) {
                        linhaCifra.substring(currentIndex, minOf(breakIdx, linhaCifra.length))
                    } else {
                        ""
                    }

                    val subLetra = if (currentIndex < linhaLetra.length) {
                        linhaLetra.substring(currentIndex, minOf(breakIdx, linhaLetra.length))
                    } else {
                        ""
                    }

                    if (subCifra.isNotBlank() || subLetra.isNotBlank()) {
                        linhasFormatadas.add("C($n): $subCifra")
                        linhasFormatadas.add("L($n): $subLetra")
                        n += 2
                    }
                    currentIndex = breakIdx
                }
                i += 2
            } else {
                if (linhaCifra.isNotBlank()) {
                    linhasFormatadas.add("C($n): $linhaCifra")
                }
                if (linhaLetra.isNotBlank()) {
                    linhasFormatadas.add("L($n): $linhaLetra")
                }
                n += 2
                i += 2
            }
        }

        return linhasFormatadas
    }
}
