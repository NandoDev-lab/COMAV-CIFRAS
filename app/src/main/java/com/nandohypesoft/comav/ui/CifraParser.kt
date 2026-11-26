package com.nandohypesoft.comav.ui

data class AcordePosicionado(
    val charIndex: Int,
    val nome: String
)

data class LinhaEstruturada(
    val id: Int,
    val letra: String,
    val acordes: List<AcordePosicionado>,
    val isHeader: Boolean = false
)

object CifraParser {

    private val regexAcordeValido = Regex("^[A-G][#b]?(?:maj|min|dim|aug|sus2|sus4|sus|add9|m|7|4|6)?\\d*(?:/[A-G][#b]?)?$")

    private fun ehAcordeValido(token: String): Boolean {
        val clean = token.replace(Regex("[()\\[\\].,;:!?|]"), "").trim()
        return regexAcordeValido.matches(clean)
    }

    fun parseCifra(conteudo: String): List<LinhaEstruturada> {
        val linhasBrutas = conteudo.split("\n")
        val resultado = mutableListOf<LinhaEstruturada>()
        var idCounter = 1

        var i = 0
        while (i < linhasBrutas.size) {
            val linhaAtual = linhasBrutas[i]
            val linhaTrim = linhaAtual.trim()

            if (linhaTrim.isEmpty()) {
                resultado.add(LinhaEstruturada(id = idCounter++, letra = "", acordes = emptyList()))
                i++
                continue
            }

            // Verifica se é cabeçalho de seção (ex: [Refrão], Intro:, etc.)
            if (isHeaderLine(linhaTrim)) {
                resultado.add(LinhaEstruturada(id = idCounter++, letra = linhaAtual, acordes = emptyList(), isHeader = true))
                i++
                continue
            }

            // Verifica se a linha atual é uma linha de cifra pura
            if (isChordLine(linhaAtual)) {
                // Olha a próxima linha para ver se é letra
                val proximaLinha = if (i + 1 < linhasBrutas.size) linhasBrutas[i + 1] else ""
                val proximaTrim = proximaLinha.trim()

                if (proximaTrim.isNotEmpty() && !isHeaderLine(proximaTrim) && !isChordLine(proximaLinha)) {
                    // Temos um par: linha de acordes (linhaAtual) + linha de letra (proximaLinha)
                    val acordes = extrairAcordesComOffset(linhaAtual)
                    resultado.add(LinhaEstruturada(id = idCounter++, letra = proximaLinha, acordes = acordes))
                    i += 2
                } else {
                    // Linha contendo apenas acordes (sem letra associada abaixo)
                    val acordes = extrairAcordesComOffset(linhaAtual)
                    resultado.add(LinhaEstruturada(id = idCounter++, letra = "", acordes = acordes))
                    i++
                }
            } else {
                // Linha de letra sem acordes acima
                resultado.add(LinhaEstruturada(id = idCounter++, letra = linhaAtual, acordes = emptyList()))
                i++
            }
        }

        return resultado
    }

    private fun isHeaderLine(t: String): Boolean {
        return (t.startsWith("[") && t.endsWith("]")) ||
            t.contains("Intro", true) ||
            t.contains("Refrão", true) ||
            t.contains("Verso", true) ||
            t.contains("Ponte", true) ||
            t.contains("Solo", true) ||
            t.contains("Final", true) ||
            t.contains("1º vez", true) ||
            t.contains("2º vez", true) ||
            t.endsWith(":")
    }

    private fun isChordLine(linha: String): Boolean {
        val t = linha.trim()
        if (t.isEmpty() || isHeaderLine(t)) return false
        val tokens = t.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return false
        val countAcordes = tokens.count { token ->
            ehAcordeValido(token)
        }
        return (countAcordes.toFloat() / tokens.size) >= 0.5f
    }

    private fun extrairAcordesComOffset(linhaAcordes: String): List<AcordePosicionado> {
        val acordes = mutableListOf<AcordePosicionado>()
        val regexAcorde = Regex("[A-G][#b]?(?:maj|min|dim|aug|sus2|sus4|sus|add9|m|7|4|6)?\\d*(?:/[A-G][#b]?)?")

        val matcher = regexAcorde.findAll(linhaAcordes)
        for (match in matcher) {
            val token = match.value
            if (ehAcordeValido(token)) {
                acordes.add(AcordePosicionado(charIndex = match.range.first, nome = token))
            }
        }
        return acordes
    }
}

fun reconstruirLinhaAcordes(linha: LinhaEstruturada): String {
    if (linha.acordes.isEmpty()) return ""
    val maxLen = maxOf(
        linha.letra.length,
        linha.acordes.maxOfOrNull { it.charIndex + it.nome.length } ?: 0
    )
    val sb = StringBuilder()
    var currentPos = 0
    for (acorde in linha.acordes.sortedBy { it.charIndex }) {
        if (acorde.charIndex > currentPos) {
            sb.append(" ".repeat(acorde.charIndex - currentPos))
            currentPos = acorde.charIndex
        }
        sb.append(acorde.nome)
        currentPos += acorde.nome.length
    }
    return sb.toString()
}
