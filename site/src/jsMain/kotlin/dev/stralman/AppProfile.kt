package dev.stralman

import com.varabyte.kobweb.silk.components.icons.fa.FaGithub
import com.varabyte.kobweb.silk.components.icons.fa.FaLinkedin
import com.varabyte.kobweb.silk.components.icons.fa.FaRss
import com.varabyte.kobweb.silk.components.style.toModifier
import dev.stralman.components.sections.FaIconStyle
import dev.stralman.data.FaIconLink
import dev.stralman.data.Profile
import dev.stralman.data.RouteLink

val profile = Profile(
    "Kalle Strålman ☢\uFE0F",
    "A tech hobbyist's guide to the galaxy.",
    "/profile.png",
    linkList = listOf(
        RouteLink(
            "Posts",
            "/",
        ),
        RouteLink(
            "About",
            "/about/",
        ),
    ),
    socialLinkList = listOf(
        FaIconLink(
            "https://github.com/kjeller"
        ) { FaGithub(FaIconStyle.toModifier()) },
        FaIconLink(
            "https://www.linkedin.com/in/karl-str%C3%A5lman-422b6b173/"
        ) { FaLinkedin(FaIconStyle.toModifier()) },
        FaIconLink(
            "/index.xml"
        ) { FaRss(FaIconStyle.toModifier()) }
    ),
    pageSourceUrl = "https://github.com/kjeller/stralman.dev/"
)
