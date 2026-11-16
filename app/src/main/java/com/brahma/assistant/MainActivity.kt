package com.brahma.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.brahma.assistant.ui.BrahmaViewModel
import com.brahma.assistant.ui.screens.HardwareHubScreen
import com.brahma.assistant.ui.screens.MainAssistantScreen
import com.brahma.assistant.ui.screens.MemoryCortexScreen
import com.brahma.assistant.ui.theme.BrahmaCyan
import com.brahma.assistant.ui.theme.BrahmaSurface
import com.brahma.assistant.ui.theme.BrahmaText
import com.brahma.assistant.ui.theme.BrahmaTextDim
import com.brahma.assistant.ui.theme.BrahmaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BrahmaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BrahmaTheme {
                BrahmaAppScaffold(viewModel)
            }
        }
    }
}

@Composable
fun BrahmaAppScaffold(viewModel: BrahmaViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = BrahmaSurface,
                contentColor = BrahmaText
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Chat, contentDescription = "AI Assistant") },
                    label = { Text("Assistant") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrahmaCyan,
                        selectedTextColor = BrahmaCyan,
                        unselectedIconColor = BrahmaTextDim,
                        unselectedTextColor = BrahmaTextDim,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Memory, contentDescription = "Memory Cortex") },
                    label = { Text("Cortex") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrahmaCyan,
                        selectedTextColor = BrahmaCyan,
                        unselectedIconColor = BrahmaTextDim,
                        unselectedTextColor = BrahmaTextDim,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Tune, contentDescription = "Hardware Hub") },
                    label = { Text("System Hub") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrahmaCyan,
                        selectedTextColor = BrahmaCyan,
                        unselectedIconColor = BrahmaTextDim,
                        unselectedTextColor = BrahmaTextDim,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        Modifier.padding(innerPadding).let {
            when (selectedTab) {
                0 -> MainAssistantScreen(viewModel)
                1 -> MemoryCortexScreen(viewModel)
                2 -> HardwareHubScreen(viewModel)
            }
        }
    }
}
