package com.example.pgpandy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ContactListScreen(onAddContact: () -> Unit) {
    val contacts = ContactRepository.contacts

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
    }
}

// Simple data structures kept here for now
data class Contact(val name: String, val publicKey: String)

object ContactRepository {
    val contacts = mutableStateListOf<Contact>()
}
