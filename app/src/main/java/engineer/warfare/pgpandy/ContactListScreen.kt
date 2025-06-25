package engineer.warfare.pgpandy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ContactListScreen(onAddContact: () -> Unit) {
    val contacts = ContactRepository.contacts
    val context = LocalContext.current

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
                    val imported = PublicKeyImportService(context).importPublicKeys(armored)
                    if (imported == 0) {
                        Toast.makeText(context, context.getString(R.string.msg_no_keys_found), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    val msg = when (e.message) {
                        "private" -> context.getString(R.string.msg_public_key_required)
                        "none" -> context.getString(R.string.msg_no_keys_found)
                        else -> context.getString(R.string.msg_import_failed, e.message ?: "")
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (contacts.isEmpty()) {
            Text(stringResource(R.string.msg_no_contacts), modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(contacts) { contact ->
                    ListItem(
                        headlineContent = { Text(contact.name) },
                        supportingContent = { Text(contact.publicKey) }
                    )
                    Divider()
                }
            }
        }

        FloatingActionButton(
            onClick = onAddContact,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFF440020),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.cd_add_contact))
        }

        FloatingActionButton(
            onClick = { importLauncher.launch(arrayOf("*/*")) },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            containerColor = Color(0xFF440020),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.FileOpen, contentDescription = stringResource(R.string.cd_import_key))
        }
    }
}

// Simple data structures kept here for now
data class Contact(val name: String, val publicKey: String)

object ContactRepository {
    val contacts = mutableStateListOf<Contact>()
}
