package dev.stralman.pages.misc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import dev.stralman.components.layouts.PageLayout
import dev.stralman.components.widgets.badge.BadgeContent
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun CircleSegmentCalculation(
    modifier: Modifier = Modifier,
    title: String,
    descriptionText: String,
    radiusText: String,
    invalidRadiusText: String,
    heightText: String,
    lengthText: String,
    calculateButtonText: String,
) {
    var validInput by remember { mutableStateOf(true) }
    var height by remember { mutableStateOf("40") }
    var length by remember { mutableStateOf("70") }
    val calcRadius: (Float?, Float?) -> Float = { h, l ->
        if (h != null && l != null) {
            validInput = true
            (l.pow(2) + 4 * h.pow(2)) / (8 * h)
        } else {
            validInput = false
            0f
        }
    }
    var rad by remember {
        mutableStateOf(calcRadius(height.toFloatOrNull(), length.toFloatOrNull()))
    }
    H1 {
        Text(title)
    }
    // TODO Add descriptive image
    P {
        Text(descriptionText)
    }
    P {
        Text("$heightText:")
    }
    TextInput(
        height, onTextChange = { height = it },
        modifier.fillMaxWidth()
    )
    P {
        Text("$lengthText:")
    }
    TextInput(
        length, onTextChange = { length = it },
        modifier.fillMaxWidth()
    )
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = { rad = calcRadius(height.toFloatOrNull(), length.toFloatOrNull()) }
    ) {
        Text(calculateButtonText)
    }

    if (validInput) {
        BadgeContent(
            modifier = modifier
                .fontSize(20.px)
                .fillMaxWidth()
        ) {
            Text("$radiusText: ${rad.roundToInt()}mm")
        }
    } else {
        BadgeContent(
            modifier = modifier
                .fontSize(20.px)
                .backgroundColor(Color("#d6665e2"))
                .fillMaxWidth()
        ) {
            Text(invalidRadiusText)
        }
    }
}

@Page
@Composable
fun WoodworkingPage(
    modifier: Modifier = Modifier
) {
    PageLayout {
        Box {
            Column {
                CircleSegmentCalculation(
                    modifier = modifier,
                    title = "Beräkna radie för cirkelsegment",
                    descriptionText =
                    """
                    |Mata in höjd och bredd (längd) på segmentet för att få ut radien avrundad till närmaste heltal.
                    |Notera: Höjden måste vara mindre än hälften av bredden
                    """.trimMargin(),
                    radiusText = "Radie",
                    invalidRadiusText = "Felaktig inmatning",
                    heightText = "Båghöjd (mm)",
                    lengthText = "Bågbredd (mm)",
                    calculateButtonText = "Beräkna",
                )
            }
        }
    }
}