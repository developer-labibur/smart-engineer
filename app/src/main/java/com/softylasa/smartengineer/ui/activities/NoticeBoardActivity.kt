package com.softylasa.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowMetrics
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdSize
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.ActivityNoticeBoardBinding
import com.softylasa.smartengineer.managers.AdManager.setBannerAds
import com.softylasa.smartengineer.managers.DialogManager.showFreemiumFeaturesDialog
import com.softylasa.smartengineer.network.NoticeResponse
import com.softylasa.smartengineer.network.RetrofitClient
import com.softylasa.smartengineer.ui.adapters.BtebNoticeAdapter
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeBoardActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityNoticeBoardBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var btebNoticeAdapter: BtebNoticeAdapter // Replace with your RecyclerView adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNoticeBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        btebNoticeAdapter = BtebNoticeAdapter(emptyList(), this)

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        recyclerView = binding.rvNotice
        recyclerView.adapter = btebNoticeAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        binding.btnBack.setOnClickListener { finish() }

        setBannerAds(this, binding.adViewContainer, adSize, false)

        isShimmerOn(true)

        fetchNotices()
    }

    private fun fetchNotices() {

        // Retrofit network call
        RetrofitClient.apiService.getNotices().enqueue(object : Callback<NoticeResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<NoticeResponse>, response: Response<NoticeResponse>) {
                if (response.isSuccessful) {
                    isShimmerOn(false)
                    btebNoticeAdapter.updateNoticeList(response.body()?.notices ?: emptyList(), false) // Update the adapter
                } else {
                    showError("Failed to load notices. Please try again.")
                }
                btebNoticeAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<NoticeResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Unknown error")
                showError("Failed to load notices. Please check your connection and try again.")
            }
        })
    }

    private fun showError(message: String) {
        isShimmerOn(false)
        // Use an AlertDialog to show error messages
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }


    // Get the ad size with screen width.
    private val adSize: AdSize
        get() {
            val displayMetrics = resources.displayMetrics
            val adWidthPixels =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics: WindowMetrics = this.windowManager.currentWindowMetrics
                    windowMetrics.bounds.width()
                } else {
                    displayMetrics.widthPixels
                }
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }


    override fun onPause() {
        super.onPause()
        isShimmerOn(false)
    }

    private fun isShimmerOn(flag: Boolean){
        binding.apply {
            if (flag){
                shimmerNotice.startShimmer()
                shimmerNotice.visibility = View.VISIBLE
                rvNotice.visibility = View.GONE
            } else {
                shimmerNotice.stopShimmer()
                shimmerNotice.visibility = View.GONE
                rvNotice.visibility = View.VISIBLE
            }
        }
    }
}