package com.akaiyukiusagi.notionmemo

import android.app.Notification
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.File


class MainActivity : AppCompatActivity() {
    lateinit var text: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeListener()

        // 起動時にメモの内容を読み込み
        val fileName = "memo.txt"
        text = readFiles(fileName).toString()
        editText.setText(text)

        // textEditの文字を編集したら 保存 & 通知に表示
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edited: Editable?) {
                if (edited.toString().isEmpty()) removeNotificationAll()
                else {
                    val char = edited!!.split("\n")
                    val count = char[0].count()
                    pushNotification(char[0], edited.substring(count))
                }
                saveFile(fileName, edited.toString())
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    // カスタムタイトルバーを適用 //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_title_bar, menu)
        return true
    }

    // タイトルバーのボタンを押した時の挙動 //
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> { // 共有
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(intent)
            }
        }
        return true
    }

    // 通知送信用 //
    private fun pushNotification(title: String = "", text: String = "") {
        val notification: Notification?
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chID = getString(R.string.app_name) // アプリ名をチャンネルIDとして利用

        // クリックされた時の遷移先
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // 通知チャンネルIDを生成してインスタンス化 //
        // IMPORTANCEはポップアップ（正式名ヘッドアップ通知）するならHIGH以上にする
        val notificationChannel = NotificationChannel(chID, chID, NotificationManager.IMPORTANCE_DEFAULT)
        // 通知の説明のセット
        notificationChannel.description = chID
        // 通知チャンネルの作成
        notificationManager.createNotificationChannel(notificationChannel)
        // 通知の生成と設定とビルド
        notification = Notification.Builder(this, chID)
            .setContentTitle(title)                            // 通知タイトル
            .setContentText(text)                              // 通知内容
            .setSmallIcon(android.R.drawable.ic_menu_edit)     // 通知用アイコン
            .setContentIntent(pendingIntent)                   // 通知タップ時
            .setAutoCancel(false)                              // タップしても消さない
            .setVisibility(VISIBILITY_PUBLIC)                  // ロック画面で内容を表示 https://developer.android.com/training/notify-user/build-notification?hl=ja#lockscreenNotification
            .build()                                           // 通知のビルド
        notification.flags = Notification.FLAG_NO_CLEAR        // スワイプで消さない

        // 通知を送信
        notificationManager.notify(0, notification)
    }

    // 通知削除用 //
    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(0) // 引数のidはnotificationManager.notifyで送ったidと揃える
    }

    // 通知全削除 //
    private fun removeNotificationAll() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    // ファイル保存 //
    private fun saveFile(fileName: String, content: String) {
        applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(content.toByteArray())
        }
    }

    // ファイル読み込み //
    private fun readFiles(fileName: String): String? {
        // to check whether file exists or not
        val readFile = File(applicationContext.filesDir, fileName)

        return if(!readFile.exists()) {
            Log.d("debug","No file exists")
            null
        } else {
            readFile.bufferedReader().use(BufferedReader::readText)
        }
    }

    // ボタンを押した時の挙動 //
    private fun initializeListener() {
//        buttonClear.setOnClickListener {
//            editText.setText("")
//        }
//        buttonTodo.setOnClickListener {
//            editText.setText("やること\n")
//        }
//        buttonBuy.setOnClickListener {
//            editText.setText("買う物\n")
//        }
    }

}

// TODO: 通知音を消す
// TODO: 起動を検知して通知を出す