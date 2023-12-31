import com.varabyte.kobweb.common.path.invariantSeparatorsPath
import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import com.varabyte.kobwebx.gradle.markdown.yamlStringToKotlinString
import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.CustomBlock

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
}

group = "dev.stralman"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("Powered by Kobweb")
        }
    }
}
class MarkdownVisitor : AbstractVisitor() {
    private val _frontMatter = mutableMapOf<String, List<String>>()
    val frontMatter: Map<String, List<String>> = _frontMatter

    override fun visit(customBlock: CustomBlock) {
        if (customBlock is YamlFrontMatterBlock) {
            val yamlVisitor = YamlFrontMatterVisitor()
            customBlock.accept(yamlVisitor)
            _frontMatter.putAll(
                yamlVisitor.data
                    .mapValues { (_, values) ->
                        values.map { it.yamlStringToKotlinString() }
                    }
            )
        }
    }
}

data class MarkdownEntry(
    val file: File,
    val date: String?,
    val title: String? = null,
    val author: String? = null,
    val tags: List<String>? = emptyList()
)

fun String.escapeQuotes() = this.replace("\"", "\\\"")

val markdownResourceDir = layout.projectDirectory.dir("src/jsMain/resources/markdown/posts")
val generateMarkdownEntriesTask = task("generateMarkdownEntries") {
    val group = "dev/stralman"
    val genDir = layout.buildDirectory.dir("generated/$group/src/jsMain/kotlin").get()

    inputs.dir(markdownResourceDir)
        .withPropertyName("markdownEntries")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(genDir)
        .withPropertyName("generatedMarkdownEntryData")

    doLast {
        val parser = kobweb.markdown.features.createParser()
        val markdownEntries: List<MarkdownEntry> =
            markdownResourceDir.asFileTree.filter { it.extension == "md" }.map {
                val visitor = MarkdownVisitor()
                parser
                    .parse(it.readText())
                    .accept(visitor)
                val fm = visitor.frontMatter
                MarkdownEntry(
                    file = it,
                    title = fm["title"]?.firstOrNull(),
                    author = fm["author"]?.firstOrNull(),
                    tags = fm["tags"],
                    date = fm["date"]?.firstOrNull()
                )
            }
        genDir.file("articles.kt").asFile.apply {
            parentFile.mkdirs()
            writeText(buildString {
                appendLine(
                    """
					|// This file is generated. Modify the build script if you need to change it.
					|
					|package dev.stralman.articles
					|
					|import dev.stralman.data.MarkdownEntry
                    |import kotlinx.datetime.toLocalDate
					|
					|val markdownEntries = listOf${if (markdownEntries.isEmpty()) "<MarkdownEntry>" else ""}(
                    """.trimMargin()
                )
                markdownEntries.sortedByDescending { it.date }.forEach { entry ->
                    appendLine(
                        """ MarkdownEntry(
                            |       path = "/posts${
                            entry
                                .file
                                .path
                                .substringAfterLast("posts")
                                .substringBefore("\\index.md")
                                .substringBefore(".md")
                                .lowercase()
                                .invariantSeparatorsPath
                        }",
                            |       date = "${entry.date?.ifEmpty { "1970-01-01" }}".toLocalDate(),
                            |       title = "${entry.title}",
                            |       author = "${entry.author}",
                            |       tags = ${if (entry.tags.isNullOrEmpty()) "emptyList()" else "listOf<String>(${entry.tags.joinToString { "\"$it\"" }})"}
							|   ),
						""".trimMargin()
                    )
                }
                appendLine(")")
            })
            println("Generated ${absolutePath}")
        }
    }
}

kotlin {
    // This example is frontend only. However, for a fullstack app, you can uncomment the includeServer parameter
    // and the `jvmMain` source set below.
    configAsKobwebApplication("stralman" /*, includeServer = true*/)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation(libs.kobweb.core)
                implementation(libs.kobweb.silk)
                // This default template uses built-in SVG icons, but what's available is limited.
                // Uncomment the following if you want access to a large set of font-awesome icons:
                // implementation(libs.silk.icons.fa)
                implementation(libs.kobwebx.markdown)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }

            kotlin.srcDir(generateMarkdownEntriesTask)
        }

        // Uncomment the following if you pass `includeServer = true` into the `configAsKobwebApplication` call.
//        val jvmMain by getting {
//            dependencies {
//                compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
//            }
//        }
    }
}

