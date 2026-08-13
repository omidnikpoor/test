package com.example.chesssoundboard

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.util.Log

/**
 * مسئول بارگذاری، پخش، و ذخیره‌سازی دائمی صداهای اختصاص داده شده به هر خونه.
 * از SoundPool برای پخش سریع و کم‌تاخیر استفاده می‌کند.
 */
class SoundManager(private val context: Context) {

    companion object {
        const val CELL_COUNT = 64
        private const val PREFS_NAME = "chess_soundboard"
        private const val TAG = "SoundManager"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val soundIdMap = HashMap<Int, Int>() // cellIndex -> soundPool id
    private val uriMap = HashMap<Int, Uri>()     // cellIndex -> uri انتخاب‌شده

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(16)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    init {
        loadSavedSounds()
    }

    private fun loadSavedSounds() {
        for (index in 0 until CELL_COUNT) {
            val saved = prefs.getString(keyFor(index), null) ?: continue
            try {
                loadIntoPool(index, Uri.parse(saved))
            } catch (e: Exception) {
                Log.e(TAG, "خطا در بارگذاری مجدد صدای خونه $index", e)
            }
        }
    }

    private fun keyFor(index: Int) = "cell_$index"

    fun hasSound(index: Int): Boolean = uriMap.containsKey(index)

    /** یک فایل صوتی انتخاب‌شده توسط کاربر را به خونه مشخص اختصاص می‌دهد و ذخیره می‌کند. */
    fun assignSound(index: Int, uri: Uri): Boolean {
        return try {
            loadIntoPool(index, uri)
            prefs.edit().putString(keyFor(index), uri.toString()).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "خطا در بارگذاری صدا برای خونه $index", e)
            false
        }
    }

    private fun loadIntoPool(index: Int, uri: Uri) {
        val afd = context.contentResolver.openAssetFileDescriptor(uri, "r")
            ?: throw IllegalStateException("امکان باز کردن فایل صوتی وجود ندارد")
        // اگر قبلا صدایی برای این خونه بارگذاری شده، آزادش کن
        soundIdMap[index]?.let { soundPool.unload(it) }

        val soundId = soundPool.load(afd, 1)
        afd.close()
        soundIdMap[index] = soundId
        uriMap[index] = uri
    }

    fun play(index: Int) {
        val id = soundIdMap[index] ?: return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun clearSound(index: Int) {
        prefs.edit().remove(keyFor(index)).apply()
        soundIdMap[index]?.let { soundPool.unload(it) }
        soundIdMap.remove(index)
        uriMap.remove(index)
    }

    fun release() {
        soundPool.release()
    }
}
