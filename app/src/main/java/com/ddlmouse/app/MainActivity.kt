package com.ddlmouse.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddlmouse.app.ui.DDLMouseApp
import com.ddlmouse.app.ui.DdlMouseViewModel
import com.ddlmouse.app.ui.theme.DDLMouseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as DDLMouseApplication).container
        setContent {
            DDLMouseTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val viewModel: DdlMouseViewModel = viewModel(
                    factory = DdlMouseViewModel.Factory(
                        taskRepository = appContainer.taskRepository,
                        petRepository = appContainer.petRepository,
                        dailySummaryRepository = appContainer.dailySummaryRepository,
                        settingsStore = appContainer.settingsStore
                    )
                )
                DDLMouseApp(viewModel = viewModel)
            }
        }
    }
}

