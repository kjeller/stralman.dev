package dev.stralman.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.gridRow
import com.varabyte.kobweb.compose.ui.modifiers.gridTemplateRows
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.silk.components.icons.fa.FaGithub
import com.varabyte.kobweb.silk.components.icons.fa.FaLinkedin
import com.varabyte.kobweb.silk.components.icons.fa.FaRss
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toModifier
import dev.stralman.components.sections.Footer
import dev.stralman.components.sections.ProfileHeader
import dev.stralman.data.FaIconLink
import dev.stralman.data.Profile
import dev.stralman.data.RouteLink
import kotlinx.browser.document
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.fr
import org.jetbrains.compose.web.css.percent

val PageContentStyle by ComponentStyle {
    base {
        Modifier.fillMaxSize()
    }
    Breakpoint.MD { Modifier.maxWidth(40.cssRem) }
}

val FaIconStyle by ComponentStyle {
    base {
        Modifier
            .color(Color("#6c757d"))
    }
    hover {
        Modifier
            .color(Color("#ffffff"))
    }
}

@Composable
fun PageLayout(content: @Composable ColumnScope.() -> Unit) {
    document.title = "Karl Strålman"
    val profile = Profile(
        "Kalle Strålman ☢\uFE0F",
        "A tech hobbyist's guide to the galaxy.",
        secondaryTextColor = "#6c757d",
        "/profile.png",
        imageBorderColor = "#42414d",
        linkList = listOf(
            RouteLink(
                "About",
                "/about/",
            ),
            RouteLink(
                "Tags",
                "/tags/"
            )
        ),
        socialLinkList = listOf(
            FaIconLink(
                "https://github.com/kjeller"
            ) { FaGithub(FaIconStyle.toModifier()) },
            FaIconLink(
                "https://www.linkedin.com/in/karl-str%C3%A5lman-422b6b173/"
            ) { FaLinkedin(FaIconStyle.toModifier()) },
            FaIconLink(
                "index.xml"
            ) { FaRss(FaIconStyle.toModifier()) }
        )
    )

    Box(
        Modifier
            .fillMaxWidth()
            .minHeight(100.percent)
            // Create a box with two rows: the main content (fills as much space as it can) and the footer (which reserves
            // space at the bottom). "min-content" means the use the height of the row, which we use for the footer.
            // Since this box is set to *at least* 100%, the footer will always appear at least on the bottom but can be
            // pushed further down if the first row grows beyond the page.
            // Grids are powerful but have a bit of a learning curve. For more info, see:
            // https://css-tricks.com/snippets/css/complete-guide-grid/
            .gridTemplateRows {
                size(1.fr)
                size(minContent)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            // Isolate the content, because otherwise the absolute-positioned SVG above will render on top of it.
            // This is confusing but how browsers work. Read up on stacking contexts for more info.
            // https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_positioned_layout/Understanding_z-index/Stacking_context
            // Some people might have used z-index instead, but best practice is to avoid that if possible, because
            // as a site gets complex, Z-fighting can be a huge pain to track down.
            Modifier.fillMaxSize().gridRow(1),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileHeader(profile)
            Column(
                PageContentStyle.toModifier(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
        // Associate the footer with the row that will get pushed off the bottom of the page if it can't fit.
        Footer(Modifier.gridRow(2))
    }
}
