package dev.stralman.data

data class Profile(
    val name: String,
    val shortDescription: String,
    val secondaryTextColor: String,
    val imageResource: String,
    val imageBorderColor: String,
    val linkList: List<RouteLink> = emptyList(),
    val socialLinkList: List<SocialLink> = emptyList(),
)

data class RouteLink(
    val id: String,
    val url: String,
)

data class SocialLink(
    val id: String,
    val url: String,
    val title: String,
    val iconSvg: String
)
