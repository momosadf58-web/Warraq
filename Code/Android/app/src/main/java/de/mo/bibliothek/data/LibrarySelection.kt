package de.mo.bibliothek.data

internal data class LibrarySelection(
    val books: List<Book>,
    val allCount: Int,
    val statusCounts: Map<ReadStatus, Int>,
    val arabicCount: Int,
    val germanCount: Int,
)

private val ISBN_INPUT = Regex("""[\p{Nd}xX\p{Pd}\s]+""")
private val ISBN_KEY = Regex("""\p{Nd}{9}[\p{Nd}X]|\p{Nd}{13}""")

internal fun selectLibraryBooks(
    books: List<Book>,
    query: String,
    status: String?,
    language: String?,
): LibrarySelection {
    val key = searchKey(query)
    val isbn = isbnKey(query).takeIf { ISBN_INPUT.matches(query) && ISBN_KEY.matches(it) }
    val searched = if (key.isEmpty()) books else books.filter { book ->
        (isbn != null && isbnKey(book.isbn) == isbn) ||
            searchKey("${book.title} ${book.author} ${book.isbn} ${book.genre} ${book.lists.joinToString(" ")}").contains(key)
    }
    val byLanguage = when (language) {
        null -> searched
        "ar" -> searched.filter { it.isArabic }
        "de" -> searched.filter { it.isGerman }
        else -> emptyList()
    }
    val byStatus = if (status == null) searched else searched.filter { it.readStatus.name == status }
    return LibrarySelection(
        books = if (status == null) byLanguage else byLanguage.filter { it.readStatus.name == status },
        allCount = searched.size,
        statusCounts = ReadStatus.entries.associateWith { status -> byLanguage.count { it.readStatus == status } },
        arabicCount = byStatus.count { it.isArabic },
        germanCount = byStatus.count { it.isGerman },
    )
}
