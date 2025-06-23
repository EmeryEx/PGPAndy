package com.example.pgpandy

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@Composable
fun KeyListScreen() {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var keys by remember { mutableStateOf(listOf<PgpKeyInfo>()) }

    fun refresh() {
        keys = DatabaseHelper(context).getPrivateKeys()
    }

    LaunchedEffect(Unit) { refresh() }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val armored = stream.bufferedReader().readText()
                KeyImportService(context).importArmoredKey(armored)
                refresh()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (keys.isEmpty()) {
            Text(stringResource(R.string.msg_no_private_keys), modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(keys) { key ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = key.comment ?: key.userId ?: key.fingerprint.take(8))
                                key.createdAt?.let {
                                    val date = Date(it * 1000)
                                    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    Text(fmt.format(date))
                                }
                            }
                            IconButton(onClick = {
                                DatabaseHelper(context).deleteKey(key.fingerprint)
                                refresh()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete_key))
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { importLauncher.launch(arrayOf("*/*")) },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            containerColor = Color(0xFF440020),
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.FileOpen,
                contentDescription = stringResource(R.string.cd_import_key)
            )
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFF440020),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Key, contentDescription = null)
        }

        if (showDialog) {
            KeyGenerationDialog(onDismiss = { showDialog = false; refresh() })
        }
    }
}
