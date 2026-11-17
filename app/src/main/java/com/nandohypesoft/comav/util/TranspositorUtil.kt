package com.nandohypesoft.comav.util

object TranspositorUtil {

    private val NOTAS_SUSTERIDO = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val NOTAS_BEMOL    = arrayOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    // Mapa para localizar a posição de qualquer nota (0 a 11)
    private val NOTA_PARA_INDICE = mapOf(
        "C" to 0, "B#" to 0,
        "C#" to 1, "DB" to 1, "Db" to 1,
        "D" to 2,
        "D#" to 3, "EB" to 3, "Eb" to 3,
        "E" to 4, "FB" to 4, "Fb" to 4,
        "F" to 5, "E#" to 5,
        "F#" to 6, "GB" to 6, "Gb" to 6,
        "G" to 7,
        "G#" to 8, "AB" to 8, "Ab" to 8,
        "A" to 9,
        "A#" to 10, "BB" to 10, "Bb" to 10,
        "B" to 11, "CB" to 11, "Cb" to 11
    )

    // Regex para identificar um acorde individual com nota raiz, sufixos opcionais e nota de baixo opcional
    private val REGEX_ACORDE_TOKEN = Regex("^[A-G][#b]?(?:maj|min|dim|aug|sus2|sus4|sus|add9|m)?\\d*(?:\\+[57])?(?:\\([#b]?\\d+\\))?(?:/[A-G][#b]?)?$", RegexOption.IGNORE_CASE)

    // Regex para capturar notas isoladas dentro de um acorde (ex: "F#m7/A#" -> Grupo 1: F#, Grupo 2: m7, Grupo 3: /A#)
    private val REGEX_PARSER_ACORDE = Regex("^([A-G][#b]?)(.*?)(?:/([A-G][#b]?))?$", RegexOption.IGNORE_CASE)

    /**
     * Transpõe uma nota individual (ex: "C#" -> "D" para semitons = +1).
     */
    fun transporNota(nota: String, semitons: Int, usarBemol: Boolean = false): String {
        val notaUpper = nota.trim()
        val indiceAtual = NOTA_PARA_INDICE[notaUpper] ?: return nota
        val novoIndice = ((indiceAtual + semitons) % 12 + 12) % 12
        val tabela = if (usarBemol) NOTAS_BEMOL else NOTAS_SUSTERIDO
        return tabela[novoIndice]
    }

    /**
     * Transpõe um token de acorde completo (ex: "F#m7/C#" com semitons = +1 -> "Gm7/D").
     */
    fun transporAcordeToken(acorde: String, semitons: Int): String {
        val match = REGEX_PARSER_ACORDE.find(acorde.trim()) ?: return acorde
        val (raiz, sufixo, baixo) = match.destructured

        // Nota bemol na notação musical usa 'b' minúsculo ex: Bb, Eb, Db (não confundir com a nota B maiúscula)
        val usaBemolOriginal = raiz.contains('b') || (baixo.isNotBlank() && baixo.contains('b'))
        val novaRaiz = transporNota(raiz, semitons, usaBemolOriginal)
        val novoBaixo = if (baixo.isNotBlank()) "/" + transporNota(baixo, semitons, usaBemolOriginal) else ""

        return "$novaRaiz$sufixo$novoBaixo"
    }

    /**
     * Verifica se um texto curto (token) é um acorde válido.
     */
    fun ehAcorde(token: String): Boolean {
        val limpo = token.replace(Regex("[()\\[\\].,;:!?|]"), "").trim()
        return REGEX_ACORDE_TOKEN.matches(limpo)
    }

    /**
     * Verifica se a linha é uma linha autêntica de cifras (e não uma frase de letra em português).
     */
    fun ehLinhaDeCifra(linha: String): Boolean {
        val limpa = linha.trim()
        if (limpa.isEmpty()) return false

        // Se for cabeçalho de seção ex: [Intro], [Refrão], Verso 1: não é linha de cifra pura
        if ((limpa.startsWith("[") && limpa.endsWith("]")) ||
            limpa.startsWith("Intro", ignoreCase = true) ||
            limpa.startsWith("Refrão", ignoreCase = true) ||
            limpa.startsWith("Verso", ignoreCase = true) ||
            limpa.startsWith("Ponte", ignoreCase = true) ||
            limpa.startsWith("Solo", ignoreCase = true) ||
            limpa.startsWith("Final", ignoreCase = true)) {
            return false
        }

        val tokens = limpa.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return false

        // Contar quantas palavras/tokens casam com o padrão de acordes
        val countAcordes = tokens.count { token ->
            val tSemPont = token.replace(Regex("[()\\[\\].,;:!?|]"), "")
            ehAcorde(tSemPont)
        }

        // Se houver palavras longas (> 3 letras sem ser sustenido/bemol/números/barra), é indício de texto de letra (ex: "presença", "Senhor", "glória")
        val temPalavrasLongasDeLetra = tokens.any { token ->
            val limpo = token.replace(Regex("[()\\[\\].,;:!?|]"), "")
            limpo.length >= 4 && !limpo.contains('/') && !limpo.contains('#') && !limpo.contains("maj", ignoreCase = true) && !limpo.contains("sus", ignoreCase = true) && !limpo.contains("add", ignoreCase = true) && !limpo.contains("dim", ignoreCase = true) && !limpo.contains("aug", ignoreCase = true)
        }

        if (temPalavrasLongasDeLetra) {
            return false
        }

        // Se a proporção de acordes na linha for alta (>= 50%), é uma linha de cifras!
        val proporcaoAcordes = countAcordes.toFloat() / tokens.size
        return proporcaoAcordes >= 0.5f
    }

    /**
     * Transpõe uma linha de cifra mantendo os alinhamentos de colunas e espaços em branco.
     */
    fun transporLinha(linha: String, semitons: Int): String {
        if (semitons == 0 || linha.isBlank()) return linha

        // Se for linha de letra em português, retorna o texto intacto sem alterar palavras como "Em", "A", "E"
        if (!ehLinhaDeCifra(linha)) {
            return linha
        }

        val sb = StringBuilder()
        var i = 0
        val n = linha.length
        var deltaEspacosPendentes = 0

        while (i < n) {
            if (linha[i].isWhitespace()) {
                val startSpace = i
                while (i < n && linha[i].isWhitespace()) {
                    i++
                }
                val originalSpaceCount = i - startSpace
                val novoSpaceCount = (originalSpaceCount - deltaEspacosPendentes).coerceAtLeast(1)
                deltaEspacosPendentes = 0
                for (s in 0 until novoSpaceCount) {
                    sb.append(' ')
                }
            } else {
                val startToken = i
                while (i < n && !linha[i].isWhitespace()) {
                    i++
                }
                val token = linha.substring(startToken, i)
                val tokenSemPontuacao = token.replace(Regex("[()\\[\\].,;:!?|]"), "")

                if (ehAcorde(tokenSemPontuacao)) {
                    val prefixo = token.takeWhile { !it.isLetter() }
                    val sufixo = token.takeLastWhile { !it.isLetter() && !it.isDigit() && it != '#' && it != 'b' && it != 'B' && it != '/' }
                    val corpoAcorde = token.substring(prefixo.length, token.length - sufixo.length)

                    if (corpoAcorde.isNotBlank() && ehAcorde(corpoAcorde)) {
                        val acordeTransposto = transporAcordeToken(corpoAcorde, semitons)
                        val tokenTransposto = "$prefixo$acordeTransposto$sufixo"
                        sb.append(tokenTransposto)
                        deltaEspacosPendentes += (tokenTransposto.length - token.length)
                    } else {
                        sb.append(token)
                    }
                } else {
                    sb.append(token)
                }
            }
        }

        return sb.toString()
    }

    /**
     * Transpõe o conteúdo completo de uma cifra linha por linha.
     */
    fun transporConteudo(conteudo: String, semitons: Int): String {
        if (semitons == 0 || conteudo.isBlank()) return conteudo
        return conteudo.split("\n").joinToString("\n") { linha ->
            transporLinha(linha, semitons)
        }
    }
}
