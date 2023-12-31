package dev.stralman.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.core.Page
import dev.stralman.components.layouts.PageLayout
import dev.stralman.components.widgets.BlogPostList

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
