package dev.stralman.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import dev.stralman.articles.markdownEntries
import dev.stralman.components.layouts.PageLayout

@Page
@Composable
fun TagsPage(
    modifier: Modifier = Modifier
) {
    PageLayout {
        Box {
            Column {
                markdownEntries
                    .flatMap { it.tags }
                    .distinct()
                    .forEach {
                        Link(
                            path = "/",
                            text = it
                        )
                    }
            }
        }
    }
}