package com.nandohypesoft.comav.model

enum class TipoFuncaoEquipe(val titulo: String, val descricao: String) {
    NAO_DEFINIDO("Não definido", "Selecione sua função"),
    MINISTRO_CANTOR("Ministro / Cantor", "Responsável pelo agendamento dos louvores"),
    MUSICO("Músico Instrumentista", "Executa teclado, violão ou bateria"),
    AMBOS("Ambos (Ministro e Músico)", "Atua tanto como ministro quanto como instrumentista")
}
