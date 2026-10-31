package com.nandohypesoft.comav.model

data class VersaoApp(
    val versionCode: Long = 1L,
    val versionName: String = "v1.0",
    val urlApk: String = "",
    val obrigatoria: Boolean = false,
    val notasAtualizacao: String = "",
    val publicadoEm: String = ""
)
