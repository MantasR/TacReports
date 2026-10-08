package lt.tacreports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Corner-bracket frame of the HUD panels: a faint outline plus glowing L-shaped corners. */
fun Modifier.bracketFrame(color: Color = Tac.Cyan, arm: Dp = 10.dp, width: Dp = 1.5.dp): Modifier = drawBehind {
    val a = arm.toPx()
    val corners = Path().apply {
        moveTo(0f, a); lineTo(0f, 0f); lineTo(a, 0f)
        moveTo(size.width - a, 0f); lineTo(size.width, 0f); lineTo(size.width, a)
        moveTo(size.width, size.height - a); lineTo(size.width, size.height); lineTo(size.width - a, size.height)
        moveTo(a, size.height); lineTo(0f, size.height); lineTo(0f, size.height - a)
    }
    drawRect(color.copy(alpha = 0.25f), style = Stroke(1f))
    drawPath(corners, color.copy(alpha = 0.22f), style = Stroke(width.toPx() * 4))
    drawPath(corners, color, style = Stroke(width.toPx()))
}

@Composable
fun TacPanel(modifier: Modifier = Modifier, color: Color = Tac.Cyan, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .background(Tac.Panel)
            .bracketFrame(color)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

/** Bracketed text button. [amber] marks the main action; [filled] a selected state. */
@Composable
fun TacButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    amber: Boolean = false,
    filled: Boolean = false,
    enabled: Boolean = true,
    small: Boolean = false,
) {
    val color = when {
        !enabled -> Tac.CyanDim
        amber -> Tac.Amber
        else -> Tac.Cyan
    }
    Box(
        modifier
            .heightIn(min = if (small) 34.dp else 44.dp)
            .background(if (filled) color else Tac.Panel)
            .bracketFrame(color, arm = if (small) 6.dp else 8.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (small) 10.dp else 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.uppercase(), style = TacType.Button, color = if (filled) Tac.Bg else color)
    }
}

/** Single tap-to-pick option. */
@Composable
fun TacChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .background(if (selected) Tac.Cyan else Tac.Field)
            .border(1.dp, if (selected) Tac.Cyan else Tac.CyanFaint)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(text, style = TacType.Body.copy(fontSize = 14.sp), color = if (selected) Tac.Bg else Tac.Text)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipRow(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
}

/** Text box in the HUD style; [hint] shows while it is empty. */
@Composable
fun TacTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    style: TextStyle = TacType.Value,
    singleLine: Boolean = false,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = style,
        singleLine = singleLine,
        cursorBrush = SolidColor(Tac.Cyan),
        keyboardOptions = KeyboardOptions(capitalization = capitalization),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Tac.Field)
                    .border(1.dp, Tac.CyanFaint)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            ) {
                if (value.text.isEmpty() && hint.isNotEmpty()) Text(hint, style = style.copy(color = Tac.TextDim.copy(alpha = 0.6f)))
                inner()
            }
        },
    )
}

/** Same as [TacTextField] for plain strings. */
@Composable
fun TacTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    style: TextStyle = TacType.Value,
    singleLine: Boolean = false,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = style,
        singleLine = singleLine,
        cursorBrush = SolidColor(Tac.Cyan),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Tac.Field)
                    .border(1.dp, Tac.CyanFaint)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            ) {
                if (value.isEmpty() && hint.isNotEmpty()) Text(hint, style = style.copy(color = Tac.TextDim.copy(alpha = 0.6f)))
                inner()
            }
        },
    )
}

/** Label on the left, a small ON/OFF style toggle on the right. */
@Composable
fun TacToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = TacType.Body, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .background(if (checked) Tac.Cyan else Tac.Field)
                .border(1.dp, if (checked) Tac.Cyan else Tac.CyanDim)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(if (checked) "ON" else "OFF", style = TacType.Button, color = if (checked) Tac.Bg else Tac.TextDim)
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = Tac.TextDim) {
    Text(text.uppercase(), style = TacType.Label, color = color, modifier = modifier)
}

