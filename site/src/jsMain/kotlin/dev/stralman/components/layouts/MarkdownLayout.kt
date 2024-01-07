package dev.stralman.components.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.OverflowWrap
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.display
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.lineHeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.overflowWrap
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobwebx.markdown.markdown
import dev.stralman.components.widgets.badge.BadgeText
import kotlinx.browser.document
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px

@InitSilk
fun initHighlightJs(ctx: InitSilkContext) {
    // Tweaks to make output from highlight.js look softer / better
    ctx.stylesheet.registerStyleBase("code.hljs") { Modifier.borderRadius(8.px) }
}

val MarkdownStyle by ComponentStyle {
    // The following rules apply to all descendant elements, indicated by the leading space.
    // When you use `cssRule`, the name of this style is prefixed in front of it.
    // See also: https://developer.mozilla.org/en-US/docs/Web/CSS/Descendant_combinator

    cssRule(" h1") {
        Modifier
            .fontSize(2.cssRem)
            .fontWeight(300)
            .margin(bottom = 1.5.cssRem)
            .lineHeight(1.2) //1.5x doesn't look as good on very large text
    }

    cssRule(" h2") {
        Modifier
            .fontSize(2.cssRem)
            .fontWeight(300)
            .margin(topBottom = 1.cssRem)
    }

    cssRule(" h3") {
        Modifier
            .fontSize(1.4.cssRem)
            .fontWeight(300)
            .margin(topBottom = 1.5.cssRem)
    }

    cssRule(" h4") {
        Modifier
            .fontSize(1.2.cssRem)
            .fontWeight(FontWeight.Bolder)
            .margin(top = 1.cssRem, bottom = 0.5.cssRem)
    }

    cssRule(" p") {
        Modifier.margin(bottom = 0.8.cssRem)
    }

    cssRule(" ul") {
        Modifier.fillMaxWidth().overflowWrap(OverflowWrap.BreakWord)
    }

    cssRule(" li,ol,ul") {
        Modifier.margin(bottom = 0.25.cssRem)
    }

    cssRule(" code") {
        Modifier
            .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.8f))
            .fontWeight(FontWeight.Bolder)
    }

    cssRule(" pre") {
        Modifier
            .margin(top = 0.5.cssRem, bottom = 2.cssRem)
            .fillMaxWidth()
    }
    cssRule(" pre > code") {
        Modifier
            .display(DisplayStyle.Block)
            .fillMaxWidth()
            .backgroundColor(Color("#373641"))
            .borderRadius(0.25.cssRem)
            .padding(0.5.cssRem)
            .fontSize(1.cssRem)
            .overflow { x(Overflow.Auto) }
    }
    cssRule(" img") {
        Modifier
            .display(DisplayStyle.Block)
            .fillMaxWidth()
            .borderRadius(0.25.cssRem)
            .padding(0.5.cssRem)
            .fontSize(1.cssRem)
    }
}

@Composable
fun MarkdownLayout(content: @Composable () -> Unit) {
    val ctx = rememberPageContext()
    LaunchedEffect(ctx.route) {
        // See kobweb config in build.gradle.kts which sets up highlight.js
        js("hljs.highlightAll()")
    }
    PageLayout {
        val colorMode by ColorMode.currentState
        LaunchedEffect(colorMode) {
            var styleElement = document.querySelector("""link[title="hljs-style"]""")
            if (styleElement == null) {
                styleElement = document.createElement("link").apply {
                    setAttribute("type", "text/css")
                    setAttribute("rel", "stylesheet")
                    setAttribute("title", "hljs-style")
                }.also { document.head!!.appendChild(it) }
            }
            styleElement.setAttribute(
                "href",
                "/highlight.js/styles/a11y-${colorMode.name.lowercase()}.min.css"
            )
        }
        Column(
            MarkdownStyle
                .toModifier()
                .maxWidth(40.cssRem)
        ) {
            val date = ctx.markdown!!.frontMatter.getValue("date").single()
            val author = ctx.markdown!!.frontMatter.getValue("author").single()
            val updated = ctx.markdown!!.frontMatter["updated"]?.singleOrNull()
            Column {
                BadgeText("$date")
                if (updated != null) {
                    BadgeText("Updated: $updated")
                }
            }
            content()
        }
    }
}
