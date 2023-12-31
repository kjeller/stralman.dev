package dev.stralman.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.VerticalAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontStyle
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.lineHeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.verticalAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.shapes.Circle
import com.varabyte.kobweb.silk.theme.shapes.clip
import dev.stralman.articles.markdownEntries
import dev.stralman.util.getShortMonth
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

val BlogPostEntry by ComponentStyle.base {
    Modifier
        .fontSize(1.cssRem)
        .margin(0.px)
        .lineHeight(1.2)
        .fillMaxWidth()
        //.width(150.px)
        .padding(right = 15.px)
        .textAlign(TextAlign.Right)
}

@Composable
fun BlogPostList(
    modifier: Modifier = Modifier
) {
    Column {
        markdownEntries.forEach {
            Column(
                modifier = modifier.margin(bottom = 15.px),
                //verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    P(
                        BlogPostEntry.toModifier().then(
                            modifier.fontSize(0.7.cssRem)
                                .color(Color.black)
                                .borderRadius(0.25.cssRem)
                                .padding(3.px)
                                .backgroundColor(Color.white)
                                .fontWeight(FontWeight.Bold)
                                .margin(right = 15.px)
                        ).toAttrs()
                    ) {
                        Text("${getShortMonth(it.date.month)} ${it.date.dayOfMonth} ${it.date.year}")
                    }
                    Column {
                        Link(
                            text = it.title,
                            path = it.path,
                            modifier = BlogPostEntry
                                .toModifier()
                                .then(
                                    modifier
                                        .width(400.px)
                                        .textAlign(TextAlign.Left)
                                        .verticalAlign(VerticalAlign.TextTop)
                                )
                        )
                        P(
                            BlogPostEntry.toModifier()
                                .then(
                                    modifier
                                        .fontSize(0.7.cssRem)
                                        .width(400.px)
                                        .textAlign(TextAlign.Left)
                                        .color(Color("#6c757d"))
                                    //.padding(left = 150.px)
                                )
                                .toAttrs()
                        ) {
                            Text(it.tags.joinToString(", "))
                        }
                    }
                }
            }
        }
    }
}