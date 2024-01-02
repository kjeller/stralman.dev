package dev.stralman.components.widgets.image

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.navigation.Anchor
import com.varabyte.kobweb.silk.components.style.toModifier
import dev.stralman.PTextStyle
import dev.stralman.data.SocialLink
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.Img

@Composable
fun AnchorImage(
    link: SocialLink,
    modifier: Modifier = Modifier
) {
    Anchor(
        attrs = {
            target(ATarget.Blank)
        },
        href = link.url
    ) {
        Img(
            attrs = PTextStyle
                .toModifier()
                .toAttrs(),
            src = link.iconSvg
        )
    }
}
