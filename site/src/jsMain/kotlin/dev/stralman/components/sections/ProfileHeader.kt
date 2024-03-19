package dev.stralman.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.navigation.Anchor
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toAttrs
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import dev.stralman.components.widgets.image.RoundBorderImage
import dev.stralman.profile
import dev.stralman.secondary
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

val PSecondaryStyle by ComponentStyle.base {
    Modifier
        .margin(0.px, 0.px, 0.px, 0.px)
        .fontSize(14.px)
        .color(colorMode.toPalette().secondary)
}
val FaIconStyle by ComponentStyle {
    base {
        Modifier
            .color(Color.rgb(0x6c757d))
            .fontSize(14.px)
    }
    hover {
        Modifier
            .color(Color.rgb(0xffffff))
    }
}

@Composable
fun ProfileHeader(
    profileImageHref: String = "/",
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(20.px)
    ) {
        Anchor(
            href = profileImageHref
        ) {
            RoundBorderImage(
                src = profile.imageResource,
                modifier = modifier.size(120.px)
            )
        }
        Div {
            Column(
                modifier = modifier
                    .padding(left = 10.px),
            ) {
                H1(
                    attrs = modifier.margin(0.px).padding(0.px).toAttrs()
                ) {
                    Text(profile.name)
                }
                P(PSecondaryStyle.toAttrs()) {
                    Text(profile.shortDescription)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier.width(85.px)
                ) {
                    profile.linkList.forEach {
                        Link(
                            path = it.url,
                            text = it.id,
                            modifier = Modifier
                                .fontSize(14.px)
                        )
                    }
                }
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .width(75.px),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    profile.socialLinkList.forEach {
                        A(
                            href = it.url
                        ) {
                            it.faIcon(FaIconStyle.toModifier())
                        }
                    }
                }
            }
        }
    }
}
