package com.rudhashi.smartengineer.utils.helpers

import android.content.Context
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.rudhashi.smartengineer.data.constants.Constants.ADMOB_KEY
import com.rudhashi.smartengineer.data.constants.Constants.VISIBILITY_KEY

class SmartConfig {
    companion object {
        // Smart Point Setup ------------------------------------------------------------------------------------
        const val NEW_USER_SIGNUP_BONUS_POINT = 250
        const val DAILY_LOGIN_BONUS_POINT = 100
        const val WATCH_VIDEO_BONUS_POINT = 25
        const val REDEEM_POINT = 10
        const val DOWNLOAD_PDF_REDEEM_POINT = 50
        const val PDF_REDEEM_POINT = 4
        const val LIKED_BONUS_POINT = 10
        const val UNLIKE_DEDUCT_POINT = 10

        // AdMob Ad Visibility ----------------------------------------------------------------------------------------
        private var isTestingMode: Boolean = true // ⚠️ Is testing mode enabled or disabled
        fun isAdsVisible(context: Context): Boolean{
            val sharedPreferences = context.getSharedPreferences(ADMOB_KEY, MODE_PRIVATE)
            return sharedPreferences.getBoolean(VISIBILITY_KEY, false)
        }

        // Get AdMob Ad IDs ----------------------------------------------------------------------------------------
        fun getAppOpenAdsId(): String = if (isTestingMode) TEST_APP_OPEN_ADS_ID else APP_OPEN_ADS_ID
        fun getAdaptiveBannerAdsId(): String = if (isTestingMode) TEST_ADAPTIVE_BANNER_ADS_ID else BANNER_ADS_ID
        fun getCollapsibleBannerAdsId(): String = if (isTestingMode) TEST_COLLAPSIBLE_BANNER_ADS_ID else BANNER_ADS_ID
        fun getFixedSizeBannerAdsId(): String = if (isTestingMode) TEST_FIXED_SIZE_BANNER_ADS_ID else FIXED_SIZE_BANNER_ADS_ID
        fun getRewardedAdsId(): String = if (isTestingMode) TEST_REWARDED_ADS_ID else REWARDED_ADS_ID

        // Google Test Admob ID ------------------------------------------------------------------------------------
        private const val TEST_APP_OPEN_ADS_ID = "ca-app-pub-3940256099942544/9257395921"
        private const val TEST_ADAPTIVE_BANNER_ADS_ID = "ca-app-pub-3940256099942544/9214589741"
        private const val TEST_COLLAPSIBLE_BANNER_ADS_ID = "ca-app-pub-3940256099942544/2014213617"
        private const val TEST_FIXED_SIZE_BANNER_ADS_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_ADS_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_REWARDED_ADS_ID = "ca-app-pub-3940256099942544/5224354917"
        private const val TEST_REWARDED_INTERSTITIAL_ADS_ID = "ca-app-pub-3940256099942544/5354046379"
        private const val TEST_NATIVE_ADS_ID = "ca-app-pub-3940256099942544/2247696110"
        private const val TEST_NATIVE_VIDEO_ADS_ID = "ca-app-pub-3940256099942544/1044960115"

        // Google Main Admob ID  -----------------------------------------------------------------------------------
        private const val APP_OPEN_ADS_ID = "ca-app-pub-6550723267014585/3022626493"
        private const val BANNER_ADS_ID = "ca-app-pub-6550723267014585/8365618360"
        private const val FIXED_SIZE_BANNER_ADS_ID = ""
        private const val INTERSTITIAL_ADS_ID = ""
        private const val REWARDED_ADS_ID = "ca-app-pub-6550723267014585/6790217784"
        private const val REWARDED_INTERSTITIAL_ADS_ID = ""
        private const val NATIVE_ADS_ID = ""
        private const val NATIVE_VIDEO_ADS_ID = ""

        // Server Link ---------------------------------------------------------------------------------------------
        const val SERVER_LINK = "https://app.rudhashi.xyz/apps"
        const val PDF_SERVER_LINK = "https://app.rudhashi.xyz/apps/pdf"
        const val PDF_COURSE_STRUCTURE = "https://app.rudhashi.xyz/apps/department"
    }
}