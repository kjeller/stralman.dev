package dev.stralman.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.textDecorationLine
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.navigation.Anchor
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.style.toModifier
import dev.stralman.HeadlineTextStyle
import dev.stralman.PTextStyle
import dev.stralman.components.widgets.RoundBorderImage
import dev.stralman.components.widgets.SocialLinkIcon
import dev.stralman.data.Profile
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun ProfileHeader(
    profile: Profile,
    profileImageHref: String = "/",
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(50.px)
        ) {
            Anchor(
                href = profileImageHref
            ) {
                RoundBorderImage(
                    src = profile.imageResource,
                    color = profile.imageBorderColor,
                    modifier = modifier.size(120.px)
                )
            }
            Column(
                modifier = modifier
                    .margin(0.px)
                    .padding(20.px)
            ) {
                H1(
                    HeadlineTextStyle.toModifier().toAttrs()
                ) {
                    Text(profile.name)
                }
                P(
                    PTextStyle
                        .toModifier()
                        .color(Color(profile.secondaryTextColor))
                        .toAttrs()
                ) {
                    Text(profile.shortDescription)
                }
                Row(
                    modifier = modifier.padding(top = 20.px, bottom = 15.px)
                ) {
                    profile.linkList.forEach {
                        Link(
                            path = it.url,
                            text = it.id,
                            modifier = PTextStyle
                                .toModifier()
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
                        SocialLinkIcon(it)
                    }
                }
            }
        }
    }
}