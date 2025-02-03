package com.softylasa.smartengineer.utils.helpers

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object PdfUtils {
    fun downloadPdf(context: Context, pdfUrl: String, cachedFile: File, onComplete: () -> Unit) {
        thread {
            try {
                val url = URL(pdfUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(cachedFile)

                    inputStream.copyTo(outputStream)

                    inputStream.close()
                    outputStream.close()

                    (context as? AppCompatActivity)?.runOnUiThread {
                        onComplete()
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                (context as? AppCompatActivity)?.runOnUiThread {
                    Toast.makeText(context, "Download Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}