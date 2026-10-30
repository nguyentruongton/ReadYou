package me.ash.reader.infrastructure.platform

import java.awt.GraphicsEnvironment
import java.awt.SystemTray
import java.awt.TrayIcon
import me.ash.reader.domain.model.feed.FeedWithArticle

class DesktopNotificationManager(
    private val onSendNotification: (title: String, message: String) -> Unit = ::sendOsNotification,
) : NotificationManager {

    override fun notify(feedWithArticle: FeedWithArticle) {
        if (!feedWithArticle.feed.isNotification || feedWithArticle.articles.isEmpty()) return

        val title = feedWithArticle.feed.name
        val message = if (feedWithArticle.articles.size == 1) {
            feedWithArticle.articles.first().title
        } else {
            "${feedWithArticle.articles.size} new articles: ${feedWithArticle.articles.first().title}"
        }

        onSendNotification(title, message)
    }

    companion object {
        fun buildAppleScriptNotificationCommand(title: String, message: String): List<String> {
            val escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"")
            val escapedTitle = title.replace("\\", "\\\\").replace("\"", "\\\"")
            return listOf(
                "/usr/bin/osascript",
                "-e",
                "display notification \"$escapedMessage\" with title \"$escapedTitle\"",
            )
        }

        fun sendOsNotification(title: String, message: String) {
            if (GraphicsEnvironment.isHeadless()) return

            val os = System.getProperty("os.name", "").lowercase()
            if (os.contains("mac")) {
                runCatching {
                    ProcessBuilder(buildAppleScriptNotificationCommand(title, message)).start()
                }
            } else if (SystemTray.isSupported()) {
                runCatching {
                    SystemTray.getSystemTray().trayIcons.firstOrNull()?.displayMessage(
                        title,
                        message,
                        TrayIcon.MessageType.INFO,
                    )
                }
            }
        }
    }
}
