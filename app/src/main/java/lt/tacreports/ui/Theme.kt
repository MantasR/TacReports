package lt.tacreports.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import lt.tacreports.R

/** "Battleship" tactical palette, shared with SimpleGrid: near-black teal, glowing cyan, amber for tools. */
object Tac {
    val Bg = Color(0xFF020B0D)
    val Panel = Color(0xF2041719)
    val Field = Color(0xFF071F22)
    val Cyan = Color(0xFF2EE6D0)
    val CyanDim = Color(0xFF178A7E)
    val CyanFaint = Color(0x402EE6D0)
    val Text = Color(0xFFCFFFF8)
    val TextDim = Color(0xFF6FA8A0)
    val Amber = Color(0xFFFFB000)
    val Red = Color(0xFFFF4D4D)
    val Scrim = Color(0xB3000000)
}

/** Chakra Petch and JetBrains Mono both cover the Lithuanian letters. */
val Chakra = FontFamily(
    Font(R.font.chakra_petch_medium, FontWeight.Medium),
    Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val Mono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.jetbrains_mono, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

object TacType {
    val Label = TextStyle(fontFamily = Chakra, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.2.sp, color = Tac.TextDim)
    val Title = TextStyle(fontFamily = Chakra, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 3.sp, color = Tac.Cyan)
    val Body = TextStyle(fontFamily = Chakra, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Tac.Text)
    val Value = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Normal, fontSize = 15.sp, color = Tac.Text)
    val Small = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = Tac.TextDim)
    val Button = TextStyle(fontFamily = Chakra, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.5.sp)
}

@Composable
fun TacTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Tac.Cyan,
            onPrimary = Tac.Bg,
            background = Tac.Bg,
            surface = Tac.Panel,
            onSurface = Tac.Text,
        ),
        content = content,
    )
}
