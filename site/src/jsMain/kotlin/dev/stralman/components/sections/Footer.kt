package dev.stralman.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.UncoloredLinkVariant
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.style.vars.color.ColorVar
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.border
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.Span
import dev.stralman.toSitePalette
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px

val FooterStyle by ComponentStyle.base {
    Modifier
        .margin(top = 2.cssRem)
        .borderTop(1.px, LineStyle.Solid, colorMode.toPalette().border)
        .padding(topBottom = 1.cssRem, leftRight = 4.cssRem)
        .fontSize(0.8.cssRem)
        .fontFamily("monospace")
}

val CopyrightStyle = ComponentStyle.base("bs-copyright") {
    Modifier.opacity(0.6).fontSize(0.8.cssRem)
}


@Composable
fun Footer(modifier: Modifier = Modifier) {
    Box(FooterStyle.toModifier().then(modifier), contentAlignment = Alignment.Center) {
        Span(Modifier.textAlign(TextAlign.Center).toAttrs()) {
            val sitePalette = ColorMode.current.toSitePalette()
            Row {
                Span(
                    Modifier.whiteSpace(WhiteSpace.PreWrap).textAlign(TextAlign.Center).toAttrs()
                ) {
                    SpanText("Powered by ")
                    Link(
                        "https://github.com/varabyte/kobweb",
                        "Kobweb",
                        Modifier.setVariable(ColorVar, sitePalette.brand.primary),
                        variant = UncoloredLinkVariant
                    )
                    SpanText(", site source ")

                    Link(
                        "https://github.com/kjeller/stralman.dev",
                        "here",
                        Modifier
                            .setVariable(ColorVar, sitePalette.brand.primary)
                            .whiteSpace(WhiteSpace.NoWrap)
                            .fontFamily("monospace"),
                        variant = UncoloredLinkVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                SpanText(
                    "© 2023, Karl Strålman",
                    CopyrightStyle
                        .toModifier()
                        .then(
                            modifier
                                .color(Color("#6c757d"))
                        )
                )
            }
        }
    }
}
