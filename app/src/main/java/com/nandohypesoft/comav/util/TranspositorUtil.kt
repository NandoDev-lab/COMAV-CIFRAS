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
    private val REGEX_ACORDE_TOKEN = Regex("^[A-G][#b]?(?:maj|min|dim|aug|sus2|sus4|sus|add9|m|7|4|6|9|11|13|5\\+|7\\+)?(?:\\([#b]?\\d+\\))?(?:/[A-G][#b]?)?$", RegexOption.IGNORE_CASE)

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
     * Transpõe uma linha de cifra mantendo os alinhamentos e espaços em branco.
     */
    fun transporLinha(linha: String, semitons: Int): String {
        if (semitons == 0 || linha.isBlank()) return linha

        // Se for cabeçalho de seção ex: [Intro], [Refrão], retorna sem alterações
        val limpa = linha.trim()
        if ((limpa.startsWith("[") && limpa.endsWith("]")) || limpa.endsWith(":")) {
            return linha
        }

        val sb = StringBuilder()
        var i = 0
        val n = linha.length

        while (i < n) {
            if (linha[i].isWhitespace()) {
                sb.append(linha[i])
                i++
            } else {
                // Encontrar a palavra/token
                val start = i
                while (i < n && !linha[i].isWhitespace()) {
                    i++
                }
                val token = linha.substring(start, i)
                val tokenSemPontuacao = token.replace(Regex("[()\\[\\].,;:!?|]"), "")

                if (ehAcorde(tokenSemPontuacao)) {
                    // Preservar pontuações em volta do acorde se houver (ex: "(Am7)")
                    val prefixo = token.takeWhile { !it.isLetter() }
                    val sufixo = token.takeLastWhile { !it.isLetter() && !it.isDigit() && it != '#' && it != 'b' && it != 'B' && it != '/' }
                    val corpoAcorde = token.substring(prefixo.length, token.length - sufixo.length)

                    if (corpoAcorde.isNotBlank() && ehAcorde(corpoAcorde)) {
                        val acordeTransposto = transporAcordeToken(corpoAcorde, semitons)
                        sb.append(prefixo).append(acordeTransposto).append(sufixo)
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
