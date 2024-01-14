package dev.stralman.components.widgets.image

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.objectFit
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.palette.border
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

val RoundBorderImageStyle by ComponentStyle {
    base {
        Modifier
            .clip(Circle())
            .objectFit(ObjectFit.Cover)
            .border(5.px, LineStyle.Solid, colorMode.toPalette().border)
            .borderRadius(50.percent)
    }
    hover {
        val color = when (colorMode.isDark) {
            true -> colorMode.toPalette().border.lightened(0.3f)
            false -> colorMode.toPalette().border.darkened(0.3f)
        }
        Modifier
            .border(5.px, LineStyle.Solid, color)
    }
}

@Composable
fun RoundBorderImage(
    src: String,
    modifier: Modifier = Modifier,
) {
    Image(
        src,
        modifier = modifier
            .then(RoundBorderImageStyle.toModifier())
    )
}