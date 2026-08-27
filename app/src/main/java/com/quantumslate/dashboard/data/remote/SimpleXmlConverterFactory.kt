package com.quantumslate.dashboard.data.remote

import okhttp3.ResponseBody
import org.w3c.dom.Document
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Custom Retrofit converter factory for converting XML responses to W3C Document objects.
 * Used for RSS feed parsing.
 */
class SimpleXmlConverterFactory private constructor() : Converter.Factory() {

    companion object {
        fun create(): SimpleXmlConverterFactory = SimpleXmlConverterFactory()
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return if (type == Document::class.java) {
            DocumentConverter
        } else {
            null
        }
    }

    private object DocumentConverter : Converter<ResponseBody, Document> {
        private val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isValidating = false
        }

        override fun convert(value: ResponseBody): Document {
            val builder = factory.newDocumentBuilder()
            val xmlString = value.string()
            return builder.parse(StringReader(xmlString).toInputSource())
        }

        private fun StringReader.toInputSource(): org.xml.sax.InputSource {
            return org.xml.sax.InputSource(this)
        }
    }
}
