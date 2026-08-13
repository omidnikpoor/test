package com.example.chesssoundboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var adapter: BoardAdapter
    private var pendingCellIndex: Int = -1

    // لانچر انتخاب فایل صوتی از حافظه گوشی (Storage Access Framework)
    private val pickAudioLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val index = pendingCellIndex
            pendingCellIndex = -1
            if (uri == null || index == -1) return@registerForActivityResult

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // بعضی providerها اجازه دائمی نمی‌دهند؛ در آن صورت فقط برای این سشن کار می‌کند
            }

            val success = soundManager.assignSound(index, uri)
            if (success) {
                adapter.notifyItemChanged(index)
                Toast.makeText(
                    this,
                    "صدا برای خونه ${index + 1} ثبت شد",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "بارگذاری فایل صوتی با خطا مواجه شد", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        soundManager = SoundManager(this)

        val recyclerView = findViewById<RecyclerView>(R.id.boardRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 8)
        adapter = BoardAdapter(
            cellCount = SoundManager.CELL_COUNT,
            hasSound = { index -> soundManager.hasSound(index) },
            onClick = { index ->
                if (soundManager.hasSound(index)) {
                    soundManager.play(index)
                } else {
                    Toast.makeText(
                        this,
                        "این خونه صدا نداره. لمس طولانی کن تا صدا اضافه کنی",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onLongClick = { index ->
                pendingCellIndex = index
                pickAudioLauncher.launch(arrayOf("audio/*"))
            }
        )
        recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
