package com.nandohypesoft.comav.model

enum class TipoFuncaoEquipe(val titulo: String, val descricao: String) {
    NAO_DEFINIDO("Não definido", "Selecione sua função no grupo"),
    MINISTRO_CANTOR("Ministro", "Agendamento de cultos e visualização da lista de letras"),
    MUSICO("Músico", "Acesso às cifras, letras e escalas dos cultos"),
    ADMIN_LIDER("Líder / Desenvolvedor", "Acesso total e integral ao sistema e gestão da equipe")
}
