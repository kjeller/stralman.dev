
import com.varabyte.kobweb.common.path.invariantSeparatorsPath
import com.varabyte.kobweb.gradle.application.extensions.AppBlock
import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import com.varabyte.kobwebx.gradle.markdown.MarkdownHandlers.Companion.HeadingIdsKey
import com.varabyte.kobwebx.gradle.markdown.ext.kobwebcall.KobwebCall
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.script

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

val markdownResourceDir = layout.projectDirectory.dir("src/jsMain/resources/markdown/posts")
val markdownGenDir = project.layout.buildDirectory.dir("generated/resources/markdown")
val rssDir = project.layout.buildDirectory.dir("generated/resources/rss")

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
            legacyRouteRedirectStrategy.set(AppBlock.LegacyRouteRedirectStrategy.DISALLOW)
        }
    }
    markdown {
        process.set { markdownEntries ->
            val fmk = FrontMatterKeys()
            val blogpostMarkdownData: List<BlogpostMarkdownData> = markdownEntries
                .filter{ it.route.contains("posts") }
                .map{ markdownData ->
                    val fm = markdownData.frontMatter
                    BlogpostMarkdownData(
                        file = File(markdownResourceDir.toString().substringBeforeLast("posts"), markdownData.filePath),
                        route = "${markdownResourceDir.toString()
                            .substringAfterLast("\\") // Windows
                            .substringAfterLast('/') // Unix
                        }${getUrlFromFilePath(File(markdownData.filePath))}",
                        title = fm[fmk.title]?.firstOrNull(),
                        author = fm[fmk.author]?.firstOrNull(),
                        tags = fm[fmk.tags],
                        date = fm[fmk.date]?.firstOrNull()
                    )
            }
            generateKotlin("dev/stralman/blogposts/BlogPosts.kt",
                buildString {
                    appendLine(
                        """
                        |// This file is generated. Modify the build script if you need to change it.
                        |
                        |package dev.stralman.blogposts
                        |
                        |import kotlinx.datetime.toLocalDate
                        |import kotlinx.datetime.LocalDate
                        |
                        |data class BlogPost(
                        |   val route: String,
                        |   val title: String,
                        |   val author: String,
                        |   val date: LocalDate,
                        |   val tags: List<String> = emptyList()
                        |)
                        |
                        |val markdownResourceDir = "${layout.projectDirectory.asFile.name}${
                            markdownResourceDir
                                .toString()
                                .substringAfterLast(layout.projectDirectory.asFile.name)
                                .replace("\\", "/")
                        }"
                        |
                        |val blogPostEntries = listOf${if (blogpostMarkdownData.isEmpty()) "<BlogPostEntry>" else ""}(
                        """.trimMargin()
                    )
                    blogpostMarkdownData.sortedByDescending { it.date}.forEach {
                        appendLine("   ${it.toBlogPost()},")
                    }
                    appendLine(")")
                }
            )
            generateRssFromBlogpostMarkdown(blogpostMarkdownData)
            copyMarkdownResources(blogpostMarkdownData)
        }
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

            val baseHeadingHandler = heading.get()
            heading.set { heading ->
                // Convert a heading to include its ID
                // e.g. <h2>My Heading</h2> becomes <h2 id="my-heading">My Heading</h2>
                val result = baseHeadingHandler.invoke(this, heading)
                // ID guaranteed to be created as side effect of base handler
                val id = data.getValue(HeadingIdsKey).getValue(heading)

                // HoverLink is a widget that will show a link icon (linking back to the header) on hover
                // This is a useful way to let people share a link to a specific header
                heading.appendChild(KobwebCall(".components.widgets.navigation.HoverLink(\"#$id\")"))

                result
            }
        }
    }
}

data class FrontMatterKeys(
    val author: String = "author",
    val date: String = "date",
    val title: String = "title",
    val tags: String = "tags",
)

data class BlogpostMarkdownData(
    val file: File,
    val route: String,
    val date: String?,
    val title: String?,
    val author: String?,
    val tags: List<String>? = emptyList()
) {
    fun toBlogPost() =
        """
        |BlogPost(
        |   route = "$route",
        |   date = "${date?.ifEmpty { "1970-01-01" }}".toLocalDate(),
        |   title = "$title",
        |   author = "$author",
        |   tags = ${if (tags.isNullOrEmpty()) "emptyList()" else "listOf<String>(${tags.joinToString { "\"$it\"" }})"}
        |)
  		""".trimMargin()
}

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
        .substringAfterLast(markdownResourceDir.toString().substringAfterLast("\\")) // Windows
        .substringAfterLast(markdownResourceDir.toString().substringAfterLast("/")) // Unix
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

fun copyMarkdownResources(blogpostMarkdownData: List<BlogpostMarkdownData>) {
    blogpostMarkdownData.forEach {
        println("Copying ${it.file.parentFile} to ${markdownGenDir.get()}")
        copy {
            from("${it.file.parentFile}")
            into("${markdownGenDir.get()}/public")
            exclude("*.md")
        }
    }
}

fun generateRssFromBlogpostMarkdown(blogpostMarkdownData: List<BlogpostMarkdownData>) {
    val buildDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val author = "Karl Strålman"
    val baseUrl = "https://www.stralman.dev"
    val rssData = RssData(
        title = "stralman.dev",
        baseUrl = baseUrl,
        author = author,
        description = kobweb.app.index.description.get(),
        language = "en-us",
        lastBuildDate = localDateTimeToRfc1123String(buildDate),
        copyright = "© ${buildDate.year}, $author",
        items = blogpostMarkdownData.map {
            val url = "${baseUrl}/${it.route}"
            var fmCount = 0
            RssItem(
                title = it.title!!,
                link = url,
                guid = url,
                pubDate = localDateToRfc1123String(it.date!!.toLocalDate()),
                // Take the first 5 sentences excluding any headline
                description = it.file
                    .readLines()
                    .asSequence()
                    .dropWhile { line ->
                        if (line == "---") {
                            fmCount++
                        }
                        fmCount < 2
                    }
                    .drop(1) // TODO this maybe can be dropped?
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
    rssDir.get().file("public/index.xml").asFile.apply {
        parentFile.mkdirs()
        writeText(
            rssData.toRssString()
        )
        println("Generated $absolutePath")
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
                implementation(libs.silk.icons.fa)
                implementation(libs.kobwebx.markdown)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
            resources.srcDir(rssDir)
            resources.srcDir(markdownGenDir)
        }

        // Uncomment the following if you pass `includeServer = true` into the `configAsKobwebApplication` call.
//        val jvmMain by getting {
//            dependencies {
//                compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
//            }
//        }
    }
}
