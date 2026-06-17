package com.rinnsan.creavity.core.util

import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

object MediaHelper {

    // Hàm tạo nhanh một Video Player cho màn hình Intro/Home
    fun buildExoPlayer(context: Context, @RawRes videoResId: Int): ExoPlayer {
        val player = ExoPlayer.Builder(context).build()

        // Tạo đường dẫn tới file video trong thư mục raw
        val videoUri = Uri.parse("android.resource://${context.packageName}/$videoResId")
        val mediaItem = MediaItem.fromUri(videoUri)

        player.apply {
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE // Lặp lại vô tận (Loop)
            volume = 0f                         // Tắt tiếng (Mute) để chạy nền
            prepare()
            playWhenReady = true                // Tự động phát
        }

        return player
    }

    // Hàm giải phóng Player khi thoát màn hình (tránh tốn pin)
    fun releasePlayer(player: ExoPlayer?) {
        player?.release()
    }
}