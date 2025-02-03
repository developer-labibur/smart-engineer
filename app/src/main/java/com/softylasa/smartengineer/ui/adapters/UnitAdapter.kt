package com.softylasa.smartengineer.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.ItemBookUnitBinding
import com.softylasa.smartengineer.managers.DialogManager
import com.softylasa.smartengineer.managers.PointManager
import com.softylasa.smartengineer.data.models.Book
import com.softylasa.smartengineer.data.models.BookUnit
import com.softylasa.smartengineer.ui.activities.SmartEBookActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UnitAdapter(
    private val context: Context,
    private val bookUnitList: List<BookUnit>,
    private val bookCode: String,
    private val onItemClick: (BookUnit, String) -> Unit
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    inner class UnitViewHolder(binding: ItemBookUnitBinding) : RecyclerView.ViewHolder(binding.root) {
        val unitName: TextView = binding.unitName
        val downloadButton: LinearLayout = binding.btnDownload
        val downloadImage: ImageView = binding.icDownload
        val viewPdfButton: ConstraintLayout = binding.clickUnitItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemBookUnitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unit = bookUnitList[position]
        val cachedFile = File(context.cacheDir, "${unit.UName}.pdf")
        holder.unitName.text = unit.UName
        holder.downloadImage.setImageResource(
            if (cachedFile.exists()) R.drawable.sl_check_circle else R.drawable.sl_cloud_download
        )

        holder.itemView.setOnClickListener { onItemClick(unit, "view") }
        holder.downloadButton.setOnClickListener {
            if (cachedFile.exists()) Log.i("Unit", "File already exists")
            else DialogManager.showDownloadDialog(
                context as Activity,
                "Download",
                "Do you want to download this unit for offline read? - by using",
                unit,
                onItemClick)
        }

        FirebaseFirestore.getInstance().collection("books").document(bookCode).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val book = document.toObject(Book::class.java)
                    if (book != null) {
                        // Open PDF link when the button is clicked
                        holder.viewPdfButton.setOnClickListener {
                            val intent = Intent(context, SmartEBookActivity::class.java)
                            intent.putExtra("UName", unit.UName)
                            intent.putExtra("cachedFilePath", cachedFile.absolutePath)
                            intent.putExtra("UPdf", unit.UPdf)
                            intent.putExtra("UNo", unit.UNo)
                            intent.putExtra("BCode", book.BCode)
                            intent.putExtra("BName", book.BName)
                            context.startActivity(intent)
                            PointManager.redeemPointsOpenPDF()
                        }
                    }
                } else {
                    Toast.makeText(context, "Book not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to fetch book details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = bookUnitList.size

    private fun showDownloadDialog(context: Context, unit: BookUnit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Download PDF")
            .setMessage("Do you want to download this unit for offline use?")
            .setPositiveButton("Download") { _, _ ->
                startDownloadWithProgress(context, unit)
            }
            .setNegativeButton("Not Now") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun startDownloadWithProgress(context: Context, unit: BookUnit) {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        //val tvProgressTitle = dialogView.findViewById<TextView>(R.id.tvProgressTitle)
        val tvProgressPercentage = dialogView.findViewById<TextView>(R.id.tvProgressPercentage)

        // Create the dialog
        val progressDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        progressDialog.show()

        val client = OkHttpClient()
        val request = Request.Builder().url(unit.UPdf).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                progressDialog.dismiss()
                (context as? Activity)?.runOnUiThread {
                    AlertDialog.Builder(context)
                        .setTitle("Download Failed")
                        .setMessage("Could not download the file. Please try again later.")
                        .setPositiveButton("OK") { d, _ -> d.dismiss() }
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    val file = File(context.cacheDir, "${unit.UName}.pdf")
                    val fos = FileOutputStream(file)
                    val buffer = ByteArray(1024)
                    var totalBytesRead = 0L
                    val contentLength = body.contentLength()

                    val inputStream = body.byteStream()
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        val progress = (totalBytesRead * 100 / contentLength).toInt()

                        (context as? Activity)?.runOnUiThread {
                            progressBar.progress = progress
                            tvProgressPercentage.text = "$progress%"
                        }
                    }
                    fos.close()
                    body.close()

                    progressDialog.dismiss()

                    (context as? Activity)?.runOnUiThread {
                        AlertDialog.Builder(context)
                            .setTitle("Download Complete")
                            .setMessage("The PDF has been downloaded successfully.")
                            .setPositiveButton("OK") { d, _ ->
                                d.dismiss()
                                onItemClick(unit, "download")
                            }
                            .show()
                    }
                }
            }
        })
    }
}