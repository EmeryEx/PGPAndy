package com.example.pgpandy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize local database on first launch. This will create tables
        // or run migrations as needed based on the defined DB version.
        DatabaseHelper(this).writableDatabase

        val savedLanguage = DatabaseHelper(this).getPreference("language")
        val language = if (!savedLanguage.isNullOrEmpty()) savedLanguage else "en-US"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PGPAndyApp(language ?: "")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PGPAndyApp(initialLanguageTag: String) {
    var darkTheme by remember { mutableStateOf(false) }
    var languageTag by remember { mutableStateOf(initialLanguageTag) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(Screen.ContactList) }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        darkTheme = DatabaseHelper(context).getPreference("dark_theme") == "1"
    }

    LaunchedEffect(darkTheme) {
        DatabaseHelper(context).setPreference("dark_theme", if (darkTheme) "1" else "0")
    }

    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = if (darkTheme) Color(0xFF222222) else MaterialTheme.colorScheme.background,
                    drawerShape = RoundedCornerShape(0.dp) // <-- removes rounded corner
                ) {
                    Text(stringResource(R.string.app_name), color = if (darkTheme) Color.White else Color.DarkGray, modifier = Modifier
                        .background(Color.Transparent).padding(16.dp).fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium)
                    NavigationDrawerItem(
                        label = { Text("Inbox") },
                        selected = screen == Screen.Inbox,
                        onClick = { screen = Screen.Inbox; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(0.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF440020),
                            unselectedContainerColor = if (darkTheme) Color(0xFF252525) else Color.White,
                            selectedTextColor = Color.White,
                            unselectedTextColor = if (darkTheme) Color.White else Color(0xFF220020),
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color(0xFF220020)
                        )
                    )
                    NavigationDrawerItem(
                        label = { Text("Sent") },
                        selected = screen == Screen.Message,
                        onClick = { screen = Screen.Message; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(0.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF440020),
                            unselectedContainerColor = if (darkTheme) Color(0xFF252525) else Color.White,
                            selectedTextColor = Color.White,
                            unselectedTextColor = if (darkTheme) Color.White else Color(0xFF220020),
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color(0xFF220020)
                        )
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_contacts)) },
                        selected = screen == Screen.ContactList,
                        onClick = { screen = Screen.ContactList; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(0.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF440020),
                            unselectedContainerColor = if (darkTheme) Color(0xFF252525) else Color.White,
                            selectedTextColor = Color.White,
                            unselectedTextColor = if (darkTheme) Color.White else Color(0xFF220020),
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color(0xFF220020)
                        )
                    )
                    NavigationDrawerItem(
                        label = { Text("Private Keys") },
                        selected = screen == Screen.KeyList,
                        onClick = { screen = Screen.KeyList; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(0.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF440020),
                            unselectedContainerColor = if (darkTheme) Color(0xFF252525) else Color.White,
                            selectedTextColor = Color.White,
                            unselectedTextColor = if (darkTheme) Color.White else Color(0xFF220020),
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color(0xFF220020)
                        )
                    )
                    NavigationDrawerItem(
                        label = { Text("Help") },
                        selected = screen == Screen.Help,
                        onClick = { screen = Screen.Help; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(0.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF440020),
                            unselectedContainerColor = if (darkTheme) Color(0xFF252525) else Color.White,
                            selectedTextColor = Color.White,
                            unselectedTextColor = if (darkTheme) Color.White else Color(0xFF220020),
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color(0xFF220020)
                        )
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = { Text(stringResource(R.string.app_name)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_menu))
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* lock */ }) {
                                Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.cd_lock))
                            }
                            IconButton(onClick = { darkTheme = !darkTheme }) {
                                Icon(
                                    if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = stringResource(R.string.cd_theme)
                                )
                            }
                            Box {
                                IconButton(onClick = { languageMenuExpanded = true }) {
                                    Icon(Icons.Default.Language, contentDescription = stringResource(R.string.cd_language))
                                }
                                DropdownMenu(expanded = languageMenuExpanded, onDismissRequest = { languageMenuExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text(
                                            text = stringResource(R.string.language_en),
                                            color = if (languageTag == "en-US") Color.Red else Color.Unspecified
                                        ) },
                                        onClick = {
                                            languageMenuExpanded = false
                                            val newLang = "en-US"
                                            if (languageTag != newLang) {
                                                DatabaseHelper(context).setPreference("language", newLang)
                                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLang))
                                                (context as? ComponentActivity)?.recreate()
                                            }
                                        },
                                        modifier = Modifier.background(
                                            if (languageTag == "en-US") Color(0x33229020) else Color.Transparent
                                        )
                                    )
                                    DropdownMenuItem(
                                        text = {Text(
                                            text = stringResource(R.string.language_es),
                                            color = if (languageTag == "es-MX") Color.Red else Color.Unspecified
                                        )},
                                        onClick = {
                                            languageMenuExpanded = false
                                            val newLang = "es-MX"
                                            if (languageTag != newLang) {
                                                DatabaseHelper(context).setPreference("language", newLang)
                                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLang))
                                                (context as? ComponentActivity)?.recreate()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(text = { Text(
                                        text = stringResource(R.string.language_fr),
                                        color = if (languageTag == "fr") Color.Red else Color.Unspecified
                                    ) }, onClick = {
                                        languageMenuExpanded = false
                                        languageTag = "fr"
                                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
                                    })
                                    DropdownMenuItem(text = { Text(
                                        text = stringResource(R.string.language_ru),
                                        color = if (languageTag == "ru") Color.Red else Color.Unspecified
                                    ) }, onClick = {
                                        languageMenuExpanded = false
                                        languageTag = "ru"
                                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
                                    })
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    when (screen) {
                        Screen.Inbox -> InboxScreen()
                        Screen.Message -> MessageForm()
                        Screen.ContactList -> ContactListScreen { screen = Screen.AddContact }
                        Screen.KeyList -> KeyListScreen { screen = Screen.KeyList }
                        Screen.AddContact -> ContactForm { screen = Screen.ContactList }
                        Screen.Help -> HelpScreen()
                    }
                }
            }
        }
    }
}

private enum class Screen { Inbox, Message, ContactList, KeyList, AddContact, Help }

