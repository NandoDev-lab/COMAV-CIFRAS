package com.nandohypesoft.comav.ui

object QuebraAutomatica {

    fun quebraInteligente(conteudo: String, maxChars: Int = 42): List<String> {
        val linhasBrutas = conteudo.split("\n")
        val linhasFormatadas = mutableListOf<String>()
        var linhaContador = 1

        var i = 0
        while (i < linhasBrutas.size) {
            val linhaCifra = linhasBrutas[i]
            val linhaLetra = if (i + 1 < linhasBrutas.size) {
                linhasBrutas[i + 1]
            } else {
                ""
            }

            // Se for cabeçalho ou seção (ex: [Refrão], [Intro]), mantém sem quebra rígida C(n)/L(n)
            if (linhaCifra.trim().startsWith("[") && linhaCifra.trim().endsWith("]")) {
                linhasFormatadas.add(linhaCifra)
                if (linhaLetra.isNotBlank() && !linhaLetra.trim().startsWith("[")) {
                    linhasFormatadas.add(linhaLetra)
                    i++
                }
                linhaContador++
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
                        linhasFormatadas.add("C($linhaContador): $subCifra")
                        linhasFormatadas.add("L($linhaContador): $subLetra")
                        linhaContador++
                    }
                    currentIndex = breakIdx
                }
                i += 2
            } else {
                if (linhaCifra.isNotBlank()) {
                    linhasFormatadas.add("C($linhaContador): $linhaCifra")
                }
                if (linhaLetra.isNotBlank()) {
                    linhasFormatadas.add("L($linhaContador): $linhaLetra")
                }
                linhaContador++
                i += 2
            }
        }

        return linhasFormatadas
    }
}
