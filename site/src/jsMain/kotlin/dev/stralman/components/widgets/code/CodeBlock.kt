package dev.stralman.components.widgets.code

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.components.style.toModifier
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.dom.Code
import org.jetbrains.compose.web.dom.Pre
import org.jetbrains.compose.web.dom.Text

// bitspittle https://github.com/bitspittle/bitspittle.dev/blob/4fbdf7d3774b065a9106afd6241464f6aa6b095b/site/src/jsMain/kotlin/dev/bitspittle/site/components/widgets/code/CodeBlock.kt#L17
val CodeBlockStyle by ComponentStyle(prefix = "bs") {
    // For some reason I'm not smart enough to figure out, code blocks are messing up the layout on mobile - they lay
    // themselves out too wide and break out of the central column. Here, we just constrain them to whatever the
    // view width is, until we are on desktop and the column is no longer so small as to cause an issue.
    base { Modifier.maxWidth(90.vw).fillMaxWidth() }
    Breakpoint.MD { Modifier.maxWidth(100.percent) }
}

@Composable
fun CodeBlock(text: String, modifier: Modifier = Modifier, lang: String? = null) {
    Pre(CodeBlockStyle.toModifier().then(modifier).toAttrs()) {
        Code(attrs = {
            classes(lang?.let { "language-$it" } ?: "nohighlight")
        }) {
            Text(text)
        }
    }
}