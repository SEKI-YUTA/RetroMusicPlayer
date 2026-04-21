package code.name.monkey.retromusic.util.theme

import androidx.compose.runtime.Composable
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun RetroTheme(content: @Composable () -> Unit) {
    Mdc3Theme {
        content()
    }
}
