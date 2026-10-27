package com.nandohypesoft.comav.ui.culto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandohypesoft.comav.model.EscalaCulto
import com.nandohypesoft.comav.model.ItemEscala
import com.nandohypesoft.comav.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ItemEscalaFormState(
    musicaInicial: String = "",
    cantorInicial: String = "",
    identificadorInicial: Long = 0L,
    val ehExtra: Boolean = false
) {
    var musica by mutableStateOf(musicaInicial)
    var cantor by mutableStateOf(cantorInicial)
    var identificador by mutableStateOf(identificadorInicial)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorMusicoSpinner(
    label: String,
    icone: String,
    musicoSelecionado: String,
    opcoesMusicos: List<String>,
    onMusicoSelecionado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var mostrandoOutroCampo by remember { mutableStateOf(false) }
    var valorPersonalizado by remember { mutableStateOf("") }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$icone $label",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (musicoSelecionado.isBlank()) "Selecione o músico..." else musicoSelecionado,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                opcoesMusicos.forEach { nome ->
                    DropdownMenuItem(
                        text = { Text(nome, fontSize = 14.sp) },
                        onClick = {
                            if (nome == "Outro...") {
                                mostrandoOutroCampo = true
                            } else {
                                mostrandoOutroCampo = false
                                onMusicoSelecionado(if (nome.startsWith("Selecione") || nome.startsWith("Nenhum")) "" else nome)
                            }
                            expanded = false
                        }
                    )
                }
            }
        }

        if (mostrandoOutroCampo) {
            OutlinedTextField(
                value = valorPersonalizado,
                onValueChange = {
                    valorPersonalizado = it
                    onMusicoSelecionado(it)
                },
                placeholder = { Text("Digite o nome do músico...") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroCultoScreen(viewModel: MainViewModel, onVoltar: () -> Unit) {
    val musicas by viewModel.todasMusicas.collectAsState()
    val escalaEmEdicao by viewModel.escalaEmEdicao.collectAsState()

    var nomeMinistro by remember { mutableStateOf(escalaEmEdicao?.nomeMinistro ?: "") }
    
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val storageDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    val initialDateStr = remember {
        if (escalaEmEdicao != null && escalaEmEdicao!!.dataCulto.isNotBlank()) {
            try {
                val d = storageDateFormat.parse(escalaEmEdicao!!.dataCulto)
                if (d != null) displayDateFormat.format(d) else escalaEmEdicao!!.dataCulto
            } catch (_: Exception) {
                escalaEmEdicao!!.dataCulto
            }
        } else {
            displayDateFormat.format(Date())
        }
    }

    var dataCultoBr by remember { mutableStateOf(initialDateStr) }
    var diaSemana by remember { mutableStateOf(escalaEmEdicao?.diaSemana?.takeIf { it.isNotBlank() } ?: "Domingo") }

    var tecladista by remember { mutableStateOf(escalaEmEdicao?.tecladista ?: "") }
    var violonista by remember { mutableStateOf(escalaEmEdicao?.violonista ?: "") }
    var baterista by remember { mutableStateOf(escalaEmEdicao?.baterista ?: "") }

    val dataStorageAtual = remember(dataCultoBr) {
        try {
            val parsedDate = displayDateFormat.parse(dataCultoBr.trim())
            if (parsedDate != null) storageDateFormat.format(parsedDate) else dataCultoBr.trim()
        } catch (_: Exception) {
            dataCultoBr.trim()
        }
    }

    val escalaMusicoDataAutobuscada = remember(dataStorageAtual) {
        viewModel.obterEscalaMusicoPorDataSync(dataStorageAtual)
    }

    LaunchedEffect(escalaMusicoDataAutobuscada) {
        escalaMusicoDataAutobuscada?.let { musicoData ->
            if (tecladista.isBlank() && musicoData.tecladista.isNotBlank()) {
                tecladista = musicoData.tecladista
            }
            if (violonista.isBlank() && musicoData.violonista.isNotBlank()) {
                violonista = musicoData.violonista
            }
            if (baterista.isBlank() && musicoData.baterista.isNotBlank()) {
                baterista = musicoData.baterista
            }
        }
    }

    val equipeCantores = listOf("Pastora Val", "Maisa", "Isabele", "Daniel", "Fernando", "Nanda", "Fabiana")

    val itensForm = remember {
        val list = mutableStateListOf<ItemEscalaFormState>()
        if (escalaEmEdicao != null && escalaEmEdicao!!.itens.isNotEmpty()) {
            escalaEmEdicao!!.itens.forEach { item ->
                list.add(
                    ItemEscalaFormState(
                        musicaInicial = item.tituloMusica,
                        cantorInicial = item.cantor,
                        identificadorInicial = item.identificadorMusica,
                        ehExtra = item.ehExtra
                    )
                )
            }
        } else {
            list.add(ItemEscalaFormState(ehExtra = false))
            list.add(ItemEscalaFormState(ehExtra = false))
            list.add(ItemEscalaFormState(ehExtra = false))
        }
        list
    }

    var dialogMusicaIndex by remember { mutableStateOf<Int?>(null) }
    var dialogCantorIndex by remember { mutableStateOf<Int?>(null) }
    var pesquisaMusicaDialog by remember { mutableStateOf("") }

    // Diálogo de Seleção de Música
    dialogMusicaIndex?.let { index ->
        val itemAtual = itensForm[index]
        val musicasFiltradas = if (pesquisaMusicaDialog.isBlank()) {
            musicas
        } else {
            musicas.filter { it.titulo.contains(pesquisaMusicaDialog, ignoreCase = true) || it.identificador.toString() == pesquisaMusicaDialog.removePrefix("#") }
        }

        AlertDialog(
            onDismissRequest = { 
                dialogMusicaIndex = null
                pesquisaMusicaDialog = ""
            },
            title = { Text("Selecionar Música ${index + 1}") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pesquisaMusicaDialog,
                        onValueChange = { pesquisaMusicaDialog = it },
                        placeholder = { Text("Pesquisar por nome ou nº (#12)...") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (musicasFiltradas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhuma música encontrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(musicasFiltradas) { m ->
                                Surface(
                                    onClick = {
                                        itemAtual.musica = m.titulo
                                        itemAtual.identificador = m.identificador
                                        dialogMusicaIndex = null
                                        pesquisaMusicaDialog = ""
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "Nº ${m.identificador}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = m.titulo,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    dialogMusicaIndex = null
                    pesquisaMusicaDialog = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Seleção de Cantor
    dialogCantorIndex?.let { index ->
        val itemAtual = itensForm[index]

        AlertDialog(
            onDismissRequest = { dialogCantorIndex = null },
            title = { Text("Selecionar Solista / Cantor") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    equipeCantores.forEach { cantorNome ->
                        Surface(
                            onClick = {
                                itemAtual.cantor = cantorNome
                                dialogCantorIndex = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = cantorNome,
                                modifier = Modifier.padding(14.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogCantorIndex = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (escalaEmEdicao != null) "Editar Louvores do Culto" else "Cadastrar Louvores do Culto") },
                navigationIcon = {
                    TextButton(onClick = onVoltar) {
                        Text("← Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Informações do Culto",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nomeMinistro,
                onValueChange = { nomeMinistro = it },
                label = { Text("Nome do Ministro / Dirigente") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dataCultoBr,
                onValueChange = { dataCultoBr = it },
                label = { Text("Data do Culto (DD/MM/AAAA)") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = diaSemana,
                onValueChange = { diaSemana = it },
                label = { Text("Dia da Semana (ex: Domingo, Quarta)") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Seção Somente Leitura da Escala de Músicos
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎸 Equipe de Instrumentistas (Consulta da Data)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Os músicos são definidos no Calendário pelo Administrador:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val tecladistaExibicao = escalaMusicoDataAutobuscada?.tecladista?.takeIf { it.isNotBlank() }
                        ?: tecladista.takeIf { it.isNotBlank() } ?: "Pendente no calendário"
                    val violonistaExibicao = escalaMusicoDataAutobuscada?.violonista?.takeIf { it.isNotBlank() }
                        ?: violonista.takeIf { it.isNotBlank() } ?: "Pendente no calendário"
                    val bateristaExibicao = escalaMusicoDataAutobuscada?.baterista?.takeIf { it.isNotBlank() }
                        ?: baterista.takeIf { it.isNotBlank() } ?: "Pendente no calendário"

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎹 Teclado:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            text = tecladistaExibicao,
                            fontSize = 14.sp,
                            fontWeight = if (tecladistaExibicao != "Pendente no calendário") FontWeight.Bold else FontWeight.Normal,
                            color = if (tecladistaExibicao != "Pendente no calendário") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎸 Violão:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            text = violonistaExibicao,
                            fontSize = 14.sp,
                            fontWeight = if (violonistaExibicao != "Pendente no calendário") FontWeight.Bold else FontWeight.Normal,
                            color = if (violonistaExibicao != "Pendente no calendário") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🥁 Bateria:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            text = bateristaExibicao,
                            fontSize = 14.sp,
                            fontWeight = if (bateristaExibicao != "Pendente no calendário") FontWeight.Bold else FontWeight.Normal,
                            color = if (bateristaExibicao != "Pendente no calendário") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "Repertório do Culto",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Renderizar itens do repertório
            itensForm.forEachIndexed { index, itemState ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (itemState.ehExtra) "Música Extra" else "Música ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        OutlinedCard(
                            onClick = { dialogMusicaIndex = index },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (itemState.identificador > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "Nº ${itemState.identificador}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = itemState.musica.ifBlank { "Selecione a música..." },
                                        color = if (itemState.musica.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = if (itemState.musica.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        OutlinedCard(
                            onClick = { dialogCantorIndex = index },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = itemState.cantor.ifBlank { "Quem vai cantar / Solista..." },
                                    color = if (itemState.cantor.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        val ehOficial = index < 3
                        if (!ehOficial) {
                            Spacer(modifier = Modifier.height(2.dp))
                            TextButton(
                                onClick = { itensForm.removeAt(index) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Remover extra", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    itensForm.add(ItemEscalaFormState(ehExtra = true))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("+ Adicionar Música Extra (Ceia / Consagração)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nomeMinistro.isBlank() || dataCultoBr.isBlank()) {
                        return@Button
                    }

                    val dataStorage = try {
                        val parsedDate = displayDateFormat.parse(dataCultoBr.trim())
                        if (parsedDate != null) storageDateFormat.format(parsedDate) else dataCultoBr.trim()
                    } catch (_: Exception) {
                        dataCultoBr.trim()
                    }

                    val itens = itensForm
                        .filter { it.musica.isNotBlank() }
                        .map { ItemEscala(tituloMusica = it.musica, cantor = it.cantor, ehExtra = it.ehExtra, identificadorMusica = it.identificador) }

                    val musicoData = viewModel.obterEscalaMusicoPorDataSync(dataStorage)
                    val tecladistaFinal = tecladista.trim().ifBlank { musicoData?.tecladista ?: "" }
                    val violonistaFinal = violonista.trim().ifBlank { musicoData?.violonista ?: "" }
                    val bateristaFinal = baterista.trim().ifBlank { musicoData?.baterista ?: "" }

                    val novaEscala = EscalaCulto(
                        nomeMinistro = nomeMinistro.trim(),
                        dataCulto = dataStorage,
                        diaSemana = diaSemana.trim(),
                        itens = itens,
                        tecladista = tecladistaFinal,
                        violonista = violonistaFinal,
                        baterista = bateristaFinal
                    )

                    viewModel.salvarEscalaCulto(novaEscala) {
                        onVoltar()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar e Sincronizar (Firebase)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
