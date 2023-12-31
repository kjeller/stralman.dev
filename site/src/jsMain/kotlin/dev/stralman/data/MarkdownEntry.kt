package dev.stralman.data

import kotlinx.datetime.LocalDate

data class MarkdownEntry(
    val path: String,
    val title: String,
    val author: String,
    val date: LocalDate,
    val tags: List<String> = emptyList()
)
