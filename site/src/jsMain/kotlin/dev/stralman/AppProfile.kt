package dev.stralman

import com.varabyte.kobweb.silk.components.icons.fa.FaGithub
import com.varabyte.kobweb.silk.components.icons.fa.FaLinkedin
import com.varabyte.kobweb.silk.components.icons.fa.FaRss
import dev.stralman.data.FaIconLink
import dev.stralman.data.Profile
import dev.stralman.data.RouteLink

val profile = Profile(
    "Kalle Strålman ☢\uFE0F",
    "A tech hobbyist's guide to the galaxy.",
    "/profile.png",
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
        ) { FaGithub() },
        FaIconLink(
            "https://www.linkedin.com/in/karl-str%C3%A5lman-422b6b173/"
        ) { FaLinkedin() },
        FaIconLink(
            "index.xml"
        ) { FaRss() }
    ),
    pageSourceUrl = "https://github.com/kjeller/stralman.dev/tree/kobweb-wip/"
)
