package com.nandohypesoft.comav.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object YouTubeUtil {

    fun extrairVideoId(url: String): String? {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return null

        val regexes = listOf(
            Regex("(?:youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/" +
                    "|.*[?&]v=)|youtu\\.be\\/)([^\"&?\\/\\s]{11})"),
            Regex("youtube\\.com\\/shorts\\/([^\"&?\\/\\s]{11})")
        )

        for (regex in regexes) {
            val match = regex.find(cleanUrl)
            if (match != null && match.groupValues.size >= 2) {
                return match.groupValues[1]
            }
        }
        return null
    }

    fun obterUrlThumbnail(url: String): String? {
        val videoId = extrairVideoId(url) ?: return null
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
    }

    fun abrirVideo(context: Context, url: String) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return
        val finalUrl = if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            "https://$cleanUrl"
        } else {
            cleanUrl
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
