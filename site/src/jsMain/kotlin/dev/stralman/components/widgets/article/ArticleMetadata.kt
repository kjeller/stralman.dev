package dev.stralman.components.widgets.article

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobwebx.markdown.markdown
import dev.stralman.blogposts.markdownResourceDir
import dev.stralman.components.layouts.BadgeText
import dev.stralman.components.widgets.badge.BadgeContent
import dev.stralman.profile
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

@Composable
fun ArticleMetadata(
    ctx: PageContext,
    colorMode: ColorMode
) {
    val date = ctx.markdown!!.frontMatter.getValue("date").single()
    val author = ctx.markdown!!.frontMatter.getValue("author").single()
    val title = ctx.markdown!!.frontMatter.getValue("title").single()
    val updated = ctx.markdown!!.frontMatter["updated"]?.singleOrNull()
    Column {
        H1 {
            Text(title)
        }
        Row(
            modifier = Modifier.margin(top = 1.cssRem)
        ) {
            BadgeContent {
                Text("Created: $date")
            }
            if (updated != null) {
                BadgeContent {
                    Text("Updated: $updated")
                }
            }
            BadgeContent {
                Link(
                    "${profile.pageSourceUrl}${
                        markdownResourceDir.substringBeforeLast(
                            "/"
                        )
                    }/${ctx.markdown!!.path}",
                    "Edit on Github",
                    modifier = BadgeText.toModifier()
                        .then(Modifier.color(colorMode.toPalette().background))
                )
            }
        }
    }
}
