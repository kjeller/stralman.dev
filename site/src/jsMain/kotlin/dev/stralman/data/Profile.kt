package dev.stralman.data

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier

data class Profile(
    val name: String,
    val shortDescription: String,
    val imageResource: String,
    val linkList: List<RouteLink> = emptyList(),
    val socialLinkList: List<FaIconLink> = emptyList(),
    val pageSourceUrl: String
)

data class RouteLink(
    val id: String,
    val url: String,
)

data class FaIconLink(
    val url: String,
    val faIcon: @Composable (modifier: Modifier) -> Unit,
)
