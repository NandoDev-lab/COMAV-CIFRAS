package com.nandohypesoft.comav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nandohypesoft.comav.data.database.AppDatabase
import com.nandohypesoft.comav.data.preferences.UserPreferencesRepository
import com.nandohypesoft.comav.data.repository.MusicaRepository
import com.nandohypesoft.comav.ui.MainScreen
import com.nandohypesoft.comav.ui.MainViewModel
import com.nandohypesoft.comav.ui.MainViewModelFactory
import com.nandohypesoft.comav.ui.theme.COMAVTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MusicaRepository(database.musicaDao())
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)

        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory(repository, userPreferencesRepository)
        }

        setContent {
            val temaEscuro by viewModel.temaEscuro.collectAsState()

            COMAVTheme(darkTheme = temaEscuro) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
