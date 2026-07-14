package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.ui.theme.CorrectGreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.TestRepository
import com.example.ui.LoadingState
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.UiState
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CbtExamScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CleanOnPrimaryContainer
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database & Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = TestRepository(database.testDao())
    val factory = MainViewModelFactory(repository, applicationContext)
    val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

    setContent {
      MyApplicationTheme {
        MainAppContent(viewModel)
      }
    }
  }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
  val uiState by viewModel.uiState.collectAsState()
  val loadingState by viewModel.loadingState.collectAsState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      // Show Bottom navigation bar ONLY when NOT in active CBT exam
      if (uiState != UiState.CbtExam) {
        CleanBottomNavigation(
          currentUiState = uiState,
          onNavigate = { viewModel.setUiState(it) }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Main Screens Routing
      when (val state = uiState) {
        UiState.Dashboard -> DashboardScreen(viewModel = viewModel)
        UiState.Library -> LibraryScreen(viewModel = viewModel)
        UiState.CbtExam -> CbtExamScreen(viewModel = viewModel)
        is UiState.Result -> ResultScreen(viewModel = viewModel, testId = state.testId)
        UiState.Analytics -> AnalyticsScreen(viewModel = viewModel)
        UiState.Settings -> SettingsScreen(viewModel = viewModel)
      }

      // Smooth Overlaid Loading and Error dialogs
      AnimatedVisibility(
        visible = loadingState is LoadingState.Loading,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        val msg = (loadingState as? LoadingState.Loading)?.message ?: "Processing..."
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) {},
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
              .padding(32.dp)
              .fillMaxWidth(0.85f)
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(
                color = CleanPrimary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(20.dp))
              Text(
                text = "Processing Request",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      // Error Overlaid dialog
      AnimatedVisibility(
        visible = loadingState is LoadingState.Error,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        val errorMsg = (loadingState as? LoadingState.Error)?.message ?: "An unexpected error occurred."
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) {},
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
              .padding(32.dp)
              .fillMaxWidth(0.85f)
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error icon",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Operation Failed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(24.dp))
              Button(
                onClick = { viewModel.dismissLoading() },
                colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_error_dismiss")
              ) {
                Text("Dismiss", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Success overlay
      AnimatedVisibility(
        visible = loadingState is LoadingState.Success,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        val successMsg = (loadingState as? LoadingState.Success)?.message ?: "Success!"
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) {},
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
              .padding(32.dp)
              .fillMaxWidth(0.85f)
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Success icon",
                tint = CorrectGreen,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Success",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = successMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(24.dp))
              Button(
                onClick = { viewModel.dismissLoading() },
                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_success_dismiss")
              ) {
                Text("Continue", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CleanBottomNavigation(
  currentUiState: UiState,
  onNavigate: (UiState) -> Unit
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
    modifier = Modifier.testTag("clean_bottom_nav")
  ) {
    // Tab 1: Dashboard
    NavigationBarItem(
      selected = currentUiState == UiState.Dashboard,
      onClick = { onNavigate(UiState.Dashboard) },
      icon = {
        Icon(
          imageVector = Icons.Default.Home,
          contentDescription = "AI Wizard Tab"
        )
      },
      label = { Text("AI Wizard", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = CleanPrimary,
        selectedTextColor = CleanPrimary,
        indicatorColor = CleanPrimaryContainer,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray
      ),
      modifier = Modifier.testTag("nav_item_dashboard")
    )

    // Tab 2: Library
    NavigationBarItem(
      selected = currentUiState == UiState.Library || currentUiState is UiState.Result,
      onClick = { onNavigate(UiState.Library) },
      icon = {
        Icon(
          imageVector = Icons.Default.LibraryBooks,
          contentDescription = "My Library Tab"
        )
      },
      label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = CleanPrimary,
        selectedTextColor = CleanPrimary,
        indicatorColor = CleanPrimaryContainer,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray
      ),
      modifier = Modifier.testTag("nav_item_library")
    )

    // Tab 3: Analytics
    NavigationBarItem(
      selected = currentUiState == UiState.Analytics,
      onClick = { onNavigate(UiState.Analytics) },
      icon = {
        Icon(
          imageVector = Icons.Default.Analytics,
          contentDescription = "Analytics Tab"
        )
      },
      label = { Text("Analytics", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = CleanPrimary,
        selectedTextColor = CleanPrimary,
        indicatorColor = CleanPrimaryContainer,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray
      ),
      modifier = Modifier.testTag("nav_item_analytics")
    )

    // Tab 4: Settings
    NavigationBarItem(
      selected = currentUiState == UiState.Settings,
      onClick = { onNavigate(UiState.Settings) },
      icon = {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings Tab"
        )
      },
      label = { Text("Control", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = CleanPrimary,
        selectedTextColor = CleanPrimary,
        indicatorColor = CleanPrimaryContainer,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray
      ),
      modifier = Modifier.testTag("nav_item_settings")
    )
  }
}
