package com.example.pgpandy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ContactForm(onSaved: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.title_pgp_contact))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.label_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = key,
            onValueChange = { key = it },
            label = { Text(stringResource(R.string.label_public_key)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                ContactRepository.contacts.add(Contact(name, key))
                onSaved()
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}
