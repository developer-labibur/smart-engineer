package com.softylasa.smartengineer.managers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.ads.AdManager
import com.softylasa.smartengineer.databinding.DialogCongratulatuinBinding
import com.softylasa.smartengineer.databinding.DialogInfoBinding
import com.softylasa.smartengineer.databinding.DialogNegativeInfoBinding
import com.softylasa.smartengineer.databinding.DialogPdfDownloadBinding
import com.softylasa.smartengineer.databinding.DialogPremiumUserBinding
import com.softylasa.smartengineer.databinding.DialogProgressBinding
import com.softylasa.smartengineer.databinding.DialogWatchAdsBinding
import com.softylasa.smartengineer.databinding.DialogWatchVideoBinding
import com.softylasa.smartengineer.ui.activities.ProfileActivity
import com.softylasa.smartengineer.data.models.BookUnit
import com.softylasa.smartengineer.databinding.DialogComingSoonBinding
import com.softylasa.smartengineer.ads.SmartConfig
import com.softylasa.smartengineer.data.constants.Constants
import com.softylasa.smartengineer.databinding.DialogDeleteAccountBinding
import com.softylasa.smartengineer.databinding.DialogForgotPasswordBinding
import com.softylasa.smartengineer.databinding.DialogPdfUnitDownloadBinding
import com.softylasa.smartengineer.databinding.DialogUploadingProgressBinding
import com.softylasa.smartengineer.ui.activities.WebBrowserActivity
import com.softylasa.smartengineer.utils.helpers.EasySdk.setToast
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


    // Negative Info Dialog ----------------------------------------------------------------
    fun showNegativeInfoDialog(
        activity: Activity,
        lottie: Int,
        txtHeading: String,
        txtInfo: String,
        function: () -> Unit
    ) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_negative_info, null)
        val dialogBinding = DialogNegativeInfoBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        dialogBinding.apply {
            animView.setAnimation(lottie)
            animView.playAnimation()
            tvHeading.text = txtHeading
            tvInfo.text = txtInfo
            btnYes.setOnClickListener {
                function()
                builder.dismiss()
            }
            btnCross.setOnClickListener { builder.dismiss() }
            btnCancel.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }


    fun showDeleteProfileDialog(
        activity: Activity,
        lottie: Int,
        function: () -> Unit
    ) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_delete_account, null)
        val dialogBinding = DialogDeleteAccountBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        setDeleteTermsPrivacyText(activity, dialogBinding.tvAgreement)

        dialogBinding.apply {
            animView.setAnimation(lottie)
            animView.playAnimation()
            btnYes.setOnClickListener {
                function()
                builder.dismiss()
            }
            btnCross.setOnClickListener { builder.dismiss() }
            btnCancel.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    private fun setDeleteTermsPrivacyText(activity: Activity, tvAgreement: TextView){
        val text = "By proceeding, you acknowledge that you’ve read our Privacy Policy and agree to our Terms of Service."
        val spannableString = SpannableString(text)

        // "Terms of Service" clickable span
        val termsStart = text.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open Terms and Conditions
                openWebView(activity,"Terms of Service", Constants.TERMS)
            }
        }
        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(activity.getColor(R.color.google__Blue), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // "Privacy Policy" clickable span
        val privacyStart = text.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open Privacy Policy
                openWebView(activity,"Privacy Policy", Constants.PRIVACY)
            }
        }
        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(activity.getColor(R.color.google__Blue), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the spannable string to the TextView
        tvAgreement.text = spannableString
        tvAgreement.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openWebView(activity: Activity, title: String, url: String) {
        val intent = Intent(activity, WebBrowserActivity::class.java)
        intent.putExtra("TITLE", title)
        intent.putExtra("URL", url)
        activity.startActivity(intent)
    }

    // Freemium Features Dialog ----------------------------------------------------------------
    fun showFreemiumFeaturesDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_premium_user, null)
        val dialogBinding = DialogPremiumUserBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        dialogBinding.apply {
            btnAllPoints.setOnClickListener {
                activity.startActivity(Intent(activity, ProfileActivity::class.java))
                builder.dismiss()
            }
            btnClose.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    // Congratulation Dialog View ----------------------------------------------------------------
    fun showCongratulationDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_congratulatuin, null)
        val dialogBinding = DialogCongratulatuinBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.tv25Point.text =
            activity.getString(R.string._smart_points, watchVideoBonusPoints.toString())
        dialogBinding.btnSmartPoint.setOnClickListener { builder.dismiss() }
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
            tv25Point.text =
                activity.getString(R.string._smart_points, watchVideoBonusPoints.toString())
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
    /*fun showDownloadDialog(activity: Activity, txtHeading: String, txtInfo: String, unit: BookUnit, onItemClick: (BookUnit, String) -> Unit) {
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
    }*/

    fun Activity.showPdfUnitDownloadDialog(
        unit: BookUnit,
        onItemClick: (BookUnit, String) -> Unit
    ) {
        val builder = AlertDialog.Builder(this).create()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pdf_unit_download, null)
        val dialogBinding = DialogPdfUnitDownloadBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        dialogBinding.apply {
            btnDownload.setOnClickListener {
                builder.dismiss()
                showDownloadingProgressDialog(this@showPdfUnitDownloadDialog, unit, onItemClick)
            }
            btnClose.setOnClickListener { builder.dismiss() }
            btnNotNow.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    // Downloading Progress Dialog View ----------------------------------------------------------------
    private fun showDownloadingProgressDialog(
        activity: Activity,
        unit: BookUnit,
        onItemClick: (BookUnit, String) -> Unit
    ) {
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
                    showSuccessfulDialog(
                        activity, "Download Failed",
                        "Could not download the unit. Please try again later.",
                        R.raw.cross, unit, R.color.google__Red, onItemClick
                    )
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
                        showSuccessfulDialog(
                            activity, "Download Complete",
                            "The unit has been downloaded successfully.",
                            R.raw.check, unit, R.color.google__Green, onItemClick
                        )
                    }
                }
            }
        })

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }


    // Uploading Progress Dialog View ----------------------------------------------------------------
    fun showUploadingDialog(activity: Activity) : AlertDialog {
        val builder = AlertDialog.Builder(activity).create()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_uploading_progress, null)
        val dialogBinding = DialogUploadingProgressBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()

        return builder
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
        dialogBinding.tvTitle.setTextColor(activity.getColor(color))
        dialogBinding.tvInfo.text = description
        dialogBinding.btnOk.setOnClickListener {
            builder.dismiss()
            onItemClick(unit, "download")
        }
        dialogBinding.btnClose.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }


    @SuppressLint("InflateParams")
    fun comingSoon(context: Context) {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_coming_soon, null)
        val dialogBinding: DialogComingSoonBinding = DialogComingSoonBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.show()

        dialogBinding.btnOk.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    fun showForgotPasswordDialog(context: Activity) {
        val auth = FirebaseAuth.getInstance()

        val builder = AlertDialog.Builder(context).create()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_forgot_password, null)
        val dialogBinding = DialogForgotPasswordBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.show()

        dialogBinding.btnReset.setOnClickListener {
            val email = dialogBinding.emailBox.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                context.setToast("Enter a valid email address")
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email).addOnSuccessListener {
                context.setToast("Password reset email sent")
                builder.dismiss()
            }.addOnFailureListener() { e ->
                context.setToast("Error: ${e.message}")
            }
        }

        dialogBinding.btnCancel.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }


}