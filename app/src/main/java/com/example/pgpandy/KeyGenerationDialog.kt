package com.example.pgpandy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/** Data container for the key generation form fields. */
data class KeyFormData(
    val label: String,
    val name: String,
    val email: String,
    val algorithm: String,
    val bitLength: Int,
    val password: String,
    val confirmPassword: String,
    val notes: String
)

/** Dialog containing form fields for generating a new PGP key pair. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyGenerationDialog(
    onDismiss: () -> Unit,
    onCreate: (KeyFormData) -> Unit = {}
) {
    var label by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var algorithm by remember { mutableStateOf("RSA") }
    var algorithmExpanded by remember { mutableStateOf(false) }
    var bitLength by remember { mutableStateOf(2048) }
    var bitExpanded by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val algorithms = listOf("RSA", "DSA", "ECDSA", "EdDSA")
    val bitOptions = listOf(2048, 3072, 4096)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onCreate(
                    KeyFormData(
                        label,
                        name,
                        email,
                        algorithm,
                        bitLength,
                        password,
                        confirmPassword,
                        notes
                    )
                )
                onDismiss()
            }) {
                Text(stringResource(R.string.action_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
        title = { Text(stringResource(R.string.title_generate_key)) },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.label_key_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.label_email)) },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = algorithmExpanded,
                    onExpandedChange = { algorithmExpanded = !algorithmExpanded }
                ) {
                    OutlinedTextField(
                        value = algorithm,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_algorithm)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = algorithmExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = algorithmExpanded,
                        onDismissRequest = { algorithmExpanded = false }
                    ) {
                        algorithms.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    algorithm = option
                                    algorithmExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = bitExpanded,
                    onExpandedChange = { bitExpanded = !bitExpanded }
                ) {
                    OutlinedTextField(
                        value = bitLength.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_bit_length)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = bitExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = bitExpanded,
                        onDismissRequest = { bitExpanded = false }
                    ) {
                        bitOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toString()) },
                                onClick = {
                                    bitLength = option
                                    bitExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.label_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.label_confirm_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.label_notes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
