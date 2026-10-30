package me.ash.reader.ui.component.base

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

@Composable
fun HtmlText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val annotatedString = remember(text) {
        buildAnnotatedString {
            val doc = Jsoup.parseBodyFragment(text)
            fun appendNode(node: Node) {
                when (node) {
                    is TextNode -> append(node.text())
                    is Element -> {
                        val spanStyle = when (node.tagName().lowercase()) {
                            "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
                            "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
                            "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                            "s", "strike", "del" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                            else -> null
                        }
                        if (spanStyle != null) {
                            pushStyle(spanStyle)
                            node.childNodes().forEach { appendNode(it) }
                            pop()
                        } else {
                            node.childNodes().forEach { appendNode(it) }
                        }
                    }
                    else -> node.childNodes().forEach { appendNode(it) }
                }
            }
            doc.body().childNodes().forEach { appendNode(it) }
        }
    }
    Text(text = annotatedString, modifier = modifier, style = style)
}
