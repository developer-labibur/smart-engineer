package com.softylasa.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.softylasa.smartengineer.databinding.ActivityTestBinding
import com.softylasa.smartengineer.network.NoticeResponse
import com.softylasa.smartengineer.network.RetrofitClient
import com.softylasa.smartengineer.ui.adapters.BtebNoticeAdapter
import com.softylasa.smartengineer.ui.adapters.NoticeAdapter
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity
import com.softylasa.smartengineer.xyz.createInsecureHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback as RetrofitCallback
import retrofit2.Response as RetrofitResponse
import java.io.IOException

class TestActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var btebNoticeAdapter: BtebNoticeAdapter // Replace with your RecyclerView adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btebNoticeAdapter = BtebNoticeAdapter(emptyList(), this@TestActivity)

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        recyclerView = binding.recyclerView
        recyclerView.adapter = btebNoticeAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchNotices()
    }

    private fun fetchNotices() {

        // Retrofit network call
        RetrofitClient.apiService.getNotices().enqueue(object : RetrofitCallback<NoticeResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<NoticeResponse>, response: RetrofitResponse<NoticeResponse>) {
                if (response.isSuccessful) {
                    btebNoticeAdapter.updateNoticeList(response.body()?.notices ?: emptyList(), true) // Update the adapter
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

    fun checkApiResponse(url: String) {
        val client = createInsecureHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("DownloadCheck", "Request Failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    Log.d("DownloadCheck", "Response Code: ${response.code}")
                    Log.d("DownloadCheck", "Content-Type: ${response.header("Content-Type")}")
                    Log.d("DownloadCheck", "Redirected URL: ${response.request.url}")
                } else {
                    Log.e("DownloadCheck", "Request Failed with status code: ${response.code}")
                }
            }
        })
    }

    private fun showError(message: String) {
        // Use an AlertDialog to show error messages
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}