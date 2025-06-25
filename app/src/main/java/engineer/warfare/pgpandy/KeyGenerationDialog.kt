package engineer.warfare.pgpandy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext

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
    var label by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var algorithm by rememberSaveable { mutableStateOf("RSA") }
    var algorithmExpanded by remember { mutableStateOf(false) }
    var bitLength by rememberSaveable { mutableStateOf(2048) }
    var bitExpanded by remember { mutableStateOf(false) }
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    val algorithms = listOf("RSA", "DSA", "ECDSA", "EdDSA")
    val bitOptions = listOf(2048, 3072, 4096)

    val context = LocalContext.current
    val formValid = label.isNotBlank() && (name.isNotBlank() || email.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = formValid,
                onClick = {
                    val data = KeyFormData(
                        label,
                        name,
                        email,
                        algorithm,
                        bitLength,
                        password,
                        confirmPassword,
                        notes
                    )
                    KeyGenerationService(context).generateAndStore(data)
                    onCreate(data)
                    onDismiss()
                }
            ) {
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
            Column(modifier = Modifier.padding(top = 8.dp).verticalScroll(rememberScrollState())) {
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { advancedExpanded = !advancedExpanded }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_advanced),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (advancedExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }

                if (advancedExpanded) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = algorithmExpanded,
                                onExpandedChange = { algorithmExpanded = !algorithmExpanded },
                                modifier = Modifier.weight(1f).padding(end = 4.dp)
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
                                onExpandedChange = { bitExpanded = !bitExpanded },
                                modifier = Modifier.weight(1f).padding(start = 4.dp)
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
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text(stringResource(R.string.label_password)) },
                                modifier = Modifier.weight(1f).padding(end = 4.dp),
                                visualTransformation = PasswordVisualTransformation()
                            )
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text(stringResource(R.string.label_confirm_password)) },
                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(stringResource(R.string.label_notes)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}
