import com.varabyte.kobweb.common.path.invariantSeparatorsPath
import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import com.varabyte.kobwebx.gradle.markdown.yamlStringToKotlinString
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.script
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

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    }
}

group = "dev.stralman"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("A tech hobbyist's guide to the galaxy.")
            head.add {
                script {
                    // Needed by components/layouts/BlogLayout.kt
                    src = "/highlight.js/highlight.min.js"
                }
            }
        }
    }
    markdown {
        handlers {
            val BS_WGT = "dev.stralman.components.widgets"

            code.set { code ->
                "$BS_WGT.code.CodeBlock(\"\"\"${code.literal.escapeTripleQuotedText()}\"\"\", lang = ${
                    code.info.takeIf { it.isNotBlank() }?.let { "\"$it\"" }
                })"
            }

            inlineCode.set { code ->
                "$BS_WGT.code.InlineCode(\"\"\"${code.literal.escapeTripleQuotedText()}\"\"\")"
            }
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

data class FrontMatterKeys(
    val author: String = "author",
    val date: String = "date",
    val title: String = "title",
    val tags: String = "tags",
)

data class MarkdownData(
    val file: File,
    val date: String?,
    val title: String?,
    val author: String?,
    val tags: List<String>? = emptyList()
)

data class RssData(
    val title: String,
    val baseUrl: String,
    val author: String,
    val description: String,
    val language: String,
    val lastBuildDate: String,
    val copyright: String,
    val generator: String = "Kobweb -- kobweb.varabyte.com",
    val items: List<RssItem> = emptyList(),
) {
    fun toRssString(): String {
        return buildString {
            appendLine(
                """<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
                |<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
                |<channel>
                |   <title>${title}</title>
                |   <link>${baseUrl}</link>
                |   <description>${description}</description>
                |   <generator>${generator}</generator>
                |   <language>${language}</language>
                |   <copyright>${copyright}</copyright>
                |   <lastBuildDate>${lastBuildDate}</lastBuildDate>
                |   <atom:link href="${baseUrl}/index.xml" rel="self" type="application/rss+xml"/>
                """.trimMargin()
            )
            items.forEach {
                appendLine(
                    """<item>
                    |   <title>${it.title}</title>
                    |   <link>${it.link}</link>
                    |   <guid>${it.guid}</guid>
                    |   <pubDate>${it.pubDate}</pubDate>
                    |   <description>${it.description}</description>
                    |</item>
                    """.trimMargin()
                )
            }
            appendLine(
                """
                |</channel>
                |</rss> 
                """.trimMargin()
            )
        }
    }
}

data class RssItem(
    val title: String,
    val link: String,
    val pubDate: String,
    val guid: String,
    val description: String,
)

fun getUrlFromFilePath(file: File) =
    file
        .path
        .substringAfterLast("posts")
        .substringBefore("\\index.md") // Windows
        .substringBefore("/index.md") // Unix
        .substringBefore(".md")
        .lowercase()
        .invariantSeparatorsPath

val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
val months = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

// Hack to convert LocalDateTime to a RFC1123 / RFC822 compatible string
// Switch to official supported lib format / parse when merged in kotlinx-datetime
// Open issue here: https://github.com/Kotlin/kotlinx-datetime/pull/251
fun localDateTimeToRfc1123String(
    date: LocalDateTime,
    timeZone: String = "GMT",
): String {
    val date =
        date.toInstant(TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.of(timeZone))
    return "${days[date.dayOfWeek.value - 1]}, ${
        date.dayOfMonth.toString().padStart(2, '0')
    } ${months[date.month.value - 1]} ${date.year} ${
        date.hour.toString().padStart(2, '0')
    }:${date.minute.toString().padStart(2, '0')}:${
        date.second.toString().padStart(2, '2')
    } $timeZone"
}

// Hack to convert localdate
fun localDateToRfc1123String(date: LocalDate): String =
    "${days[date.dayOfWeek.value - 1]}, ${
        date.dayOfMonth.toString().padStart(2, '0')
    } ${months[date.month.value - 1]} ${date.year} 00:00:00 +0000"

val markdownResourceDir = layout.projectDirectory.dir("src/jsMain/resources/markdown/posts")
val fmk = FrontMatterKeys()
val parser = kobweb.markdown.features.createParser()
val markdownEntries: List<MarkdownData> =
    markdownResourceDir.asFileTree.filter { it.extension == "md" }.map {
        val visitor = MarkdownVisitor()
        parser
            .parse(it.readText())
            .accept(visitor)
        val fm = visitor.frontMatter

        MarkdownData(
            file = it,
            title = fm[fmk.title]?.firstOrNull(),
            author = fm[fmk.author]?.firstOrNull(),
            tags = fm[fmk.tags],
            date = fm[fmk.date]?.firstOrNull()
        )
    }

val copyMarkdownResourcesTask = task("copyMarkdownResources") {
    val genDir = layout.buildDirectory.dir("generated/resources/markdown").get()

    inputs.dir(markdownResourceDir)
        .withPropertyName("markdownEntries")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(genDir)
        .withPropertyName("markdownResources")

    doLast {
        markdownEntries.forEach {
            println("Copying ${it.file.parentFile} to $genDir")
            copy {
                from("${it.file.parentFile}")
                into("${genDir}/public")
                exclude("*.md")
            }
        }
    }
}
val generateMarkdownEntriesTask = task("generateMarkdownEntries") {
    //dependsOn(copyMarkdownResourcesTask.name)
    val group = "dev/stralman"
    val genDir = layout.buildDirectory.dir("generated/$group/src/jsMain/kotlin").get()

    inputs.dir(markdownResourceDir)
        .withPropertyName("markdownEntries")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(genDir)
        .withPropertyName("generatedMarkdownEntryData")

    doLast {
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
                    |val markdownResourceDir = "${layout.projectDirectory.asFile.name}${
                        markdownResourceDir
                            .toString()
                            .substringAfterLast(layout.projectDirectory.asFile.name)
                            .replace("\\", "/")
                    }"
                    |
                    |val markdownEntries = listOf${if (markdownEntries.isEmpty()) "<MarkdownEntry>" else ""}(
                    """.trimMargin()
                )
                markdownEntries.sortedByDescending { it.date }.forEach { entry ->
                    appendLine(
                        """ MarkdownEntry(
                            |       path = "/posts${getUrlFromFilePath(entry.file)}",
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
            println("Generated $absolutePath")
        }
    }
}
val generateRssFromMarkdownEntriesTask = task("generateRssFromMarkdownEntries") {
    val genDir = layout.buildDirectory.dir("generated/resources/rss").get()
    val buildDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val author = "Karl Strålman"
    val baseUrl = "https://www.compose.stralman.dev"
    val rssData = RssData(
        title = "stralman.dev",
        baseUrl = baseUrl,
        author = author,
        description = kobweb.app.index.description.get(),
        language = "en-us",
        lastBuildDate = localDateTimeToRfc1123String(buildDate),
        copyright = "© ${buildDate.year}, $author",
        items = markdownEntries.map {
            // TODO make this more generic
            val url = "${baseUrl}/posts${getUrlFromFilePath(it.file)}"
            var fmCount = 0
            RssItem(
                title = it.title!!,
                link = url,
                guid = url,
                pubDate = localDateToRfc1123String(it.date!!.toLocalDate()),
                description = it.file
                    .readLines()
                    .asSequence()
                    .dropWhile { line ->
                        if (line == "---") {
                            fmCount++
                        }
                        fmCount < 2
                    }
                    .drop(1)
                    .filter { line ->
                        line.isNotEmpty() &&
                                line.isNotBlank() &&
                                line.first() != '#'
                    }
                    .joinToString(". ")
                    .split(Regex("(?<=[.!?])\\s+"))
                    .take(5)
                    .joinToString(". ")
            )
        }
    )
    inputs.dir(markdownResourceDir)
        .withPropertyName("markdownEntries")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(genDir)
        .withPropertyName("rssEntrydata")

    doLast {
        genDir.file("public/index.xml").asFile.apply {
            parentFile.mkdirs()
            writeText(
                rssData.toRssString()
            )
            println("Generated $absolutePath")
        }
    }
}

kotlin {
    // This example is frontend only. However, for a fullstack app, you can uncomment the includeServer parameter
    // and the `jvmMain` source set below.
    configAsKobwebApplication("stralman" /*, includeServer = true*/)
    generateRssFromMarkdownEntriesTask
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
                implementation(libs.silk.icons.fa)
                implementation(libs.kobwebx.markdown)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }

            kotlin.srcDir(generateMarkdownEntriesTask)
            resources.srcDir(generateRssFromMarkdownEntriesTask)
            resources.srcDir(copyMarkdownResourcesTask)
        }

        // Uncomment the following if you pass `includeServer = true` into the `configAsKobwebApplication` call.
//        val jvmMain by getting {
//            dependencies {
//                compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
//            }
//        }
    }
}
