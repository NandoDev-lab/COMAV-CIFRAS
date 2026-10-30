package com.nandohypesoft.comav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.nandohypesoft.comav.data.database.AppDatabase
import com.nandohypesoft.comav.data.preferences.UserPreferencesRepository
import com.nandohypesoft.comav.data.repository.MusicaRepository
import com.nandohypesoft.comav.ui.MainScreen
import com.nandohypesoft.comav.ui.MainViewModel
import com.nandohypesoft.comav.ui.MainViewModelFactory
import com.nandohypesoft.comav.ui.theme.COMAVTheme
import com.nandohypesoft.comav.util.SessaoManager
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MusicaRepository(database.musicaDao())
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        MainViewModelFactory(
            repository,
            userPreferencesRepository,
            database.escalaCultoDao(),
            database.escalaMusicoDataDao(),
            database.ensaioDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val temaEscuro by viewModel.temaEscuro.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            COMAVTheme(darkTheme = temaEscuro) {
                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        SessaoManager.iniciarSessaoAtiva(
            context = applicationContext,
            nomeUsuario = viewModel.nomeUsuario.value,
            tipoFuncao = viewModel.tipoFuncaoEquipe.value
        )
        SessaoManager.escutarAtividadesSessaoFirebase(
            context = applicationContext,
            tipoFuncao = viewModel.tipoFuncaoEquipe.value,
            nomeUsuario = viewModel.nomeUsuario.value
        )
    }

    override fun onStop() {
        super.onStop()
        SessaoManager.encerrarSessaoAtiva(applicationContext)
        SessaoManager.pararEscuta()
    }

    override fun onDestroy() {
        super.onDestroy()
        SessaoManager.encerrarSessaoAtiva(applicationContext)
        SessaoManager.pararEscuta()
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = "Logo COMAV",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
