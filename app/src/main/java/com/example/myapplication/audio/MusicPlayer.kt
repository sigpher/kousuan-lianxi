package com.example.myapplication.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.documentfile.provider.DocumentFile
import com.example.myapplication.R

object MusicPlayer {

    private const val PREFS = "app_prefs"
    private const val KEY_ENABLED = "bg_music_enabled"
    private const val KEY_MODE = "bg_music_mode"
    private const val KEY_DIR_URI = "music_dir_uri"

    private val AUDIO_EXTENSIONS = setOf(
        "mp3", "m4a", "wav", "ogg", "flac", "aac", "wma", "aif", "aiff", "mid", "xmf", "mxmf"
    )

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
    private var mode: MusicMode = MusicMode.LIST_LOOP
    private var sources: List<Uri> = emptyList()
    private var customTracks: List<Uri> = emptyList()
    private var appContext: Context? = null
    private val listeners = mutableListOf<(Boolean) -> Unit>()

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun builtinTrackCount(): Int = trackIds.size

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) start(context) else pause()
    }

    fun getMode(context: Context): MusicMode =
        MusicMode.fromValue(prefs(context).getInt(KEY_MODE, MusicMode.LIST_LOOP.value))

    fun setMode(context: Context, newMode: MusicMode) {
        mode = newMode
        prefs(context).edit().putInt(KEY_MODE, newMode.value).apply()
    }

    fun musicDirectoryUri(context: Context): String? =
        prefs(context).getString(KEY_DIR_URI, null)

    fun setMusicDirectory(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        val appCtx = context.applicationContext
        val resolver = context.contentResolver
        try {
            resolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            // permission not persistable on this provider 鈥?still usable this session
        }
        Thread {
            val tracks = enumerateDirectory(appCtx, uri)
            Handler(Looper.getMainLooper()).post {
                if (tracks.isEmpty()) {
                    onResult(false)
                    return@post
                }
                customTracks = tracks
                sources = emptyList()
                prefs(appCtx).edit().putString(KEY_DIR_URI, uri.toString()).apply()
                rebuild(appCtx)
                onResult(true)
            }
        }.start()
    }

    fun clearMusicDirectory(context: Context) {
        val uriStr = musicDirectoryUri(context) ?: run {
            customTracks = emptyList()
            sources = emptyList()
            return
        }
        customTracks = emptyList()
        sources = emptyList()
        prefs(context).edit().remove(KEY_DIR_URI).apply()
        runCatching {
            val uri = Uri.parse(uriStr)
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        rebuild(context)
    }

    fun start(context: Context) {
        appContext = context.applicationContext
        mode = getMode(context)
        val p = player
        if (p != null) {
            if (!p.isPlaying) {
                try {
                    p.start()
                } catch (e: IllegalStateException) {
                    player = null
                }
            }
            notify(true)
            return
        }
        val list = ensureSources(context)
        if (list.isEmpty()) {
            notify(false)
            return
        }
        currentIndex = PlaybackRules.startIndex(mode, list.size)
        playTrack(currentIndex)
    }

    fun pause() {
        player?.let {
            if (it.isPlaying) it.pause()
        }
        notify(false)
    }

    fun destroy() {
        player?.release()
        player = null
        appContext = null
        listeners.clear()
    }

    fun isPlaying(): Boolean = player?.isPlaying == true

    fun addPlayStateListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
        listener(isPlaying())
    }

    fun removePlayStateListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun builtinSources(context: Context): List<Uri> =
        trackIds.map { Uri.parse("android.resource://${context.packageName}/$it") }

    private fun ensureSources(context: Context): List<Uri> {
        if (sources.isNotEmpty()) return sources
        sources = if (customTracks.isNotEmpty()) {
            customTracks
        } else {
            val dir = musicDirectoryUri(context)
            if (dir == null) {
                builtinSources(context)
            } else {
                val tracks = runCatching { Uri.parse(dir) }
                    .getOrNull()
                    ?.let { enumerateDirectory(context, it) }
                    .orEmpty()
                customTracks = tracks
                if (tracks.isNotEmpty()) tracks else builtinSources(context)
            }
        }
        return sources
    }

    private fun enumerateDirectory(context: Context, uri: Uri): List<Uri> {
        return runCatching {
            val tree = DocumentFile.fromTreeUri(context, uri) ?: return emptyList()
            tree.listFiles()
                .orEmpty()
                .filter { it.isFile && isAudioDocument(it) }
                .sortedBy { it.name.orEmpty() }
                .map { it.uri }
        }.getOrDefault(emptyList())
    }

    private fun isAudioDocument(file: DocumentFile): Boolean {
        val type = file.type
        if (type != null) return type.startsWith("audio/")
        val ext = file.name?.substringAfterLast('.', "").orEmpty().lowercase()
        return ext in AUDIO_EXTENSIONS
    }

    private fun rebuild(context: Context) {
        player?.release()
        player = null
        if (appContext != null && isEnabled(context)) {
            start(context)
        }
    }

    private fun playTrack(index: Int) {
        val ctx = appContext ?: return
        val list = if (sources.isNotEmpty()) sources else ensureSources(ctx)
        if (list.isEmpty()) {
            notify(false)
            return
        }
        val size = list.size
        currentIndex = ((index % size) + size) % size
        val uri = list[currentIndex]
        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mp.setDataSource(ctx, uri)
            mp.setOnCompletionListener {
                when (mode) {
                    MusicMode.SINGLE_LOOP -> {
                        try {
                            mp.seekTo(0)
                            mp.start()
                        } catch (e: IllegalStateException) {
                            playTrack(currentIndex)
                        }
                    }
                    MusicMode.LIST_LOOP, MusicMode.SHUFFLE -> {
                        val listNow = if (sources.isNotEmpty()) sources else builtinSources(ctx)
                        val next = PlaybackRules.nextIndex(mode, listNow.size, currentIndex)
                        playTrack(next)
                    }
                }
                notify(true)
            }
            mp.prepare()
            player?.release()
            player = mp
            notify(true)
            mp.start()
        } catch (e: Exception) {
            mp.release()
            player = null
            notify(false)
        }
    }

    private fun notify(playing: Boolean) {
        listeners.toList().forEach { listener ->
            try {
                listener(playing)
            } catch (_: Exception) {
            }
        }
    }
}
