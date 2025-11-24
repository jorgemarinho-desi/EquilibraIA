package com.example.equilibraia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite // Usaremos este ícone
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star // Ou estrela
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.equilibraia.AppDatabase
import com.example.equilibraia.Tarefa

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                    var showSplash by remember { mutableStateOf(true) }
                    if (showSplash) {
                        SplashScreen(onTimeout = { showSplash = false })
                    } else {
                        EquilibraApp()
                    }
                }
            }
        }
    }
}

// --- TELA
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(
                durationMillis = 800,
                easing = { OvershootInterpolator(4f).getInterpolation(it) }
            )
        )
        delay(1500)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Equilibra.IA",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                modifier = Modifier.scale(scale.value)
            )
            Text(
                text = "Produtividade & Bem-estar",
                fontSize = 14.sp,
                color = Color(0xFF43A047),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquilibraApp() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val escopo = rememberCoroutineScope()
    val listaTarefas = remember { mutableStateListOf<Tarefa>() }

    val dicas = listOf(
        "💧 Beba um copo de água agora.",
        "👀 Olhe para longe da tela por 20 segundos.",
        "🧘 Respire fundo: Inspire (4s), Segure (7s), Expire (8s).",
        "🚶 Levante-se e alongue as pernas.",
        "📵 Que tal 5 minutos sem celular?",
        "🌱 Cuide da sua energia, não só do seu tempo."
    )
    val dicaDoDia = remember { dicas.random() }

    LaunchedEffect(Unit) {
        db.tarefaDao().getTarefasOrdenadasPorCarga().collect { tarefas ->
            listaTarefas.clear()
            listaTarefas.addAll(tarefas)
        }
    }

    var titulo by remember { mutableStateOf("") }
    var nivelEnergia by remember { mutableFloatStateOf(1f) }
    var categoriaSelecionada by remember { mutableStateOf("Trabalho") }
    var tempoSelecionado by remember { mutableIntStateOf(30) }
    var modoDescansoAtivo by remember { mutableStateOf(false) }

    val totalTarefas = listaTarefas.size
    val tarefasFeitas = listaTarefas.count { it.concluida }
    val progresso = if (totalTarefas > 0) tarefasFeitas.toFloat() / totalTarefas else 0f

    val tarefasExibidas = if (modoDescansoAtivo) {
        listaTarefas.filter { !it.concluida && it.nivelEnergia <= 2 }
    } else {
        listaTarefas
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Equilibra.IA", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                Text(if(modoDescansoAtivo) "Modo Zen Ativado 🧘" else "Foco e Produtividade 🚀", fontSize = 12.sp, color = Color.Gray)
            }
            Switch(checked = modoDescansoAtivo, onCheckedChange = { modoDescansoAtivo = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32)))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card Dica
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = "Dica", tint = Color(0xFF00695C))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Dica de Equilíbrio:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00695C))
                    Text(dicaDoDia, fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (totalTarefas > 0) {
            Text("Progresso Diário: ${(progresso * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = progresso, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = Color(0xFF2E7D32), trackColor = Color(0xFFC8E6C9))
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Formulário
        if (!modoDescansoAtivo) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = titulo, onValueChange = { titulo = it },
                        label = { Text("Nova Tarefa") }, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Categoria:", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Trabalho", "Estudo", "Pessoal").forEach { cat ->
                            FilterChip(selected = categoriaSelecionada == cat, onClick = { categoriaSelecionada = cat }, label = { Text(cat) }, leadingIcon = { if (categoriaSelecionada == cat) Text("✓") })
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Tempo Estimado:", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 60, 90).forEach { min ->
                            FilterChip(selected = tempoSelecionado == min, onClick = { tempoSelecionado = min }, label = { Text("${min}m") }, leadingIcon = { if (tempoSelecionado == min) Text("⏱️") })
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Nível de Energia: ${nivelEnergia.toInt()}/5", fontWeight = FontWeight.SemiBold)
                    Slider(value = nivelEnergia, onValueChange = { nivelEnergia = it }, valueRange = 1f..5f, steps = 3, colors = SliderDefaults.colors(thumbColor = if (nivelEnergia > 3) Color(0xFFD32F2F) else Color(0xFF1976D2)))

                    Button(
                        onClick = {
                            if (titulo.isNotEmpty()) {
                                val pendente = listaTarefas.filter { !it.concluida }.sumOf { it.nivelEnergia }
                                if (pendente + nivelEnergia.toInt() > 15) dispararAlertaBurnout(context)
                                val nova = Tarefa(
                                    titulo = titulo, descricao = "", dataPrazo = System.currentTimeMillis(),
                                    nivelEnergia = nivelEnergia.toInt(), categoriaBemEstar = categoriaSelecionada, duracaoMinutos = tempoSelecionado
                                )
                                escopo.launch { db.tarefaDao().inserirTarefa(nova); titulo = ""; nivelEnergia = 1f }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("Adicionar") }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista
        if (tarefasExibidas.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🍃", fontSize = 50.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = if (modoDescansoAtivo) "Modo Zen ativado.\nAproveite o silêncio." else "Tudo limpo por aqui!\nSua mente agradece.", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(tarefasExibidas) { tarefa ->
                    ItemTarefaAvancado(tarefa, onCheck = { m -> escopo.launch { db.tarefaDao().atualizarTarefa(tarefa.copy(concluida = m)) } }, onDelete = { escopo.launch { db.tarefaDao().deletarTarefa(tarefa) } })
                }
            }
        }
    }
}

@Composable
fun ItemTarefaAvancado(tarefa: Tarefa, onCheck: (Boolean) -> Unit, onDelete: () -> Unit) {
    val corFundo by animateColorAsState(targetValue = if (tarefa.concluida) Color(0xFFE0E0E0) else if (tarefa.nivelEnergia >= 4) Color(0xFFFFEBEE) else Color(0xFFE8F5E9), label = "corCard")
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = corFundo), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = tarefa.concluida, onCheckedChange = { onCheck(it) })
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(text = tarefa.titulo, fontWeight = FontWeight.Bold, textDecoration = if (tarefa.concluida) TextDecoration.LineThrough else TextDecoration.None, color = if (tarefa.concluida) Color.Gray else Color.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${tarefa.categoriaBemEstar} • E:${tarefa.nivelEnergia}", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("⏱️ ${tarefa.duracaoMinutos}m", fontSize = 10.sp, color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
                    if (tarefa.nivelEnergia >= 4 && !tarefa.concluida) Text(" 🔥", fontSize = 10.sp)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray) }
        }
    }
}

fun dispararAlertaBurnout(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "equilibra_alertas"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Alertas de Bem-Estar", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }
    val notificacao = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("⚠️ Cuidado: Sobrecarga Mental")
        .setContentText("Muitas tarefas pesadas hoje. Respire e faça uma pausa!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
    notificationManager.notify(1, notificacao)
}