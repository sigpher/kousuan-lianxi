package com.example.myapplication.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.myapplication.R
import kotlin.random.Random

object MusicPlayer {

    private const val PREFS = "app_prefs"
    private const val KEY_ENABLED = "bg_music_enabled"

    private val trackIds = intArrayOf(
        R.raw.music_1,
        R.raw.music_2,
        R.raw.music_3,
        R.raw.music_4,
        R.raw.music_5,
        R.raw.music_6,
        R.raw.music_7,
        R.raw.music_8,
        R.raw.music_9,
        R.raw.music_10
    )

    private var player: MediaPlayer? = null
    private var currentIndex = 0
    private var appContext: Context? = null

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) start(context) else pause()
    }

    fun start(context: Context) {
        appContext = context.applicationContext
        val p = player
        if (p != null) {
            if (!p.isPlaying) {
                try {
                    p.start()
                } catch (e: IllegalStateException) {
                    player = null
                }
            }
            return
        }
        currentIndex = Random.nextInt(trackIds.size)
        playTrack(currentIndex)
    }

    fun pause() {
        player?.let {
            if (it.isPlaying) it.pause()
        }
    }

    fun destroy() {
        player?.release()
        player = null
        appContext = null
    }

    private fun playTrack(index: Int) {
        val ctx = appContext ?: return
        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mp.setDataSource(
                ctx,
                Uri.parse("android.resource://${ctx.packageName}/${trackIds[index]}")
            )
            mp.setOnCompletionListener {
                currentIndex = (currentIndex + 1) % trackIds.size
                playTrack(currentIndex)
            }
            mp.prepare()
            player?.release()
            player = mp
            mp.start()
        } catch (e: Exception) {
            mp.release()
            player = null
        }
    }
}