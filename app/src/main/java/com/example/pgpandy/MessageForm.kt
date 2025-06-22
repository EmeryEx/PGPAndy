package com.example.pgpandy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Composable
fun MessageForm() {
    var recipient by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.title_create_message))
        OutlinedTextField(
            value = recipient,
            onValueChange = { recipient = it },
            label = { Text(stringResource(R.string.label_recipient)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(stringResource(R.string.label_message)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { /* send */ },
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF440020),
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.action_send))
        }
    }
}
