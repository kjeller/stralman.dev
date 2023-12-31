package dev.stralman.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.justifyContent
import com.varabyte.kobweb.compose.ui.modifiers.lineHeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.stralman.PTextStyle
import dev.stralman.articles.markdownEntries
import dev.stralman.util.getShortMonth
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

val BlogPostTextStyle by ComponentStyle.base {
    Modifier
        .width(50.px)
        .padding(right = 10.px)
        .fontSize(1.cssRem)
        .margin(0.px)
        .lineHeight(1.2)
}

@Composable
fun BlogPostList(
    modifier: Modifier = Modifier
) {
    Column {
        markdownEntries.forEach {
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                P(
                    BlogPostTextStyle.toModifier().then(
                        modifier.width(100.px).justifyContent(
                            JustifyContent.Right
                        )
                    ).toAttrs()
                ) {
                    Text("${getShortMonth(it.date.month)} ${it.date.dayOfMonth} ${it.date.year}")
                }
                Link(
                    text = it.title,
                    path = it.path,
                    modifier = BlogPostTextStyle.toModifier().then(modifier.width(400.px))
                )
            }
        }
    }
}