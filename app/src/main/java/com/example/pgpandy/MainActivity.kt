package com.example.pgpandy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { PGPAndyApp() }
    }
}

@Composable
fun PGPAndyApp() {
    var darkTheme by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(Screen.Contacts) }

    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("PGPAndy", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    NavigationDrawerItem(
                        label = { Text("Contacts") },
                        selected = screen == Screen.Contacts,
                        onClick = { screen = Screen.Contacts; scope.launch { drawerState.close() } }
                    )
                    NavigationDrawerItem(
                        label = { Text("Create Message") },
                        selected = screen == Screen.Message,
                        onClick = { screen = Screen.Message; scope.launch { drawerState.close() } }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = { Text("PGPAndy") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* search */ }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { /* lock */ }) {
                                Icon(Icons.Default.Lock, contentDescription = "Lock")
                            }
                            IconButton(onClick = { darkTheme = !darkTheme }) {
                                Icon(
                                    if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Theme"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    when (screen) {
                        Screen.Contacts -> ContactForm()
                        Screen.Message -> MessageForm()
                    }
                }
            }
        }
    }
}

private enum class Screen { Contacts, Message }

@Composable
fun ContactForm() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("PGP Contact", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Public Key") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { /* save */ }, modifier = Modifier.align(Alignment.End)) { Text("Save") }
    }
}

@Composable
fun MessageForm() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create Message", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Recipient") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { /* send */ }, modifier = Modifier.align(Alignment.End)) { Text("Send") }
    }
}
