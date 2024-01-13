package dev.stralman.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.textDecorationLine
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.navigation.Anchor
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toAttrs
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import dev.stralman.components.widgets.image.RoundBorderImage
import dev.stralman.data.Profile
import dev.stralman.secondary
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

val PSecondaryStyle by ComponentStyle.base {
    Modifier
        .margin(0.px)
        .fontSize(14.px)
        .color(colorMode.toPalette().secondary)
}

@Composable
fun ProfileHeader(
    profile: Profile,
    profileImageHref: String = "/",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.px),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(bottom = 0.px)
        ) {
            Anchor(
                href = profileImageHref
            ) {
                RoundBorderImage(
                    src = profile.imageResource,
                    modifier = modifier.size(120.px)
                )
            }
            Column(
                modifier = modifier
                    .margin(0.px)
                    .padding(10.px)
            ) {
                H1 {
                    Text(profile.name)
                }
                P(PSecondaryStyle.toAttrs()) {
                    Text(profile.shortDescription)
                }
                Row(
                    modifier = modifier.padding(top = 5.px, bottom = 15.px)
                ) {
                    profile.linkList.forEach {
                        Link(
                            path = it.url,
                            text = it.id,
                            modifier = Modifier
                                .fontSize(14.px)
                                .padding(right = 20.px)
                                .textDecorationLine(TextDecorationLine.Underline)
                        )
                    }
                }
                Row(
                    modifier = modifier.width(100.px),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    profile.socialLinkList.forEach {
                        Anchor(
                            href = it.url
                        ) {
                            it.faIcon()
                        }
                    }
                }
            }
        }
    }
}