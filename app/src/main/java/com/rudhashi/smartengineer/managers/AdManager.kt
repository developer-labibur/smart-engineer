package com.rudhashi.smartengineer.managers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.rudhashi.smartengineer.utils.helpers.SmartConfig

object AdManager {
    private const val TAG = "AdManager"
    var rewardedAd: RewardedAd? = null
    fun showRewardedAd(activity: Activity) {
        // Attach callback to manage ad lifecycle events
        rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed. Reloading ad.")
                rewardedAd = null
                loadRewardedAd(activity) // Reload the ad

                // Show congratulation dialog
                DialogManager.showCongratulationDialog(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

        rewardedAd?.let { ad ->
            ad.show(activity) { _ ->
                // Grant reward points
                PointManager.grantRewardPoints(activity)

                // Handle the reward.
                //val rewardAmount = rewardItem.amount
                //val rewardType = rewardItem.type
            }
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }

    }

    fun loadRewardedAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            SmartConfig.getRewardedAdsId(),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun setBannerAds(context: Context, adViewContainer: AdView, adSize: AdSize, isCollapsible: Boolean) {
        if (SmartConfig.isAdsVisible(context)){
            // Create a new ad view.
            val adView = AdView(context)
            adView.adUnitId = SmartConfig.getCollapsibleBannerAdsId()
            adView.setAdSize(adSize)
            //this.adView = adView

            // Replace ad container with new ad view.
            adViewContainer.removeAllViews()
            adViewContainer.addView(adView)
            adViewContainer.visibility = View.VISIBLE

            if (isCollapsible){
                // Create an extra parameter
                val extras = Bundle()
                extras.putString("collapsible", "bottom")

                // Start loading the ad in the background.
                val adRequest = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
                adView.loadAd(adRequest)
            } else {
                // Start loading the ad in the background.
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
            }

        } else adViewContainer.visibility = View.GONE

    }
}