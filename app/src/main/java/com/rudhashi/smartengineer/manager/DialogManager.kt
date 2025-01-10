package com.rudhashi.smartengineer.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat.getColor
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.DialogCongratulatuinBinding
import com.rudhashi.smartengineer.databinding.DialogInfoBinding
import com.rudhashi.smartengineer.databinding.DialogPdfDownloadBinding
import com.rudhashi.smartengineer.databinding.DialogProgressBinding
import com.rudhashi.smartengineer.databinding.DialogWatchAdsBinding
import com.rudhashi.smartengineer.databinding.DialogWatchVideoBinding
import com.rudhashi.smartengineer.model.BookUnit
import com.rudhashi.smartengineer.util.SmartConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@SuppressLint("InflateParams")
object DialogManager {

    private var watchVideoBonusPoints = SmartConfig.WATCH_VIDEO_BONUS_POINT
    private var spendToDownload = SmartConfig.DOWNLOAD_PDF_REDEEM_POINT

    // Congratulation Dialog View ----------------------------------------------------------------
    fun showCongratulationDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_congratulatuin, null)
        val dialogBinding = DialogCongratulatuinBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.tv25Point.text = activity.getString(R.string._smart_points, watchVideoBonusPoints.toString())
        dialogBinding.btnSmartPoint.setOnClickListener {builder.dismiss() }
        dialogBinding.btnClose.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    // Watch Video Ad Dialog View ----------------------------------------------------------------
    fun showWatchAdDialog(activity: Activity, txtHeading: Int, txtInfo: Int) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_watch_ads, null)
        val dialogBinding = DialogWatchAdsBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        dialogBinding.apply {
            tvHeading.text = activity.getString(txtHeading)
            tvWatchInfo.text = activity.getString(txtInfo)
            tv25Point.text = activity.getString(R.string._smart_points, watchVideoBonusPoints.toString())
            btnSmartPoint.setOnClickListener {
                AdManager.showRewardedAd(activity)
                builder.dismiss()
            }
            btnClose.setOnClickListener { builder.dismiss() }
            btnNotThanks.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    // Not Enough Point Dialog View ----------------------------------------------------------------
    fun showNotEnoughPointDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_watch_video, null)
        val dialogBinding = DialogWatchVideoBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        val firstText = "You don't have enough points. Watch a video to get"
        val rewardText = "$watchVideoBonusPoints Smart Points"
        val fullText = "$firstText $rewardText" // Combine them into one string
        val spannableString = SpannableString(fullText) // Create a SpannableString
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD), // Bold style
            firstText.length + 1, // Start index (include the space after firstText)
            fullText.length, // End index (end of the string)
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        dialogBinding.tvWatchInfo.text = spannableString // Set the SpannableString to the TextView
        dialogBinding.btnWatchAds.setOnClickListener {
            AdManager.showRewardedAd(activity) // Show the rewarded ad
            builder.dismiss()
        }
        dialogBinding.btnClose.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(
            ColorDrawable(0)
        )
        builder.show()
    }

    // EBook Download Dialog View ----------------------------------------------------------------
    fun showDownloadDialog(activity: Activity, txtHeading: String, txtInfo: String, unit: BookUnit, onItemClick: (BookUnit, String) -> Unit) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_pdf_download, null)
        val dialogBinding = DialogPdfDownloadBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        val rewardText = "$spendToDownload Smart Points"
        val fullText = "$txtInfo $rewardText" // Combine them into one string
        val spannableString = SpannableString(fullText) // Create a SpannableString
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD), // Bold style
            txtInfo.length + 1, // Start index (include the space after firstText)
            fullText.length, // End index (end of the string)
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        dialogBinding.apply {
            tvHeading.text = txtHeading
            tvInfo.text = spannableString
            btnDownload.setOnClickListener {
                builder.dismiss()
                showDownloadingProgressDialog(activity, unit, onItemClick)
            }
            btnClose.setOnClickListener { builder.dismiss() }
            btnNotNow.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    // Downloading Progress Dialog View ----------------------------------------------------------------
    private fun showDownloadingProgressDialog(activity: Activity, unit: BookUnit, onItemClick: (BookUnit, String) -> Unit) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_progress, null)
        val dialogBinding = DialogProgressBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        val client = OkHttpClient()
        val request = Request.Builder().url(unit.UPdf).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                builder.dismiss()
                (activity).runOnUiThread {
                    showSuccessfulDialog(activity, "Download Failed", "Could not download the unit. Please try again later.", R.raw.cross, unit, Color.RED, onItemClick)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    val file = File(activity.cacheDir, "${unit.UName}.pdf")
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
                        (activity).runOnUiThread {
                            dialogBinding.progressBar.progress = progress
                            dialogBinding.tvProgressPercentage.text = "$progress%"
                        }
                    }
                    fos.close()
                    body.close()
                    builder.dismiss()
                    (activity).runOnUiThread {
                        showSuccessfulDialog( activity, "Download Complete", "The unit has been downloaded successfully.", R.raw.check, unit, Color.BLUE, onItemClick)
                    }
                }
            }
        })

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    // Successful Dialog View ----------------------------------------------------------------
    fun showSuccessfulDialog(
        activity: Activity,
        title: String,
        description: String,
        lottie: Int,
        unit: BookUnit,
        color: Int,
        onItemClick: (BookUnit, String) -> Unit
    ) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_info, null)
        val dialogBinding = DialogInfoBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.lottieAnimationView.setAnimation(lottie)
        dialogBinding.lottieAnimationView.playAnimation()
        dialogBinding.tvTitle.text = title
        dialogBinding.tvTitle.setTextColor(color)
        dialogBinding.tvInfo.text = description
        dialogBinding.btnOk.setOnClickListener {
            builder.dismiss()
            onItemClick(unit, "download")
        }
        dialogBinding.btnClose.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }
}