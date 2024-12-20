package com.rudhashi.smartengineer.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.DialogCongratulatuinBinding
import com.rudhashi.smartengineer.databinding.DialogWatchAdsBinding
import com.rudhashi.smartengineer.databinding.DialogWatchVideoBinding
import com.rudhashi.smartengineer.util.SmartConfig

@SuppressLint("InflateParams")
object DialogManager {

    private var watchVideoBonusPoints = SmartConfig.WATCH_VIDEO_BONUS_POINT

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
}