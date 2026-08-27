package com.quantumslate.dashboard.data.remote

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Parses RSS 2.0 and Atom feeds into a common shape.
 *
 * The original parser only understood RSS 2.0 `<item>` elements and a single date format, so
 * an Atom feed produced zero articles and surfaced as "No articles found in feed" — which
 * reads like an empty feed rather than an unsupported format.
 */
object FeedParser {

    /** One parsed entry, independent of which feed dialect produced it. */
    data class ParsedItem(
        val guid: String,
        val title: String,
        val link: String,
        val description: String?,
        val publishedAt: Long
    )

    fun parse(document: Document, limit: Int = 10): List<ParsedItem> {
        // RSS 2.0 / RDF use <item>; Atom uses <entry>.
        val nodes = document.getElementsByTagName("item")
            .takeIf { it.length > 0 }
            ?: document.getElementsByTagName("entry")

        val items = mutableListOf<ParsedItem>()
        for (i in 0 until minOf(nodes.length, limit)) {
            val el = nodes.item(i) as? Element ?: continue

            val title = el.firstText("title")?.trim().orEmpty().ifEmpty { "Untitled" }
            val link = el.extractLink()
            val description = (el.firstText("description")
                ?: el.firstText("summary")
                ?: el.firstText("content"))
                ?.let { stripHtml(it) }
                ?.takeIf { it.isNotBlank() }

            val published = parseDate(
                el.firstText("pubDate")
                    ?: el.firstText("published")
                    ?: el.firstText("updated")
                    ?: el.firstText("date")
            )

            // Prefer the feed's own identifier so re-fetching updates rather than duplicates;
            // fall back to the link, then the title.
            val guid = el.firstText("guid")?.trim()
                ?: el.firstText("id")?.trim()
                ?: link.takeIf { it != "#" }
                ?: title

            items += ParsedItem(guid, title, link, description, published)
        }
        return items
    }

    /**
     * Atom puts the URL in `<link href="...">`; RSS puts it in the element's text.
     * Atom feeds usually carry several links, so prefer `rel="alternate"` (the article
     * itself) over `rel="self"` (the feed).
     */
    private fun Element.extractLink(): String {
        val links = getElementsByTagName("link")
        var fallbackHref: String? = null
        for (i in 0 until links.length) {
            val el = links.item(i) as? Element ?: continue
            val text = el.textContent?.trim()
            if (!text.isNullOrEmpty()) return text

            val href = el.getAttribute("href")?.trim()
            if (href.isNullOrEmpty()) continue
            val rel = el.getAttribute("rel")?.trim().orEmpty()
            if (rel.isEmpty() || rel == "alternate") return href
            if (fallbackHref == null) fallbackHref = href
        }
        return fallbackHref ?: "#"
    }

    private fun Element.firstText(tag: String): String? =
        getElementsByTagName(tag).item(0)?.textContent?.takeIf { it.isNotBlank() }

    /** Feed descriptions are frequently HTML; the widget renders plain text. */
    private fun stripHtml(raw: String): String = raw
        .replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("\\s+"), " ")
        .trim()

    /**
     * Feeds in the wild use RFC-822 (RSS), ISO-8601 (Atom), and several near-misses.
     * Unparseable dates fall back to now, so one odd entry never drops an otherwise good feed.
     */
    private fun parseDate(raw: String?): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis()
        val value = raw.trim()

        // SimpleDateFormat's Z wants +0000, not the +00:00 that ISO-8601 emits.
        val normalised = Regex("([+-]\\d{2}):(\\d{2})$")
            .replace(value) { it.groupValues[1] + it.groupValues[2] }

        for (pattern in PATTERNS) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.ENGLISH)
                if (pattern.endsWith("'Z'")) fmt.timeZone = TimeZone.getTimeZone("UTC")
                fmt.parse(normalised)?.let { return it.time }
            } catch (e: Exception) {
                // Try the next pattern.
            }
        }
        return System.currentTimeMillis()
    }

    private val PATTERNS = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",      // RFC 822 with numeric offset
        "EEE, dd MMM yyyy HH:mm:ss zzz",    // RFC 822 with a zone name (GMT, EST)
        "EEE, dd MMM yyyy HH:mm Z",
        "yyyy-MM-dd'T'HH:mm:ssZ",           // ISO 8601, offset normalised above
        "yyyy-MM-dd'T'HH:mm:ss'Z'",         // ISO 8601 UTC
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )
}
