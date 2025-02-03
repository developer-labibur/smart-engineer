package com.rudhashi.smartengineer.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.rudhashi.smartengineer.data.models.TableOfContentItem
import com.rudhashi.smartengineer.databinding.ActivityTestBinding
import com.rudhashi.smartengineer.ui.adapters.TableOfContentAdapter
import com.rudhashi.smartengineer.utils.themes.SmartThemeActivity
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class TestActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityTestBinding
    private val unitList = listOf(
        TableOfContentItem("1","Intro", "https://firebasestorage.googleapis.com/v0/b/lur-icon.appspot.com/o/PDF%2Fstructure%2FAIDT1.pdf?alt=media&token=24aec324-f4a1-4e08-90f6-1cd8727eb15f"),
        TableOfContentItem("1","Chapter 2", "https://firebasestorage.googleapis.com/v0/b/smart-engineer-674e5.appspot.com/o/%E0%A7%A6%E0%A7%A7%E0%A5%A4%20%E0%A6%AC%E0%A6%99%E0%A7%8D%E0%A6%97%E0%A6%AD%E0%A6%BE%E0%A6%B7%E0%A6%BE%20-%20%E0%A6%AE%E0%A6%BE%E0%A6%87%E0%A6%95%E0%A7%87%E0%A6%B2%20%E0%A6%AE%E0%A6%A7%E0%A7%81%E0%A6%B8%E0%A7%82%E0%A6%A6%E0%A6%A8%20%E0%A6%A6%E0%A6%A4%E0%A7%8D%E0%A6%A4%20(%E0%A6%AC%E0%A6%BE%E0%A6%82%E0%A6%B2%E0%A6%BE%20%E0%A6%95%E0%A6%AC%E0%A6%BF%E0%A6%A4%E0%A6%BE).pdf?alt=media&token=c2c30629-a73d-46b0-8cbc-5a759ab2da78")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Ensure the content fills the entire screen, including under the status bar
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@TestActivity)
            recyclerView.adapter = TableOfContentAdapter(this@TestActivity, unitList) { bookUnit, action ->
                when (action) {
                    "view" -> handleViewAction(bookUnit)
                    "download" -> handleDownloadAction(bookUnit)
                }
            }
        }
    }

    // =================================================================================================
    private fun handleViewAction(bookUnit: TableOfContentItem) {
        val cachedFile = getCachedPdfFile(bookUnit.title)
        if (cachedFile.exists()) {
            loadPdf(cachedFile)
        } else {
            loadPdfFromUrl(bookUnit.pdfUrl)
        }
    }

    private fun handleDownloadAction(bookUnit: TableOfContentItem) {
        val cachedFile = getCachedPdfFile(bookUnit.title)
        if (cachedFile.exists()) {
            Toast.makeText(this, "Already downloaded", Toast.LENGTH_SHORT).show()
        } else {
            showDownloadDialog(bookUnit, cachedFile)
        }
    }

    private fun getCachedPdfFile(unitName: String): File {
        return File(cacheDir, "$unitName.pdf")
    }

    private fun showDownloadDialog(bookUnit: TableOfContentItem, cachedFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Download PDF")
            .setMessage("Do you want to download this PDF for offline use?")
            .setPositiveButton("Yes") { _, _ -> downloadPdf(bookUnit, cachedFile) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun downloadPdf(bookUnit: TableOfContentItem, cachedFile: File) {
        thread {
            try {
                val url = URL(bookUnit.pdfUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(cachedFile)

                    inputStream.copyTo(outputStream)

                    inputStream.close()
                    outputStream.close()

                    runOnUiThread {
                        Toast.makeText(this, "Downloaded successfully!", Toast.LENGTH_SHORT).show()
                        binding.recyclerView.adapter?.notifyDataSetChanged() // Refresh the RecyclerView
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Failed to download PDF", Toast.LENGTH_SHORT).show()
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadPdf(file: File) {
        binding.pdfView.fromFile(file).load()
    }

    private fun loadPdfFromUrl(pdfUrl: String) {
        thread {
            try {
                val url = URL(pdfUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bytes = inputStream.readBytes()
                    inputStream.close()

                    runOnUiThread {
                        binding.pdfView.fromBytes(bytes).load()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Failed to load PDF online", Toast.LENGTH_SHORT).show()
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}