package dev.stralman.components.widgets.image

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.alignContent
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.objectFit
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import org.jetbrains.compose.web.css.AlignContent
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

val RoundBorderImageStyle by ComponentStyle {
    base {
        Modifier
            .clip(Circle())
            .alignContent(AlignContent.Center)
            .objectFit(ObjectFit.Cover)
            .border(5.px, LineStyle.Solid, Color("#42414d"))
            .borderRadius(50.percent)
    }
    hover {
        Modifier
            .border(5.px, LineStyle.Solid, Color("#6c757d"))
    }
}

@Composable
fun RoundBorderImage(
    src: String,
    color: String,
    modifier: Modifier = Modifier,
) {
    Image(
        src,
        modifier = RoundBorderImageStyle
            .toModifier()
            .then(modifier)
    )
}