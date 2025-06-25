package engineer.warfare.pgpandy

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import engineer.warfare.pgpandy.PgpKeyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp

@Composable
fun KeyListScreen(isDarkTheme: Boolean) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var keys by remember { mutableStateOf(listOf<PgpKeyInfo>()) }
    var keyToDelete by remember { mutableStateOf<PgpKeyInfo?>(null) }
    var keyToView by remember { mutableStateOf<PgpKeyInfo?>(null) }

    fun refresh() {
        keys = DatabaseHelper(context).getPrivateKeys()
    }

    LaunchedEffect(Unit) { refresh() }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileName = context.contentResolver.query(
                it,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }

            if (fileName == null || !fileName.endsWith(".asc", ignoreCase = true)) {
                Toast.makeText(context, context.getString(R.string.msg_invalid_key_file), Toast.LENGTH_LONG).show()
                return@let
            }

            context.contentResolver.openInputStream(it)?.use { stream ->
                val armored = stream.bufferedReader().readText()
                try {
                    KeyImportService(context).importArmoredKey(armored)
                    refresh()
                } catch (_: Exception) {
                    Toast.makeText(context, context.getString(R.string.msg_invalid_key_file), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (keys.isEmpty()) {
            Text(stringResource(R.string.msg_no_private_keys), modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)) {
                items(keys) { key ->
                    Card(
                        onClick = { keyToView = key },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF363636) else Color.White,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = if (isDarkTheme) BorderStroke(0.dp, Color.Black) else BorderStroke(0.5.dp, Color(0xFFDDDDDD))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = key.comment ?: key.userId ?:"?")
                                Text(text = "Fingerprint:", fontSize = 11.sp, color = if (isDarkTheme) Color(0xFFEEAAAA) else Color(0xFFAA5555))
                                Text(text = key.fingerprint, fontSize = 10.sp)
                                key.createdAt?.let {
                                    val date = Date(it * 1000)
                                    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    Text("Generated: " + fmt.format(date), fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { keyToDelete = key }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete_key))
                            }
                        }
                    }
                }
            }
        }

        keyToDelete?.let { pending ->
            AlertDialog(
                onDismissRequest = { keyToDelete = null },
                confirmButton = {
                    TextButton(onClick = {
                        DatabaseHelper(context).deleteKey(pending.fingerprint)
                        keyToDelete = null
                        refresh()
                    }) { Text(stringResource(R.string.action_delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { keyToDelete = null }) {
                        Text(stringResource(R.string.action_close))
                    }
                },
                title = { Text(stringResource(R.string.cd_delete_key)) },
                text = { Text(stringResource(R.string.confirm_delete_key)) }
            )
        }

        keyToView?.let { viewKey ->
            AlertDialog(
                onDismissRequest = { keyToView = null },
                confirmButton = {
                    TextButton(onClick = { keyToView = null }) {
                        Text(stringResource(R.string.action_close))
                    }
                },
                title = { Text(stringResource(R.string.title_public_key)) },
                text = {
                    val clipboardManager = LocalClipboardManager.current
                    val publicKey = remember(viewKey) {
                        PgpKeyUtils.extractPublicKey(viewKey.armoredKey) ?: ""
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = publicKey,
                                fontSize = 12.sp,
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(publicKey))
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            shape = RoundedCornerShape(3.dp) // 👈 Rounded corners
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.action_copy_public_key))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(viewKey.armoredKey))
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.action_copy_private_key))
                        }
                    }
                }
            )
        }

        FloatingActionButton(
            onClick = { importLauncher.launch(arrayOf("*/*")) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = Color(0xFF440020),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp) // optional, makes it pill-shaped
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    Icons.Default.FileOpen,
                    contentDescription = stringResource(R.string.cd_import_key)
                )
                Text(text = "Import .asc")
            }
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
