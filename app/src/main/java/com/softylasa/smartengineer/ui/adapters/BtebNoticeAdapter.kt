package com.softylasa.smartengineer.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.BtebNotice
import com.softylasa.smartengineer.databinding.DialogPdfDownloadBinding
import com.softylasa.smartengineer.databinding.ItemBtebNoticeBinding
import com.softylasa.smartengineer.utils.helpers.SmartData
import kotlin.properties.Delegates

class BtebNoticeAdapter(private var noticeList: List<BtebNotice>, private val context: Context) :
    RecyclerView.Adapter<BtebNoticeAdapter.NoticeViewHolder>() {

    private var isRandom by Delegates.notNull<Boolean>()

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val binding = ItemBtebNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(noticeList[position])
    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int = noticeList.size

    // Update the notice list with new data
    @SuppressLint("NotifyDataSetChanged")
    fun updateNoticeList(newNoticeList: List<BtebNotice>, isRandom: Boolean) {
        noticeList = newNoticeList
        this.isRandom = isRandom
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    inner class NoticeViewHolder(private val binding: ItemBtebNoticeBinding) : RecyclerView.ViewHolder(binding.root) {

        // Bind the notice data to the views
        fun bind(notice: BtebNotice) {
            binding.apply {
                tvNoticeText.text = notice.mainTitle
                tvNoticeDate.text = notice.date

                // Random Gradient Background Color Programmatically
                val number = SmartData.randomNumber(6)
                if (isRandom){
                    when (number) {
                        1 -> setUpLoveButton(R.drawable.abc_blue_main, R.drawable.abc_blue_light)
                        2 -> setUpLoveButton(R.drawable.abc_green_main, R.drawable.abc_green_light)
                        3 -> setUpLoveButton(R.drawable.abc_pink_main, R.drawable.abc_pink_light)
                        4 -> setUpLoveButton(R.drawable.abc_orange_main, R.drawable.abc_orange_light)
                        5 -> setUpLoveButton(R.drawable.abc_purple_main, R.drawable.abc_purple_light)
                        6 -> setUpLoveButton(R.drawable.abc_red_main, R.drawable.abc_red_light)
                        else -> setUpLoveButton(R.drawable.abc_blue_main, R.drawable.abc_blue_light)
                    }
                }

                // Set up three-dot menu
                menuQImageView.setOnClickListener { showCustomPopupMenu(it, notice) }

                binding.root.setOnClickListener { showNoticeDownloadDialog(notice) }
                binding.btnViewNotice.setOnClickListener { showNoticeDownloadDialog(notice) }
            }
        }
        private fun setUpLoveButton(llItemBGBg: Int, clickNoticeItemBg: Int){
            binding.llItemBG.setBackgroundResource(llItemBGBg)
            binding.btnViewNotice.setBackgroundResource(llItemBGBg)
            binding.clickNoticeItem.setBackgroundResource(clickNoticeItemBg)
        }

        @SuppressLint("InflateParams")
        private fun showCustomPopupMenu(anchorView: View, notice: BtebNotice) {
            // Inflate the custom menu layout
            val popupView =
                LayoutInflater.from(anchorView.context).inflate(R.layout.popup_menu_layout, null)

            // Create the PopupWindow
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // Focusable, so it will close when touched outside
            )

            // Set custom background drawable (optional if background is set in XML)
            popupWindow.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.menu_background
                )
            )

            // Handle menu item clicks
            val shareNote = popupView.findViewById<LinearLayout>(R.id.share_note)
            shareNote.setOnClickListener {
                shareNotice(notice.mainTitle)
                popupWindow.dismiss() // Close the popup after click
            }

            // Measure the width of the popup to calculate the right margin
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val popupWidth = popupView.measuredWidth

            // Get the location of the anchor view (three-dot icon)
            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)

            // Set your desired right margin
            val rightMargin = 16 // Adjust this value to set the margin you want

            // Calculate x position to align the right endpoints
            // x position should be the right edge of the anchor view minus the popup width minus the desired right margin
            val xPos = location[0] + anchorView.width - popupWidth - rightMargin

            // Show the PopupWindow at the calculated position
            popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos, location[1])
        }

        private fun shareNotice(noticeText: String) {
            val context = itemView.context
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, noticeText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share this Note via"))
        }

    }

    // Notice Download Dialog View ----------------------------------------------------------------
    @SuppressLint("InflateParams")
    private fun showNoticeDownloadDialog(notice: BtebNotice) {
        val builder = AlertDialog.Builder(context).create()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_pdf_download, null)
        val dialogBinding = DialogPdfDownloadBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        dialogBinding.apply {
            tvHeading.text = context.getString(R.string.download_notice)
            tvInfo.text = context.getString(R.string.do_you_want_to_download_this_notice_file)
            btnDownload.setOnClickListener {
                builder.dismiss()
                openChromeCustomTab(notice.link)
            }
            btnClose.setOnClickListener { builder.dismiss() }
            btnNotNow.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    @Suppress("DEPRECATION")
    private fun openChromeCustomTab(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true) // Shows the page title
            .setToolbarColor(ContextCompat.getColor(context, R.color.custom_tab_color)) // Change toolbar color
            .setUrlBarHidingEnabled(true) // Hides URL bar for a cleaner UI
            .setShareState(CustomTabsIntent.SHARE_STATE_ON) // Enables share button
            .setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right) // Smooth transition
            .setExitAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
            .build()

        val uri = Uri.parse(url)

        try {
            customTabsIntent.intent.putExtra("android.support.customtabs.extra.TOOLBAR_COLOR", ContextCompat.getColor(context, R.color.custom_tab_color))
            customTabsIntent.intent.putExtra("android.support.customtabs.extra.SECONDARY_TOOLBAR_COLOR", ContextCompat.getColor(context, R.color.custom_tab_color))

            // Make it open as a bottom sheet
            customTabsIntent.intent.putExtra("androidx.browser.customtabs.extra.SHARE_STATE", CustomTabsIntent.SHARE_STATE_OFF)
            customTabsIntent.intent.putExtra("androidx.browser.customtabs.extra.ACTIVITY_HEIGHT_RESIZE_BEHAVIOR", 1) // Enable bottom-sheet mode

            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to open Chrome Custom Tab", Toast.LENGTH_SHORT).show()
        }
    }

}