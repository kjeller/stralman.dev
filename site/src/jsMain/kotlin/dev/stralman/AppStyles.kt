package dev.stralman

import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.letterSpacing
import com.varabyte.kobweb.compose.ui.modifiers.lineHeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.scrollBehavior
import com.varabyte.kobweb.silk.components.layout.HorizontalDividerStyle
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.MutablePalette
import com.varabyte.kobweb.silk.theme.colors.palette.Palette
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.border
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.link
import com.varabyte.kobweb.silk.theme.modifyComponentStyleBase
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px

const val COLOR_MODE_KEY = "stralman:colorMode"

private val BLOCK_MARGIN = Modifier.margin(top = 1.cssRem)
private val HEADER_MARGIN = Modifier.margin(top = 2.cssRem)
val FaIconStyle by ComponentStyle {
    base {
        Modifier
            .color(Color.rgb(0x6c757d))
    }
    hover {
        Modifier
            .color(Color.rgb(0xffffff))
    }
}

@InitSilk
fun initSiteStyles(ctx: InitSilkContext) {
    ctx.config.initialColorMode =
        localStorage.getItem(COLOR_MODE_KEY)?.let { ColorMode.valueOf(it) } ?: ColorMode.DARK
    ctx.apply {
        stylesheet.apply {
            registerStyleBase("html") {
                // Always show a vertical scroller, or else our page content shifts when switching from one page that
                // can scroll to one that can't
                Modifier
                    .scrollBehavior(ScrollBehavior.Smooth)
                    .overflow { y(Overflow.Scroll) }
            }
            registerStyleBase("body") {
                Modifier
                    .fontFamily(
                        "Ubuntu", "Roboto", "Arial", "Helvetica", "sans-serif"
                    )
                    .fontSize(18.px)
                    .lineHeight(1.5)
            }
            registerStyleBase("code") {
                Modifier
                    .fontFamily(
                        "Ubuntu Mono", "Roboto Mono", "Lucida Console", "Courier New", "monospace"
                    )
                    .fontSize(18.px)
                    .lineHeight(1.5)
            }
            registerStyleBase("canvas") { BLOCK_MARGIN }
            registerStyleBase("p") { BLOCK_MARGIN }
            registerStyleBase("pre") { BLOCK_MARGIN }
            registerStyleBase("h1") {
                HEADER_MARGIN
                    .fontSize(2.cssRem)
                    .letterSpacing((-1.5).px)
                    .lineHeight(1.1)
            }
            registerStyleBase("h2") { HEADER_MARGIN.fontSize(2.cssRem) }
            registerStyleBase("h3") { HEADER_MARGIN.fontSize(1.5.cssRem) }
            registerStyleBase("h4") { HEADER_MARGIN.fontSize(1.25.cssRem) }

            /*base {
                Modifier
                    .color(Color("#6c757d"))
            }
            hover {
                Modifier
                    .color(Color("#ffffff"))
            }

             */
        }
        theme.apply {
            modifyComponentStyleBase(HorizontalDividerStyle) {
                Modifier.fillMaxWidth()
            }
            palettes.apply {
                light.apply {
                    color = Colors.Black
                    background = Color.rgb(0x2b2a33).inverted()
                    border = Color.rgb(0x42414d).inverted()
                    secondary = Color.rgb(0x6c757d).inverted()
                    link.visited = ctx.theme.palettes.light.link.default
                }
                dark.apply {
                    color = Colors.White
                    background = Color.rgb(0x2b2a33)
                    border = Color.rgb(0x42414d)
                    secondary = Color.rgb(0x6c757d)
                    link.visited = ctx.theme.palettes.dark.link.default
                }
            }
        }
    }
}

val Palette.secondary get() = (this as MutablePalette).secondary
var MutablePalette.secondary: Color
    get() = this.getValue("secondary")
    set(value) = this.set("secondary", value)
