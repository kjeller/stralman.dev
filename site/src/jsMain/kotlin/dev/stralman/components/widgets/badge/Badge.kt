package dev.stralman.components.widgets.badge

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.minWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun BadgeSpan(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Span(
        modifier
            .borderRadius(0.25.cssRem)
            .padding(2.px)
            .backgroundColor(Color.white)
            .toAttrs()
    ) {
        content()
    }
}

@Composable
fun BadgeContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BadgeSpan(
        modifier = modifier.margin(right = 15.px)
    ) {
        P(
            modifier
                .fontSize(0.7.cssRem)
                .color(Color("#2b2a33"))
                .fontWeight(FontWeight.Bold)
                .fillMaxWidth()
                .maxWidth(400.px)
                .minWidth(80.px)
                .margin(0.px)
                .padding(0.px)
                .textAlign(TextAlign.Center)
                .toAttrs()
        ) {
            content()
        }
    }
}
