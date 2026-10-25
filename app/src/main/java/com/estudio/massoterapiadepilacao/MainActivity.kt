package com.estudio.massoterapiadepilacao

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

// ====================================================================================
// 1. MODELOS DE DADOS E VALORES PADRÃO DO COMÉRCIO
// ====================================================================================
enum class CategoriaServico(val titulo: String) {
    MASSOTERAPIA("Massoterapia"),
    DEPILACAO("Depilação")
}

enum class FormaPagamento(val descricao: String) {
    PIX("Pix (Instantâneo)"),
    CARTAO_CREDITO("Cartão de Crédito"),
    CARTAO_DEBITO("Cartão de Débito"),
    DINHEIRO("Dinheiro no Local")
}

enum class StatusAgendamento(val label: String, val corHex: Long) {
    PENDENTE("Aguardando Confirmação", 0xFFE65100),
    CONFIRMADO("Confirmado", 0xFF2E7D32),
    CONCLUIDO("Concluído / Pago", 0xFF1565C0),
    CANCELADO("Cancelado", 0xFFC62828),
    BLOQUEADO("Horário Bloqueado", 0xFF616161)
}

data class Servico(
    val id: String,
    val nome: String,
    val categoria: CategoriaServico,
    val duracaoMinutos: Int,
    val preco: Double,
    val descricao: String
)

data class Agendamento(
    val id: String = UUID.randomUUID().toString(),
    val clienteNome: String,
    val clienteTelefone: String,
    val servicosResumo: String,
    val observacoesCliente: String,
    val valorTotal: Double,
    val duracaoTotalMinutos: Int,
    val dataIso: String,
    val horario: String,
    val formaPagamento: String,
    val status: String = StatusAgendamento.PENDENTE.name,
    val criadoEmTimestamp: Long = System.currentTimeMillis()
) {
    val statusEnum: StatusAgendamento
        get() = StatusAgendamento.entries.find { it.name == status } ?: StatusAgendamento.PENDENTE
}

object DadosIniciaisPadrao {
    const val NOME_ESTUDIO = "Espaço Massoterapia & Depilação"
    const val WHATSAPP_PROFISSIONAL = "5544999990000"
    const val CHAVE_PIX = "contato@massoterapiadepilacao.com.br"
    const val SENHA_ADMIN = "1234"
    // Caso você crie um banco gratuito no Firebase Realtime Database, cole a URL na aba Admin > Nuvem
    // ou diretamente na constante abaixo para sincronizar entre todos os aparelhos:
    const val FIREBASE_REALTIME_DB_URL = ""

    val HORARIOS_PADRAO = listOf(
        "08:00", "09:00", "10:00", "11:00",
        "13:30", "14:30", "15:30", "16:30", "17:30", "18:30"
    )

    val CATALOGO_SERVICOS = listOf(
        Servico(
            id = "masso_relax",
            nome = "Massagem Relaxante Corporal",
            categoria = CategoriaServico.MASSOTERAPIA,
            duracaoMinutos = 60,
            preco = 120.00,
            descricao = "Alívio de tensão muscular e estresse com óleos essenciais."
        ),
        Servico(
            id = "masso_terapeutica",
            nome = "Massagem Terapêutica / Desportiva",
            categoria = CategoriaServico.MASSOTERAPIA,
            duracaoMinutos = 60,
            preco = 140.00,
            descricao = "Foco em pontos de dor, coluna, contraturas e recuperação muscular."
        ),
        Servico(
            id = "masso_drenagem",
            nome = "Drenagem Linfática Manual",
            categoria = CategoriaServico.MASSOTERAPIA,
            duracaoMinutos = 60,
            preco = 130.00,
            descricao = "Redução de retenção de líquidos e ativação da circulação."
        ),
        Servico(
            id = "depil_perna_int",
            nome = "Depilação Perna Inteira",
            categoria = CategoriaServico.DEPILACAO,
            duracaoMinutos = 45,
            preco = 75.00,
            descricao = "Depilação com cera morna hipoalergênica."
        ),
        Servico(
            id = "depil_virilha",
            nome = "Depilação Virilha Completa",
            categoria = CategoriaServico.DEPILACAO,
            duracaoMinutos = 30,
            preco = 65.00,
            descricao = "Procedimento rápido, seguro e com loção calmante pós-depilação."
        ),
        Servico(
            id = "depil_axilas",
            nome = "Depilação Axilas",
            categoria = CategoriaServico.DEPILACAO,
            duracaoMinutos = 20,
            preco = 35.00,
            descricao = "Remoção delicada com acabamento suave."
        ),
        Servico(
            id = "depil_buco",
            nome = "Depilação Facial / Buço",
            categoria = CategoriaServico.DEPILACAO,
            duracaoMinutos = 15,
            preco = 25.00,
            descricao = "Cera especial para peles sensíveis."
        )
    )
}

// ====================================================================================
// 2. GERENCIADOR DE PERSISTÊNCIA E SINCRONIZAÇÃO 100% ONLINE (REST / FIREBASE CLOUD)
// ====================================================================================
class EstadoEstudioManager(context: Context) {
    private val prefs = context.getSharedPreferences("estudio_masso_depil_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Main)

    var nomeEstudio by mutableStateOf(prefs.getString("nomeEstudio", DadosIniciaisPadrao.NOME_ESTUDIO) ?: DadosIniciaisPadrao.NOME_ESTUDIO)
    var whatsappProfissional by mutableStateOf(prefs.getString("whatsappProfissional", DadosIniciaisPadrao.WHATSAPP_PROFISSIONAL) ?: DadosIniciaisPadrao.WHATSAPP_PROFISSIONAL)
    var chavePix by mutableStateOf(prefs.getString("chavePix", DadosIniciaisPadrao.CHAVE_PIX) ?: DadosIniciaisPadrao.CHAVE_PIX)
    var senhaAdmin by mutableStateOf(prefs.getString("senhaAdmin", DadosIniciaisPadrao.SENHA_ADMIN) ?: DadosIniciaisPadrao.SENHA_ADMIN)
    var firebaseDbUrl by mutableStateOf(prefs.getString("firebaseDbUrl", DadosIniciaisPadrao.FIREBASE_REALTIME_DB_URL) ?: DadosIniciaisPadrao.FIREBASE_REALTIME_DB_URL)

    var horariosPadrao by mutableStateOf(carregarHorarios())
    var catalogoServicos by mutableStateOf(carregarServicos())
    var listaAgendamentos by mutableStateOf(carregarAgendamentos())

    var dataSelecionada by mutableStateOf(LocalDate.now().toString())
    var mensagemFeedback by mutableStateOf<String?>(null)

    init {
        iniciarSincronizacaoAutomaticaNuvem()
    }

    private fun iniciarSincronizacaoAutomaticaNuvem() {
        scope.launch {
            while (true) {
                if (firebaseDbUrl.startsWith("https://")) {
                    buscarDadosDaNuvemSilencioso()
                }
                delay(8000L) // Atualiza a agenda online a cada 8 segundos
            }
        }
    }

    fun salvarTudoESincronizar() {
        prefs.edit()
            .putString("nomeEstudio", nomeEstudio)
            .putString("whatsappProfissional", whatsappProfissional)
            .putString("chavePix", chavePix)
            .putString("senhaAdmin", senhaAdmin)
            .putString("firebaseDbUrl", firebaseDbUrl)
            .putString("horarios_json", JSONArray(horariosPadrao).toString())
            .putString("servicos_json", serializarServicos(catalogoServicos).toString())
            .putString("agendamentos_json", serializarAgendamentos(listaAgendamentos).toString())
            .apply()

        if (firebaseDbUrl.startsWith("https://")) {
            scope.launch {
                enviarDadosParaNuvem()
            }
        }
    }

    fun sincronizarManualAgora() {
        if (!firebaseDbUrl.startsWith("https://")) {
            mensagemFeedback = "Informe uma URL https:// do Firebase Realtime Database na aba Nuvem."
            return
        }
        scope.launch {
            val ok = buscarDadosDaNuvemSilencioso()
            if (ok) {
                mensagemFeedback = "Sincronizado online com a nuvem!"
            } else {
                enviarDadosParaNuvem()
                mensagemFeedback = "Dados locais enviados para a nuvem!"
            }
        }
    }

    private suspend fun enviarDadosParaNuvem(): Boolean = withContext(Dispatchers.IO) {
        try {
            val urlBase = firebaseDbUrl.trim().removeSuffix("/")
            val endpoint = URL("$urlBase/estudio_agenda.json")
            val payload = JSONObject().apply {
                put("nomeEstudio", nomeEstudio)
                put("whatsappProfissional", whatsappProfissional)
                put("chavePix", chavePix)
                put("horariosPadrao", JSONArray(horariosPadrao))
                put("servicos", serializarServicos(catalogoServicos))
                put("agendamentos", serializarAgendamentos(listaAgendamentos))
            }
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = 7000
                readTimeout = 7000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun buscarDadosDaNuvemSilencioso(): Boolean = withContext(Dispatchers.IO) {
        try {
            val urlBase = firebaseDbUrl.trim().removeSuffix("/")
            val endpoint = URL("$urlBase/estudio_agenda.json")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
            }
            if (conn.responseCode in 200..299) {
                val texto = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                conn.disconnect()
                if (texto.isNotBlank() && texto != "null") {
                    val obj = JSONObject(texto)
                    val agRemotos = obj.optJSONArray("agendamentos")?.let { desserializarAgendamentos(it) }
                    val servRemotos = obj.optJSONArray("servicos")?.let { desserializarServicos(it) }
                    val horRemotos = obj.optJSONArray("horariosPadrao")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    }
                    withContext(Dispatchers.Main) {
                        if (agRemotos != null) listaAgendamentos = agRemotos
                        if (servRemotos != null && servRemotos.isNotEmpty()) catalogoServicos = servRemotos
                        if (horRemotos != null && horRemotos.isNotEmpty()) horariosPadrao = horRemotos
                    }
                    return@withContext true
                }
            }
            conn.disconnect()
            false
        } catch (e: Exception) {
            false
        }
    }

    fun obterHorariosOcupadosNaData(dataIso: String): Set<String> {
        return listaAgendamentos
            .filter { it.dataIso == dataIso && it.statusEnum != StatusAgendamento.CANCELADO }
            .map { it.horario }
            .toSet()
    }

    fun criarAgendamentoCliente(
        nome: String,
        telefone: String,
        servicosSelecionados: List<Servico>,
        observacoes: String,
        dataIso: String,
        horario: String,
        formaPagamento: FormaPagamento,
        onSucesso: (Agendamento) -> Unit
    ) {
        if (servicosSelecionados.isEmpty()) {
            mensagemFeedback = "Selecione pelo menos 1 procedimento de Massoterapia ou Depilação."
            return
        }
        if (horario.isBlank()) {
            mensagemFeedback = "Escolha um horário disponível na agenda."
            return
        }
        if (nome.isBlank() || telefone.isBlank()) {
            mensagemFeedback = "Preencha seu Nome e seu WhatsApp."
            return
        }
        if (obterHorariosOcupadosNaData(dataIso).contains(horario)) {
            mensagemFeedback = "Este horário acabou de ser reservado. Escolha outro horário."
            return
        }

        val resumo = servicosSelecionados.joinToString(" + ") { "${it.nome} (${it.categoria.titulo})" }
        val novo = Agendamento(
            clienteNome = nome.trim(),
            clienteTelefone = telefone.trim(),
            servicosResumo = resumo,
            observacoesCliente = observacoes.trim(),
            valorTotal = servicosSelecionados.sumOf { it.preco },
            duracaoTotalMinutos = servicosSelecionados.sumOf { it.duracaoMinutos },
            dataIso = dataIso,
            horario = horario,
            formaPagamento = formaPagamento.descricao,
            status = StatusAgendamento.PENDENTE.name
        )
        listaAgendamentos = listOf(novo) + listaAgendamentos
        salvarTudoESincronizar()
        mensagemFeedback = "Agendamento realizado com sucesso!"
        onSucesso(novo)
    }

    fun alterarStatusAgendamento(agendamentoId: String, novoStatus: StatusAgendamento) {
        listaAgendamentos = listaAgendamentos.map {
            if (it.id == agendamentoId) it.copy(status = novoStatus.name) else it
        }
        salvarTudoESincronizar()
        mensagemFeedback = "Status atualizado para ${novoStatus.label}!"
    }

    fun alternarBloqueioHorario(dataIso: String, horario: String) {
        val existente = listaAgendamentos.find {
            it.dataIso == dataIso && it.horario == horario && it.statusEnum != StatusAgendamento.CANCELADO
        }
        if (existente != null) {
            if (existente.statusEnum == StatusAgendamento.BLOQUEADO) {
                listaAgendamentos = listaAgendamentos.filterNot { it.id == existente.id }
                salvarTudoESincronizar()
                mensagemFeedback = "Horário $horario liberado para clientes!"
            } else {
                mensagemFeedback = "Existe um cliente agendado às $horario. Cancele antes de bloquear."
            }
        } else {
            val bloqueio = Agendamento(
                clienteNome = "HORÁRIO BLOQUEADO",
                clienteTelefone = "-",
                servicosResumo = "Indisponível / Pausa do Profissional",
                observacoesCliente = "",
                valorTotal = 0.0,
                duracaoTotalMinutos = 0,
                dataIso = dataIso,
                horario = horario,
                formaPagamento = "-",
                status = StatusAgendamento.BLOQUEADO.name
            )
            listaAgendamentos = listOf(bloqueio) + listaAgendamentos
            salvarTudoESincronizar()
            mensagemFeedback = "Horário $horario bloqueado na agenda."
        }
    }

    fun adicionarServico(nome: String, categoria: CategoriaServico, preco: Double, duracao: Int, descricao: String) {
        val novo = Servico(
            id = "serv_${System.currentTimeMillis()}",
            nome = nome.trim(),
            categoria = categoria,
            duracaoMinutos = duracao,
            preco = preco,
            descricao = descricao.trim().ifBlank { "Procedimento especializado." }
        )
        catalogoServicos = catalogoServicos + novo
        salvarTudoESincronizar()
        mensagemFeedback = "Procedimento adicionado ao catálogo!"
    }

    fun removerServico(servicoId: String) {
        catalogoServicos = catalogoServicos.filterNot { it.id == servicoId }
        salvarTudoESincronizar()
        mensagemFeedback = "Procedimento removido."
    }

    fun adicionarHorarioGrade(novoHorario: String) {
        val h = novoHorario.trim()
        if (h.isNotBlank() && !horariosPadrao.contains(h)) {
            horariosPadrao = (horariosPadrao + h).sorted()
            salvarTudoESincronizar()
            mensagemFeedback = "Horário $h adicionado à grade!"
        }
    }

    fun removerHorarioGrade(horario: String) {
        horariosPadrao = horariosPadrao.filterNot { it == horario }
        salvarTudoESincronizar()
        mensagemFeedback = "Horário $horario removido da grade."
    }

    private fun carregarHorarios(): List<String> {
        val raw = prefs.getString("horarios_json", null) ?: return DadosIniciaisPadrao.HORARIOS_PADRAO
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            DadosIniciaisPadrao.HORARIOS_PADRAO
        }
    }

    private fun carregarServicos(): List<Servico> {
        val raw = prefs.getString("servicos_json", null) ?: return DadosIniciaisPadrao.CATALOGO_SERVICOS
        return try {
            desserializarServicos(JSONArray(raw))
        } catch (e: Exception) {
            DadosIniciaisPadrao.CATALOGO_SERVICOS
        }
    }

    private fun carregarAgendamentos(): List<Agendamento> {
        val raw = prefs.getString("agendamentos_json", null) ?: return emptyList()
        return try {
            desserializarAgendamentos(JSONArray(raw))
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializarServicos(lista: List<Servico>): JSONArray {
        val arr = JSONArray()
        lista.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("nome", s.nome)
                put("categoria", s.categoria.name)
                put("duracaoMinutos", s.duracaoMinutos)
                put("preco", s.preco)
                put("descricao", s.descricao)
            })
        }
        return arr
    }

    private fun desserializarServicos(arr: JSONArray): List<Servico> {
        val out = mutableListOf<Servico>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val cat = CategoriaServico.entries.find { it.name == o.optString("categoria") }
                ?: CategoriaServico.MASSOTERAPIA
            out.add(
                Servico(
                    id = o.optString("id", "serv_$i"),
                    nome = o.optString("nome", "Procedimento"),
                    categoria = cat,
                    duracaoMinutos = o.optInt("duracaoMinutos", 60),
                    preco = o.optDouble("preco", 0.0),
                    descricao = o.optString("descricao", "")
                )
            )
        }
        return out
    }

    private fun serializarAgendamentos(lista: List<Agendamento>): JSONArray {
        val arr = JSONArray()
        lista.forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id)
                put("clienteNome", a.clienteNome)
                put("clienteTelefone", a.clienteTelefone)
                put("servicosResumo", a.servicosResumo)
                put("observacoesCliente", a.observacoesCliente)
                put("valorTotal", a.valorTotal)
                put("duracaoTotalMinutos", a.duracaoTotalMinutos)
                put("dataIso", a.dataIso)
                put("horario", a.horario)
                put("formaPagamento", a.formaPagamento)
                put("status", a.status)
                put("criadoEmTimestamp", a.criadoEmTimestamp)
            })
        }
        return arr
    }

    private fun desserializarAgendamentos(arr: JSONArray): List<Agendamento> {
        val out = mutableListOf<Agendamento>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Agendamento(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    clienteNome = o.optString("clienteNome", ""),
                    clienteTelefone = o.optString("clienteTelefone", ""),
                    servicosResumo = o.optString("servicosResumo", ""),
                    observacoesCliente = o.optString("observacoesCliente", ""),
                    valorTotal = o.optDouble("valorTotal", 0.0),
                    duracaoTotalMinutos = o.optInt("duracaoTotalMinutos", 60),
                    dataIso = o.optString("dataIso", LocalDate.now().toString()),
                    horario = o.optString("horario", "08:00"),
                    formaPagamento = o.optString("formaPagamento", FormaPagamento.PIX.descricao),
                    status = o.optString("status", StatusAgendamento.PENDENTE.name),
                    criadoEmTimestamp = o.optLong("criadoEmTimestamp", System.currentTimeMillis())
                )
            )
        }
        return out
    }
}

// ====================================================================================
// 3. ACTIVITY PRINCIPAL E TEMA VISUAL
// ====================================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = EstadoEstudioManager(applicationContext)
        setContent {
            TemaEstudioMassoterapia {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AplicativoUnicoScreen(manager = manager)
                }
            }
        }
    }
}

@Composable
fun TemaEstudioMassoterapia(content: @Composable () -> Unit) {
    val esquemaCores = lightColorScheme(
        primary = Color(0xFF00695C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFB2DFDB),
        onPrimaryContainer = Color(0xFF00332C),
        secondary = Color(0xFFAD1457),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF8BBD0),
        background = Color(0xFFF4F7F6),
        surface = Color.White
    )
    MaterialTheme(colorScheme = esquemaCores, content = content)
}

enum class AbaPrincipal {
    CLIENTE_AGENDAR,
    CLIENTE_MEUS_HORARIOS,
    PROFISSIONAL
}

enum class SubAbaProfissional {
    AGENDA_E_PEDIDOS,
    EDITAR_SERVICOS_HORARIOS,
    NUVEM_E_PIX
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AplicativoUnicoScreen(manager: EstadoEstudioManager) {
    var abaAtual by remember { mutableStateOf(AbaPrincipal.CLIENTE_AGENDAR) }
    var adminAutenticado by remember { mutableStateOf(false) }
    var exibirDialogoSenha by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(manager.mensagemFeedback) {
        manager.mensagemFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            manager.mensagemFeedback = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = manager.nomeEstudio,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (abaAtual == AbaPrincipal.PROFISSIONAL)
                                "Painel do Profissional • Controle Total"
                            else
                                "Agendamento 100% Online • Massoterapia e Depilação",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    if (abaAtual == AbaPrincipal.PROFISSIONAL) {
                        TextButton(onClick = { abaAtual = AbaPrincipal.CLIENTE_AGENDAR }) {
                            Text("Modo Cliente", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (adminAutenticado) abaAtual = AbaPrincipal.PROFISSIONAL
                                else exibirDialogoSenha = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Área do Profissional",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = abaAtual == AbaPrincipal.CLIENTE_AGENDAR,
                    onClick = { abaAtual = AbaPrincipal.CLIENTE_AGENDAR },
                    icon = { Icon(Icons.Default.Spa, contentDescription = null) },
                    label = { Text("Agendar") }
                )
                NavigationBarItem(
                    selected = abaAtual == AbaPrincipal.CLIENTE_MEUS_HORARIOS,
                    onClick = { abaAtual = AbaPrincipal.CLIENTE_MEUS_HORARIOS },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = null) },
                    label = { Text("Meus Horários") }
                )
                NavigationBarItem(
                    selected = abaAtual == AbaPrincipal.PROFISSIONAL,
                    onClick = {
                        if (adminAutenticado) abaAtual = AbaPrincipal.PROFISSIONAL
                        else exibirDialogoSenha = true
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Profissional") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (abaAtual) {
                AbaPrincipal.CLIENTE_AGENDAR -> TelaClienteAgendar(
                    manager = manager,
                    onIrMeusHorarios = { abaAtual = AbaPrincipal.CLIENTE_MEUS_HORARIOS }
                )
                AbaPrincipal.CLIENTE_MEUS_HORARIOS -> TelaClienteMeusHorarios(manager = manager)
                AbaPrincipal.PROFISSIONAL -> TelaProfissionalCompleta(
                    manager = manager,
                    onSairAdmin = {
                        adminAutenticado = false
                        abaAtual = AbaPrincipal.CLIENTE_AGENDAR
                    }
                )
            }
        }
    }

    if (exibirDialogoSenha) {
        DialogoSenhaProfissional(
            senhaCorreta = manager.senhaAdmin,
            onDismiss = { exibirDialogoSenha = false },
            onSucesso = {
                adminAutenticado = true
                exibirDialogoSenha = false
                abaAtual = AbaPrincipal.PROFISSIONAL
            }
        )
    }
}

// ====================================================================================
// 4. VISÃO DO CLIENTE: ESCOLHER PROCEDIMENTO, VER AGENDA DISPONÍVEL E PAGAMENTO
// ====================================================================================
@Composable
fun TelaClienteAgendar(
    manager: EstadoEstudioManager,
    onIrMeusHorarios: () -> Unit
) {
    val context = LocalContext.current
    var filtroCat by remember { mutableStateOf<CategoriaServico?>(null) }
    var selecionadosIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var horarioEscolhido by remember { mutableStateOf("") }
    var formaPagamento by remember { mutableStateOf(FormaPagamento.PIX) }
    var nomeCliente by remember { mutableStateOf("") }
    var foneCliente by remember { mutableStateOf("") }
    var obsCliente by remember { mutableStateOf("") }
    var agendamentoRecemCriado by remember { mutableStateOf<Agendamento?>(null) }

    val dataIso = manager.dataSelecionada
    val ocupados = manager.obterHorariosOcupadosNaData(dataIso)
    val servicosEscolhidos = manager.catalogoServicos.filter { selecionadosIds.contains(it.id) }
    val valorTotal = servicosEscolhidos.sumOf { it.preco }
    val duracaoTotal = servicosEscolhidos.sumOf { it.duracaoMinutos }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SecaoCabecalho(
                numero = "1",
                titulo = "O que você deseja agendar?",
                subtitulo = "Selecione um ou mais procedimentos de Massoterapia ou Depilação"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filtroCat == null,
                    onClick = { filtroCat = null },
                    label = { Text("Todos") }
                )
                CategoriaServico.entries.forEach { cat ->
                    FilterChip(
                        selected = filtroCat == cat,
                        onClick = { filtroCat = cat },
                        label = { Text(cat.titulo) }
                    )
                }
            }
        }

        val servicosFiltrados = manager.catalogoServicos.filter {
            filtroCat == null || it.categoria == filtroCat
        }

        items(servicosFiltrados, key = { it.id }) { servico ->
            val marcado = selecionadosIds.contains(servico.id)
            CardServicoCliente(
                servico = servico,
                selecionado = marcado,
                onClick = {
                    selecionadosIds = if (marcado) selecionadosIds - servico.id else selecionadosIds + servico.id
                }
            )
        }

        item {
            SecaoCabecalho(
                numero = "2",
                titulo = "Agenda Disponível",
                subtitulo = "Escolha a data e veja os horários livres em tempo real"
            )
            Spacer(modifier = Modifier.height(8.dp))
            CarrosselDatas(
                dataSelecionadaIso = dataIso,
                onSelecionarData = { novaData ->
                    manager.dataSelecionada = novaData
                    horarioEscolhido = ""
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Horários em ${formatarDataBr(dataIso)}:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            GradeHorariosCliente(
                horariosPadrao = manager.horariosPadrao,
                horariosOcupados = ocupados,
                horarioSelecionado = horarioEscolhido,
                onSelecionarHorario = { horarioEscolhido = it }
            )
        }

        item {
            SecaoCabecalho(
                numero = "3",
                titulo = "Forma de Pagamento",
                subtitulo = "Escolha como prefere pagar pelo seu atendimento"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormaPagamento.entries.forEach { fp ->
                    val ativo = formaPagamento == fp
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { formaPagamento = fp },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (ativo) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = ativo, onClick = { formaPagamento = fp })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fp.descricao,
                                fontWeight = if (ativo) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = formaPagamento == FormaPagamento.PIX) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chave Pix do Estúdio:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF004D40)
                                )
                                Text(
                                    text = manager.chavePix,
                                    fontSize = 13.sp,
                                    color = Color(0xFF00695C)
                                )
                            }
                            TextButton(
                                onClick = {
                                    copiarParaClipboard(context, manager.chavePix)
                                    manager.mensagemFeedback = "Chave Pix copiada!"
                                }
                            ) {
                                Text("Copiar Pix", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            SecaoCabecalho(
                numero = "4",
                titulo = "Seus Dados e Observações",
                subtitulo = "Preencha seu contato e detalhes extras do que você precisa"
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nomeCliente,
                onValueChange = { nomeCliente = it },
                label = { Text("Seu Nome Completo *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = foneCliente,
                onValueChange = { foneCliente = it },
                label = { Text("Seu WhatsApp com DDD *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = obsCliente,
                onValueChange = { obsCliente = it },
                label = { Text("Alguma observação especial? (Ex.: dor lombar, pele sensível)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Resumo do Agendamento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Procedimentos: ${
                            if (servicosEscolhidos.isEmpty()) "Nenhum selecionado"
                            else servicosEscolhidos.joinToString(", ") { it.nome }
                        }",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Data e Horário: ${formatarDataBr(dataIso)} às ${horarioEscolhido.ifBlank { "--:--" }} ($duracaoTotal min)",
                        fontSize = 13.sp
                    )
                    Text(text = "Pagamento: ${formaPagamento.descricao}", fontSize = 13.sp)
                    Text(
                        text = "Valor Total: ${formatarMoedaBr(valorTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            manager.criarAgendamentoCliente(
                                nome = nomeCliente,
                                telefone = foneCliente,
                                servicosSelecionados = servicosEscolhidos,
                                observacoes = obsCliente,
                                dataIso = dataIso,
                                horario = horarioEscolhido,
                                formaPagamento = formaPagamento,
                                onSucesso = { novo ->
                                    selecionadosIds = emptySet()
                                    horarioEscolhido = ""
                                    obsCliente = ""
                                    agendamentoRecemCriado = novo
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CONFIRMAR AGENDAMENTO ONLINE", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    agendamentoRecemCriado?.let { ag ->
        AlertDialog(
            onDismissRequest = {
                agendamentoRecemCriado = null
                onIrMeusHorarios()
            },
            title = { Text("Agendamento Reservado!", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Seu horário foi registrado na agenda online:")
                    Text("• Cliente: ${ag.clienteNome}", fontSize = 13.sp)
                    Text("• Procedimento: ${ag.servicosResumo}", fontSize = 13.sp)
                    Text("• Data/Hora: ${formatarDataBr(ag.dataIso)} às ${ag.horario}", fontSize = 13.sp)
                    Text("• Pagamento: ${ag.formaPagamento} (${formatarMoedaBr(ag.valorTotal)})", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = "Olá! Acabei de agendar pelo aplicativo:
" +
                            "• Cliente: ${ag.clienteNome}
" +
                            "• Procedimento: ${ag.servicosResumo}
" +
                            "• Data/Hora: ${formatarDataBr(ag.dataIso)} às ${ag.horario}
" +
                            "• Pagamento: ${ag.formaPagamento} (${formatarMoedaBr(ag.valorTotal)})"
                        abrirWhatsApp(context, manager.whatsappProfissional, msg)
                        agendamentoRecemCriado = null
                        onIrMeusHorarios()
                    }
                ) {
                    Text("Enviar no WhatsApp")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        agendamentoRecemCriado = null
                        onIrMeusHorarios()
                    }
                ) {
                    Text("Ver Meus Horários")
                }
            }
        )
    }
}

// ====================================================================================
// 5. VISÃO DO CLIENTE: ACOMPANHAR MEUS HORÁRIOS E STATUS
// ====================================================================================
@Composable
fun TelaClienteMeusHorarios(manager: EstadoEstudioManager) {
    var filtroTexto by remember { mutableStateOf("") }

    val lista = manager.listaAgendamentos.filter { ag ->
        ag.statusEnum != StatusAgendamento.BLOQUEADO &&
            (filtroTexto.isBlank() ||
                ag.clienteNome.contains(filtroTexto, ignoreCase = true) ||
                ag.clienteTelefone.contains(filtroTexto, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Meus Horários Agendados", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text = "Acompanhe se o seu atendimento já foi confirmado pelo profissional.",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = filtroTexto,
            onValueChange = { filtroTexto = it },
            label = { Text("Filtrar por seu Nome ou WhatsApp") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (lista.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum agendamento encontrado.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(lista, key = { it.id }) { ag ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${formatarDataBr(ag.dataIso)} às ${ag.horario}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                BadgeStatusChip(ag.statusEnum)
                            }
                            Text("Cliente: ${ag.clienteNome} • ${ag.clienteTelefone}", fontSize = 13.sp)
                            Text("Procedimento: ${ag.servicosResumo}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            if (ag.observacoesCliente.isNotBlank()) {
                                Text("Observações: ${ag.observacoesCliente}", fontSize = 12.sp, color = Color(0xFFAD1457))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pagamento: ${ag.formaPagamento}", fontSize = 12.sp, color = Color.DarkGray)
                                Text(
                                    text = formatarMoedaBr(ag.valorTotal),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================================
// 6. PAINEL DO PROFISSIONAL (AGENDA, PEDIDOS, PAGAMENTOS + EDIÇÃO DE SERVIÇOS E NUVEM)
// ====================================================================================
@Composable
fun TelaProfissionalCompleta(
    manager: EstadoEstudioManager,
    onSairAdmin: () -> Unit
) {
    var subAba by remember { mutableStateOf(SubAbaProfissional.AGENDA_E_PEDIDOS) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = subAba == SubAbaProfissional.AGENDA_E_PEDIDOS,
                onClick = { subAba = SubAbaProfissional.AGENDA_E_PEDIDOS },
                label = { Text("Agenda & Pedidos", fontSize = 12.sp) }
            )
            FilterChip(
                selected = subAba == SubAbaProfissional.EDITAR_SERVICOS_HORARIOS,
                onClick = { subAba = SubAbaProfissional.EDITAR_SERVICOS_HORARIOS },
                label = { Text("Serviços & Horários", fontSize = 12.sp) }
            )
            FilterChip(
                selected = subAba == SubAbaProfissional.NUVEM_E_PIX,
                onClick = { subAba = SubAbaProfissional.NUVEM_E_PIX },
                label = { Text("Nuvem & Pix", fontSize = 12.sp) }
            )
        }

        when (subAba) {
            SubAbaProfissional.AGENDA_E_PEDIDOS -> SubTelaAdminAgenda(manager = manager, onSairAdmin = onSairAdmin)
            SubAbaProfissional.EDITAR_SERVICOS_HORARIOS -> SubTelaAdminCatalogo(manager = manager)
            SubAbaProfissional.NUVEM_E_PIX -> SubTelaAdminConfigNuvem(manager = manager)
        }
    }
}

@Composable
fun SubTelaAdminAgenda(manager: EstadoEstudioManager, onSairAdmin: () -> Unit) {
    val context = LocalContext.current
    val dataSel = manager.dataSelecionada
    var verTodosOsDias by remember { mutableStateOf(false) }

    val doPeriodo = manager.listaAgendamentos
        .filter { verTodosOsDias || it.dataIso == dataSel }
        .sortedWith(compareBy({ it.dataIso }, { it.horario }))

    val ativos = doPeriodo.filter {
        it.statusEnum != StatusAgendamento.CANCELADO && it.statusEnum != StatusAgendamento.BLOQUEADO
    }
    val faturamentoPrevisto = ativos.sumOf { it.valorTotal }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (verTodosOsDias) "Resumo — Todos os Dias" else "Resumo de ${formatarDataBr(dataSel)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = onSairAdmin) {
                            Text("Sair Admin", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Clientes Ativos", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("${ativos.size} agendamentos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Receita Prevista", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text(formatarMoedaBr(faturamentoPrevisto), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Minha Agenda por Dia", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                FilterChip(
                    selected = verTodosOsDias,
                    onClick = { verTodosOsDias = !verTodosOsDias },
                    label = { Text("Ver Todos os Dias") }
                )
            }
            CarrosselDatas(
                dataSelecionadaIso = dataSel,
                onSelecionarData = {
                    manager.dataSelecionada = it
                    verTodosOsDias = false
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Bloquear / Liberar Horários em ${formatarDataBr(dataSel)} (toque no horário):",
                fontSize = 12.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(manager.horariosPadrao) { hora ->
                    val reg = manager.listaAgendamentos.find {
                        it.dataIso == dataSel && it.horario == hora && it.statusEnum != StatusAgendamento.CANCELADO
                    }
                    val bloqueado = reg?.statusEnum == StatusAgendamento.BLOQUEADO
                    val temCliente = reg != null && !bloqueado

                    AssistChip(
                        onClick = { manager.alternarBloqueioHorario(dataSel, hora) },
                        label = {
                            Text(
                                when {
                                    bloqueado -> "$hora (Bloqueado)"
                                    temCliente -> "$hora (Cliente)"
                                    else -> "$hora (Livre)"
                                }
                            )
                        }
                    )
                }
            }
        }

        val pedidosClientes = doPeriodo.filter { it.statusEnum != StatusAgendamento.BLOQUEADO }

        item {
            Text(
                text = "O Que os Clientes Agendaram (${pedidosClientes.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (pedidosClientes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum agendamento para este dia.", color = Color.Gray)
                    }
                }
            }
        } else {
            items(pedidosClientes, key = { it.id }) { ag ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${formatarDataBr(ag.dataIso)} às ${ag.horario} (${ag.duracaoTotalMinutos} min)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            BadgeStatusChip(ag.statusEnum)
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cliente: ${ag.clienteNome}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("WhatsApp: ${ag.clienteTelefone}", fontSize = 13.sp, color = Color(0xFF00695C))
                            }
                            OutlinedButton(
                                onClick = {
                                    val msg = "Olá, ${ag.clienteNome}! Aqui é do ${manager.nomeEstudio}. Confirmando seu horário de ${ag.servicosResumo} em ${formatarDataBr(ag.dataIso)} às ${ag.horario}."
                                    abrirWhatsApp(context, ag.clienteTelefone, msg)
                                }
                            ) {
                                Text("WhatsApp", fontSize = 12.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("O que o cliente quer:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text(ag.servicosResumo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                if (ag.observacoesCliente.isNotBlank()) {
                                    Text(
                                        text = "Observação do cliente: "${ag.observacoesCliente}"",
                                        fontSize = 12.sp,
                                        color = Color(0xFFAD1457)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pagamento: ${ag.formaPagamento}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = formatarMoedaBr(ag.valorTotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { manager.alterarStatusAgendamento(ag.id, StatusAgendamento.CONFIRMADO) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Confirmar", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { manager.alterarStatusAgendamento(ag.id, StatusAgendamento.CONCLUIDO) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pago/Concluir", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { manager.alterarStatusAgendamento(ag.id, StatusAgendamento.CANCELADO) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", fontSize = 11.sp, color = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubTelaAdminCatalogo(manager: EstadoEstudioManager) {
    var nomeNovo by remember { mutableStateOf("") }
    var catNova by remember { mutableStateOf(CategoriaServico.MASSOTERAPIA) }
    var precoNovo by remember { mutableStateOf("") }
    var duracaoNova by remember { mutableStateOf("60") }
    var descNova by remember { mutableStateOf("") }
    var horarioNovo by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Adicionar Procedimento Manualmente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = nomeNovo,
                        onValueChange = { nomeNovo = it },
                        label = { Text("Nome do Procedimento") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoriaServico.entries.forEach { cat ->
                            FilterChip(
                                selected = catNova == cat,
                                onClick = { catNova = cat },
                                label = { Text(cat.titulo) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = precoNovo,
                            onValueChange = { precoNovo = it },
                            label = { Text("Valor (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = duracaoNova,
                            onValueChange = { duracaoNova = it },
                            label = { Text("Duração (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = descNova,
                        onValueChange = { descNova = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val p = precoNovo.replace(",", ".").toDoubleOrNull()
                            val d = duracaoNova.toIntOrNull() ?: 60
                            if (nomeNovo.isNotBlank() && p != null) {
                                manager.adicionarServico(nomeNovo, catNova, p, d, descNova)
                                nomeNovo = ""
                                precoNovo = ""
                                descNova = ""
                            } else {
                                manager.mensagemFeedback = "Preencha nome e valor numérico válido."
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Adicionar ao Catálogo")
                    }
                }
            }
        }

        item {
            Text("Procedimentos Atuais (${manager.catalogoServicos.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(manager.catalogoServicos, key = { it.id }) { s ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.nome, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${s.categoria.titulo} • ${s.duracaoMinutos} min • ${formatarMoedaBr(s.preco)}", fontSize = 12.sp, color = Color.Gray)
                    }
                    TextButton(onClick = { manager.removerServico(s.id) }) {
                        Text("Excluir", color = Color(0xFFC62828))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Grade de Horários Padrão", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = horarioNovo,
                            onValueChange = { horarioNovo = it },
                            label = { Text("Novo horário (Ex.: 19:30)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                manager.adicionarHorarioGrade(horarioNovo)
                                horarioNovo = ""
                            }
                        ) {
                            Text("Adicionar")
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(manager.horariosPadrao) { h ->
                            AssistChip(
                                onClick = { manager.removerHorarioGrade(h) },
                                label = { Text("$h ✕") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubTelaAdminConfigNuvem(manager: EstadoEstudioManager) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Configurações do Comércio & Banco Online", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = manager.nomeEstudio,
                        onValueChange = { manager.nomeEstudio = it },
                        label = { Text("Nome do Estúdio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = manager.whatsappProfissional,
                        onValueChange = { manager.whatsappProfissional = it },
                        label = { Text("Seu WhatsApp (com DDI 55 e DDD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = manager.chavePix,
                        onValueChange = { manager.chavePix = it },
                        label = { Text("Sua Chave Pix") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = manager.senhaAdmin,
                        onValueChange = { manager.senhaAdmin = it },
                        label = { Text("Senha de Acesso do Profissional") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = manager.firebaseDbUrl,
                        onValueChange = { manager.firebaseDbUrl = it },
                        label = { Text("URL do Firebase Realtime Database (https://...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                manager.salvarTudoESincronizar()
                                manager.mensagemFeedback = "Configurações salvas!"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                        OutlinedButton(
                            onClick = { manager.sincronizarManualAgora() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sincronizar Nuvem")
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================================
// 7. COMPONENTES VISUAIS COMPARTILHADOS E UTILITÁRIOS
// ====================================================================================
@Composable
fun CardServicoCliente(
    servico: Servico,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (selecionado) 2.dp else 1.dp,
                color = if (selecionado) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selecionado, onCheckedChange = { onClick() })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(servico.nome, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "${servico.categoria.titulo} • ${servico.duracaoMinutos} min",
                    fontSize = 12.sp,
                    color = if (servico.categoria == CategoriaServico.MASSOTERAPIA) Color(0xFF00695C) else Color(0xFFAD1457),
                    fontWeight = FontWeight.SemiBold
                )
                if (servico.descricao.isNotBlank()) {
                    Text(servico.descricao, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatarMoedaBr(servico.preco),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CarrosselDatas(
    dataSelecionadaIso: String,
    onSelecionarData: (String) -> Unit
) {
    val hoje = remember { LocalDate.now() }
    val dias = remember { (0..13).map { hoje.plusDays(it.toLong()) } }
    val fmt = remember { DateTimeFormatter.ofPattern("dd/MM") }
    val nomesSemana = mapOf(1 to "Seg", 2 to "Ter", 3 to "Qua", 4 to "Qui", 5 to "Sex", 6 to "Sáb", 7 to "Dom")

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(dias) { d ->
            val iso = d.toString()
            val sel = iso == dataSelecionadaIso
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelecionarData(iso) },
                color = if (sel) MaterialTheme.colorScheme.primary else Color.White,
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = nomesSemana[d.dayOfWeek.value] ?: "",
                        fontSize = 11.sp,
                        color = if (sel) Color.White.copy(alpha = 0.85f) else Color.Gray
                    )
                    Text(
                        text = d.format(fmt),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (sel) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GradeHorariosCliente(
    horariosPadrao: List<String>,
    horariosOcupados: Set<String>,
    horarioSelecionado: String,
    onSelecionarHorario: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        horariosPadrao.forEach { hora ->
            val ocupado = horariosOcupados.contains(hora)
            val sel = horarioSelecionado == hora
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !ocupado) { onSelecionarHorario(hora) },
                color = when {
                    ocupado -> Color(0xFFEEEEEE)
                    sel -> MaterialTheme.colorScheme.primary
                    else -> Color(0xFFE8F5E9)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (ocupado) "$hora (Ocupado)" else hora,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = when {
                        ocupado -> Color.Gray
                        sel -> Color.White
                        else -> Color(0xFF1B5E20)
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun SecaoCabecalho(numero: String, titulo: String, subtitulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(numero, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitulo, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BadgeStatusChip(status: StatusAgendamento) {
    Surface(
        color = Color(status.corHex).copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status.label,
            color = Color(status.corHex),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DialogoSenhaProfissional(
    senhaCorreta: String,
    onDismiss: () -> Unit,
    onSucesso: () -> Unit
) {
    var digitada by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acesso do Profissional") },
        text = {
            Column {
                Text(
                    text = "Digite sua senha de administrador para gerenciar sua agenda e ver os pedidos dos clientes (Padrão: $senhaCorreta):",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = digitada,
                    onValueChange = {
                        digitada = it
                        erro = false
                    },
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = erro,
                    singleLine = true
                )
                if (erro) {
                    Text("Senha incorreta.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (digitada.trim() == senhaCorreta) onSucesso()
                    else erro = true
                }
            ) {
                Text("Entrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun formatarMoedaBr(valor: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)
}

fun formatarDataBr(dataIso: String): String {
    return try {
        LocalDate.parse(dataIso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        dataIso
    }
}

fun copiarParaClipboard(context: Context, texto: String) {
    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    cb?.setPrimaryClip(ClipData.newPlainText("Chave Pix", texto))
}

fun abrirWhatsApp(context: Context, telefone: String, mensagem: String) {
    try {
        val limpo = telefone.filter { it.isDigit() }
        val foneFinal = if (limpo.length <= 11) "55$limpo" else limpo
        val url = "https://wa.me/$foneFinal?text=${URLEncoder.encode(mensagem, "UTF-8")}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Ignora caso não haja navegador/WhatsApp instalado no emulador
    }
}
