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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var screen by remember { mutableStateOf(Screen.ContactList) }

    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("PGPAndy", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    NavigationDrawerItem(
                        label = { Text("Contacts") },
                        selected = screen == Screen.ContactList,
                        onClick = { screen = Screen.ContactList; scope.launch { drawerState.close() } }
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
                        Screen.ContactList -> ContactListScreen { screen = Screen.AddContact }
                        Screen.AddContact -> ContactForm { screen = Screen.ContactList }
                        Screen.Message -> MessageForm()
                    }
                }
            }
        }
    }
}

private enum class Screen { ContactList, AddContact, Message }

data class Contact(val name: String, val publicKey: String)

object ContactRepository {
    val contacts = mutableStateListOf<Contact>()
}

@Composable
fun ContactForm(onSaved: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("PGP Contact", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = key, onValueChange = { key = it }, label = { Text("Public Key") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                ContactRepository.contacts.add(Contact(name, key))
                onSaved()
            },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Save") }
    }
}

@Composable
fun MessageForm() {
    var recipient by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create Message", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("Recipient") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { /* send */ }, modifier = Modifier.align(Alignment.End)) { Text("Send") }
    }
}

@Composable
fun ContactListScreen(onAddContact: () -> Unit) {
    val contacts = ContactRepository.contacts

    Box(modifier = Modifier.fillMaxSize()) {
        if (contacts.isEmpty()) {
            Text("No contacts added", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(contacts) { contact ->
                    ListItem(
                        headlineText = { Text(contact.name) },
                        supportingText = { Text(contact.publicKey) }
                    )
                    Divider()
                }
            }
        }

        FloatingActionButton(
            onClick = onAddContact,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact")
        }
    }
}
