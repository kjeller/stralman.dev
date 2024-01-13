package dev.stralman.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.VerticalAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.lineHeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.verticalAlign
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.border
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import dev.stralman.articles.markdownEntries
import dev.stralman.components.layouts.PageLayout
import dev.stralman.components.widgets.badge.BadgeContent
import dev.stralman.secondary
import dev.stralman.util.getShortMonth
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
        .padding(right = 15.px)
        .textAlign(TextAlign.Right)
}

val BlogPostRow by ComponentStyle {
    base {
        Modifier
            .backgroundColor(colorMode.toPalette().background)
            .borderRadius(0.25.cssRem)
            .fillMaxWidth()
            .padding(5.px)
    }
    hover {
        Modifier
            .backgroundColor(colorMode.toPalette().border)
    }
}

@Composable
fun BlogPostList(
    modifier: Modifier = Modifier
) {
    val colorMode by ColorMode.currentState
    Column {
        markdownEntries.forEach {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = BlogPostRow.toModifier()
            ) {
                BadgeContent {
                    Text(
                        "${getShortMonth(it.date.month)} ${
                            it.date.dayOfMonth.toString().padStart(2, '0')
                        } ${it.date.year}"
                    )
                }
                Column {
                    Link(
                        text = it.title,
                        path = it.path,
                        modifier = BlogPostEntry
                            .toModifier()
                            .then(
                                modifier
                                    .textAlign(TextAlign.Left)
                                    .verticalAlign(VerticalAlign.TextTop)
                            )
                    )
                    P(
                        BlogPostEntry.toModifier()
                            .then(
                                modifier
                                    .fontSize(0.7.cssRem)
                                    .textAlign(TextAlign.Left)
                                    .color(colorMode.toPalette().secondary)
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

@Page
@Composable
fun HomePage(
    modifier: Modifier = Modifier
) {
    PageLayout {
        Box {
            BlogPostList(modifier)
        }
    }
}
