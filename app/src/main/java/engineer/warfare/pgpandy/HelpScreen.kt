package engineer.warfare.pgpandy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HelpScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Help", modifier = Modifier.align(Alignment.Center))
    }
}
