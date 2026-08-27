package com.quantumslate.dashboard

import com.google.common.truth.Truth.assertThat
import com.quantumslate.dashboard.data.remote.FeedParser
import org.junit.Test
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The shipped parser understood only RSS 2.0 `<item>` and one date format, so Atom feeds
 * produced zero entries and surfaced as "No articles found" — indistinguishable from an
 * empty feed. These tests pin both dialects.
 */
class FeedParserTest {

    private fun parse(xml: String): Document {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isValidating = false
        }
        return factory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
    }

    private val rss = """
        <?xml version="1.0"?>
        <rss version="2.0">
          <channel>
            <title>Example Feed</title>
            <item>
              <title>First headline</title>
              <link>https://example.com/1</link>
              <description>&lt;p&gt;Some &amp;amp; markup&lt;/p&gt;</description>
              <pubDate>Tue, 26 Aug 2026 09:15:00 +0000</pubDate>
              <guid>tag:example,1</guid>
            </item>
            <item>
              <title>Second headline</title>
              <link>https://example.com/2</link>
              <pubDate>Tue, 26 Aug 2026 08:00:00 GMT</pubDate>
            </item>
          </channel>
        </rss>
    """.trimIndent()

    private val atom = """
        <?xml version="1.0"?>
        <feed xmlns="http://www.w3.org/2005/Atom">
          <title>Atom Example</title>
          <entry>
            <title>Atom headline</title>
            <link rel="self" href="https://example.com/feed.xml"/>
            <link rel="alternate" href="https://example.com/article"/>
            <id>urn:uuid:1234</id>
            <summary>An Atom summary</summary>
            <published>2026-08-26T09:15:00+00:00</published>
          </entry>
        </feed>
    """.trimIndent()

    @Test
    fun `parses RSS 2 items`() {
        val items = FeedParser.parse(parse(rss))
        assertThat(items).hasSize(2)
        assertThat(items[0].title).isEqualTo("First headline")
        assertThat(items[0].link).isEqualTo("https://example.com/1")
    }

    @Test
    fun `parses Atom entries`() {
        // The whole point of the fix: this returned zero before.
        val items = FeedParser.parse(parse(atom))
        assertThat(items).hasSize(1)
        assertThat(items[0].title).isEqualTo("Atom headline")
    }

    @Test
    fun `Atom link comes from the href attribute, preferring alternate over self`() {
        val items = FeedParser.parse(parse(atom))
        // rel="self" points at the feed, not the article; picking it would send the user
        // back to raw XML.
        assertThat(items[0].link).isEqualTo("https://example.com/article")
    }

    @Test
    fun `strips HTML and decodes entities in descriptions`() {
        val items = FeedParser.parse(parse(rss))
        assertThat(items[0].description).isEqualTo("Some & markup")
    }

    @Test
    fun `reads Atom summary as the description`() {
        assertThat(FeedParser.parse(parse(atom))[0].description).isEqualTo("An Atom summary")
    }

    /** 2026-08-26T09:15:00Z, computed rather than hardcoded. */
    private fun utc(y: Int, mo: Int, d: Int, h: Int, mi: Int): Long =
        java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("UTC")).apply {
            clear(); set(y, mo - 1, d, h, mi, 0)
        }.timeInMillis

    @Test
    fun `parses RFC822 dates with a numeric offset`() {
        val items = FeedParser.parse(parse(rss))
        assertThat(items[0].publishedAt).isEqualTo(utc(2026, 8, 26, 9, 15))
    }

    @Test
    fun `parses RFC822 dates with a zone name`() {
        // "GMT" instead of "+0000" broke the single-format parser entirely.
        val items = FeedParser.parse(parse(rss))
        assertThat(items[1].publishedAt).isEqualTo(utc(2026, 8, 26, 8, 0))
    }

    @Test
    fun `parses ISO 8601 dates with a colon offset`() {
        val items = FeedParser.parse(parse(atom))
        assertThat(items[0].publishedAt).isEqualTo(utc(2026, 8, 26, 9, 15))
    }

    @Test
    fun `RSS and Atom forms of the same instant agree`() {
        // Cross-check between dialects, independent of any hardcoded epoch.
        assertThat(FeedParser.parse(parse(atom))[0].publishedAt)
            .isEqualTo(FeedParser.parse(parse(rss))[0].publishedAt)
    }

    @Test
    fun `prefers the feed guid so refetching updates instead of duplicating`() {
        val items = FeedParser.parse(parse(rss))
        assertThat(items[0].guid).isEqualTo("tag:example,1")
    }

    @Test
    fun `falls back to the link when no guid is present`() {
        val items = FeedParser.parse(parse(rss))
        assertThat(items[1].guid).isEqualTo("https://example.com/2")
    }

    @Test
    fun `an unparseable date does not drop the entry`() {
        val xml = """
            <rss version="2.0"><channel><item>
              <title>Odd date</title><link>https://example.com/x</link>
              <pubDate>last Thursday-ish</pubDate>
            </item></channel></rss>
        """.trimIndent()
        val items = FeedParser.parse(parse(xml))
        assertThat(items).hasSize(1)
        assertThat(items[0].publishedAt).isGreaterThan(0L)
    }

    @Test
    fun `missing title falls back rather than producing a blank row`() {
        val xml = """
            <rss version="2.0"><channel><item>
              <link>https://example.com/x</link>
            </item></channel></rss>
        """.trimIndent()
        assertThat(FeedParser.parse(parse(xml))[0].title).isEqualTo("Untitled")
    }

    @Test
    fun `respects the item limit`() {
        val many = buildString {
            append("<rss version=\"2.0\"><channel>")
            repeat(25) { append("<item><title>T$it</title><link>https://e.com/$it</link></item>") }
            append("</channel></rss>")
        }
        assertThat(FeedParser.parse(parse(many), limit = 10)).hasSize(10)
    }

    @Test
    fun `an empty feed yields no items rather than throwing`() {
        val xml = """<rss version="2.0"><channel><title>Empty</title></channel></rss>"""
        assertThat(FeedParser.parse(parse(xml))).isEmpty()
    }
}
