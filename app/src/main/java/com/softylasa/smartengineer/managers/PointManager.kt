package com.softylasa.smartengineer.managers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.utils.helpers.GetSet.getCurrentDate
import com.softylasa.smartengineer.databinding.DialogDailyRewardBinding
import com.softylasa.smartengineer.ads.SmartConfig
import com.softylasa.smartengineer.ui.activities.BookDetailsActivity
import com.softylasa.smartengineer.ui.activities.SmartPointActivity

object PointManager {
    private const val TAG = "PointManager"
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val userRef = FirebaseDatabase.getInstance().getReference("Users/$userId")
    private val userPointRef = Firebase.database.getReference("Users/$userId/points")
    private val userPointAllRef = Firebase.database.getReference("Users/$userId/pointsAll")

    // Reward 25 Smart Point Bonus. When ad is watch complete  -------------------------------------------------
    fun grantRewardPoints(context: Context) {
        if (userId == null) {
            Log.w(TAG, "User is not logged in. Cannot grant points.")
            return
        }

        val sharedPreferences = context.getSharedPreferences("RewardedAdsPrefs", Context.MODE_PRIVATE)
        val pendingPoints = sharedPreferences.getInt("pending_points", 0)

        // Attempt to fetch and update user points
        userPointRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            val updatedPoints = currentPoints + SmartConfig.WATCH_VIDEO_BONUS_POINT + pendingPoints

            userPointRef.setValue(updatedPoints).addOnSuccessListener {
                Log.d(TAG, "Reward granted successfully.")
                // Clear pending rewards after successful update
                sharedPreferences.edit().remove("pending_points").apply()
            }.addOnFailureListener { error ->
                Log.e(TAG, "Failed to update points: ${error.message}")
                saveRewardLocally(context, SmartConfig.WATCH_VIDEO_BONUS_POINT)
            }
        }.addOnFailureListener { error ->
            Log.e(TAG, "Failed to fetch user points: ${error.message}")
            saveRewardLocally(context, SmartConfig.WATCH_VIDEO_BONUS_POINT)
        }
        userPointAllRef.get().addOnSuccessListener { snapshot ->
            val currentPointsAll = snapshot.value?.toString()?.toInt() ?: 0
            val updatedPointsAll = currentPointsAll + SmartConfig.WATCH_VIDEO_BONUS_POINT + pendingPoints

            userPointAllRef.setValue(updatedPointsAll).addOnSuccessListener {
                Log.d(TAG, "Reward granted successfully.")
                // Clear pending rewards after successful update
                sharedPreferences.edit().remove("pending_points").apply()
            }.addOnFailureListener { error ->
                Log.e(TAG, "Failed to update points: ${error.message}")
                saveRewardLocally(context, SmartConfig.WATCH_VIDEO_BONUS_POINT)
            }
        }.addOnFailureListener { error ->
            Log.e(TAG, "Failed to fetch user points: ${error.message}")
            saveRewardLocally(context, SmartConfig.WATCH_VIDEO_BONUS_POINT)
        }
    }

    // Locally Store Point if internet connection is unavailable and If available then auto synchronization  ---
    @Suppress("SameParameterValue")
    private fun saveRewardLocally(context: Context, points: Int) {
        val sharedPreferences = context.getSharedPreferences("RewardedAdsPrefs", Context.MODE_PRIVATE)
        val pendingPoints = sharedPreferences.getInt("pending_points", 0) + points

        sharedPreferences.edit().putInt("pending_points", pendingPoints).apply()
        Log.d(TAG, "Reward saved locally. Pending points: $pendingPoints")
    }
    fun syncPendingRewards(context: Context) {
        val sharedPreferences = context.getSharedPreferences("RewardedAdsPrefs", Context.MODE_PRIVATE)
        val pendingPoints = sharedPreferences.getInt("pending_points", 0)

        if (pendingPoints > 0) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Log.w(TAG, "User is not logged in. Cannot sync rewards.")
                return
            }

            userPointRef.get().addOnSuccessListener { snapshot ->
                val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
                val updatedPoints = currentPoints + pendingPoints

                userPointRef.setValue(updatedPoints).addOnSuccessListener {
                    Log.d(TAG, "Pending rewards synced successfully.")
                    sharedPreferences.edit().remove("pending_points").apply()
                }.addOnFailureListener { error ->
                    Log.e(TAG, "Failed to sync pending rewards: ${error.message}")
                }
            }.addOnFailureListener { error ->
                Log.e(TAG, "Failed to fetch user points for syncing: ${error.message}")
            }

            userPointAllRef.get().addOnSuccessListener { snapshot ->
                val currentPointsAll = snapshot.value?.toString()?.toInt() ?: 0
                val updatedPointsAll = currentPointsAll + pendingPoints

                userPointAllRef.setValue(updatedPointsAll).addOnSuccessListener {
                    Log.d(TAG, "Pending rewards synced successfully.")
                    sharedPreferences.edit().remove("pending_points").apply()
                }.addOnFailureListener { error ->
                    Log.e(TAG, "Failed to sync pending rewards: ${error.message}")
                }
            }.addOnFailureListener { error ->
                Log.e(TAG, "Failed to fetch user points for syncing: ${error.message}")
            }
        } else {
            Log.d(TAG, "No pending rewards to sync.")
        }
    }

    // Check and Reward Daily App Opening Bonus  ----------------------------------------------------------------
    @SuppressLint("InflateParams")
    fun checkAndAwardDailyBonus(context: Context) {
        userRef.get().addOnSuccessListener { snapshot ->
            val currentDate = getCurrentDate() // Get today's date
            val lastBonusDate = snapshot.child("lastBonusDate").value?.toString()
            val currentPoints = snapshot.child("points").value?.toString()?.toInt() ?: 0
            val allPoints = snapshot.child("pointsAll").value?.toString()?.toInt() ?: 0

            if (lastBonusDate != currentDate) {
                // User hasn't received the daily bonus today
                val newPoints = currentPoints + SmartConfig.DAILY_LOGIN_BONUS_POINT
                val newPointsAll = allPoints + SmartConfig.DAILY_LOGIN_BONUS_POINT
                val updates = mapOf(
                    "points" to newPoints,
                    "pointsAll" to newPointsAll,
                    "lastBonusDate" to currentDate
                )
                userRef.updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
                        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_daily_reward, null)
                        val dialogBinding = DialogDailyRewardBinding.bind(dialogView)
                        builder.setView(dialogBinding.root)
                        builder.setCanceledOnTouchOutside(false)
                        builder.show()

                        dialogBinding.btnAllPoints.setOnClickListener {
                            context.startActivity(Intent(context, SmartPointActivity::class.java))
                            builder.dismiss()
                        }
                        dialogBinding.btnClose.setOnClickListener { builder.dismiss() }

                        if (builder.window != null) builder.window!!.setBackgroundDrawable(
                            ColorDrawable(0)
                        )
                        builder.show()
                    } else {
                        Toast.makeText(context, "Failed to add bonus points!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Set User Current Smart Points in TextView ----------------------------------------------------------------
    fun addSmartPoints(context: Context, addPoint: Int){
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userPointRef = Firebase.database.getReference("Users/$userId/points")
        val sharedPreferences = context.getSharedPreferences("RewardedAdsPrefs", Context.MODE_PRIVATE)
        val pendingPoints = sharedPreferences.getInt("pending_points", 0)

        if (userId == null) {
            Log.w(TAG, "User is not logged in. Cannot grant points.")
            return
        }

        // Attempt to fetch and update user points
        userPointRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            val updatedPoints = currentPoints + addPoint + pendingPoints

            userPointRef.setValue(updatedPoints).addOnSuccessListener {
                Log.d(TAG, "Reward granted successfully.")
                // Clear pending rewards after successful update
                sharedPreferences.edit().remove("pending_points").apply()
            }.addOnFailureListener { error ->
                Log.e(TAG, "Failed to update points: ${error.message}")
                //AdManager.saveRewardLocally(context, addPoint)
            }
        }.addOnFailureListener { error ->
            Log.e(TAG, "Failed to fetch user points: ${error.message}")
            //AdManager.saveRewardLocally(context, addPoint)
        }
    }

    // Every time redeem 10 points when open book details page
    fun redeemPointsOpenEBook(activity: Activity, bookCode: String) {
        userPointRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            if (SmartConfig.isAdsVisible(activity)){
                if (currentPoints >= SmartConfig.REDEEM_POINT) {
                    val newPoints = currentPoints - SmartConfig.REDEEM_POINT

                    val intent = Intent(activity, BookDetailsActivity::class.java)
                    userPointRef.setValue(newPoints)
                    intent.putExtra("BOOK_CODE", bookCode)
                    activity.startActivity(intent)

                } else {
                    DialogManager.showWatchAdDialog(activity, txtHeading = R.string.not_enough_points, txtInfo = R.string.do_not_have_enough_points)
                }
            } else {
                if (currentPoints >= SmartConfig.REDEEM_POINT) {
                    val newPoints = currentPoints - SmartConfig.REDEEM_POINT

                    val intent = Intent(activity, BookDetailsActivity::class.java)
                    userPointRef.setValue(newPoints)
                    intent.putExtra("BOOK_CODE", bookCode)
                    activity.startActivity(intent)

                } else {
                    val intent = Intent(activity, BookDetailsActivity::class.java)
                    intent.putExtra("BOOK_CODE", bookCode)
                    activity.startActivity(intent)
                }
            }
        }
    }

    // Every time redeem 4 points when open pdf
    fun redeemPointsOpenPDF() {
        userPointRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            val newPoints = currentPoints - SmartConfig.PDF_REDEEM_POINT
            userPointRef.setValue(newPoints)
        }
    }
}