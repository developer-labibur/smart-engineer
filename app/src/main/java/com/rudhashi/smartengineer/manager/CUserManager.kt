package com.rudhashi.smartengineer.manager

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object CUserManager {
    private var department: String = ""
    private var email: String = ""
    private var institute: String = ""
    private var joiningDate: String = ""
    private var lastBonusDate: String = ""
    private var name: String = ""
    private var picture: String = ""
    private var points: Int = 0
    private var pointsAll: Int = 0
    private var role: String = ""
    private var semester: String = ""
    private var userId: String = ""


    // Current User Department ------------------------------------------------------------------------------------------------
    private val listenersDepartments = mutableListOf<(String) -> Unit>()
    private fun updateDepartment(department: String) { CUserManager.department = department; notifyDepartmentListeners() }
    fun department(listeners: (String) -> Unit) { listenersDepartments.add(listeners) }
    private fun notifyDepartmentListeners() { for (listener in listenersDepartments) { listener(
        department
    ) } }

    // Current User Institute ------------------------------------------------------------------------------------------------
    private val listenersInstitutes = mutableListOf<(String) -> Unit>()
    private fun updateInstitute(department: String) { institute = department; notifyInstituteListeners() }
    fun institute(listeners: (String) -> Unit) { listenersInstitutes.add(listeners) }
    private fun notifyInstituteListeners() { for (listener in listenersInstitutes) { listener(
        institute
    ) } }

    // Current User Email ------------------------------------------------------------------------------------------------
    private val listenersEmails = mutableListOf<(String) -> Unit>()
    private fun updateEmail(department: String) { email = department; notifyEmailListeners() }
    fun email(listeners: (String) -> Unit) { listenersEmails.add(listeners) }
    private fun notifyEmailListeners() { for (listener in listenersEmails) { listener(email) } }

    // Current User JoiningDate ------------------------------------------------------------------------------------------------
    private val listenersJoiningDates = mutableListOf<(String) -> Unit>()
    private fun updateJoiningDate(department: String) { joiningDate = department; notifyJoiningDateListeners() }
    fun joiningDate(listeners: (String) -> Unit) { listenersJoiningDates.add(listeners) }
    private fun notifyJoiningDateListeners() { for (listener in listenersJoiningDates) { listener(
        joiningDate
    ) } }

    // Current User LastBonusDate ------------------------------------------------------------------------------------------------
    private val listenersLastBonusDates = mutableListOf<(String) -> Unit>()
    private fun updateLastBonusDate(department: String) { lastBonusDate = department; notifyLastBonusDateListeners() }
    fun lastBonusDate(listeners: (String) -> Unit) { listenersLastBonusDates.add(listeners) }
    private fun notifyLastBonusDateListeners() { for (listener in listenersLastBonusDates) { listener(
        lastBonusDate
    ) } }

    // Current User Name ------------------------------------------------------------------------------------------------
    private val listenersNames = mutableListOf<(String) -> Unit>()
    private fun updateName(update: String) { name = update; notifyNameListeners() }
    fun name(listeners: (String) -> Unit) { listenersNames.add(listeners) }
    private fun notifyNameListeners() { for (listener in listenersNames) { listener(name) } }

    // Current User Picture ------------------------------------------------------------------------------------------------
    private val listenersPictures = mutableListOf<(String) -> Unit>()
    private fun updatePicture(update: String) { picture = update; notifyPictureListeners() }
    fun picture(listeners: (String) -> Unit) { listenersPictures.add(listeners) }
    private fun notifyPictureListeners() { for (listener in listenersPictures) { listener(picture) } }
    fun removePictureListener(listener: (String) -> Unit) { listenersPictures.remove(listener) }

    // Current User Points ------------------------------------------------------------------------------------------------
    private val listenersPoints = mutableListOf<(Int) -> Unit>()
    private fun updatePoints(update: Int) { points = update; notifyPointListeners() }
    fun points(listeners: (Int) -> Unit) { listenersPoints.add(listeners) }
    private fun notifyPointListeners() { for (listener in listenersPoints) { listener(points) } }

    // Current User PointsAll ------------------------------------------------------------------------------------------------
    private val listenersPointsAll = mutableListOf<(Int) -> Unit>()
    private fun updatePointsAll(update: Int) { pointsAll = update; notifyPointAllListeners() }
    fun pointsAll(listeners: (Int) -> Unit) { listenersPointsAll.add(listeners) }
    private fun notifyPointAllListeners() { for (listener in listenersPointsAll) { listener(
        pointsAll
    ) } }

    // Current User Role ------------------------------------------------------------------------------------------------
    private val listenersRole = mutableListOf<(String) -> Unit>()
    private fun updateRole(update: String) { role = update; notifyRoleListeners() }
    fun role(listeners: (String) -> Unit) { listenersRole.add(listeners) }
    private fun notifyRoleListeners() { for (listener in listenersRole) { listener(role) } }

    // Current User Semester ------------------------------------------------------------------------------------------------
    private val listenersSemester = mutableListOf<(String) -> Unit>()
    private fun updateSemester(update: String) { semester = update; notifySemesterListeners() }
    fun semester(listeners: (String) -> Unit) { listenersSemester.add(listeners) }
    private fun notifySemesterListeners() { for (listener in listenersSemester) { listener(semester) } }

    // Current User UserId ------------------------------------------------------------------------------------------------
    private val listenersUserId = mutableListOf<(String) -> Unit>()
    private fun updateUserId(update: String) { userId = update; notifyUserIdListeners() }
    fun userId(listeners: (String) -> Unit) { listenersUserId.add(listeners) }
    private fun notifyUserIdListeners() { for (listener in listenersUserId) { listener(userId) } }



    // Initialize User Data ------------------------------------------------------------------------------------------------
    fun initialize() {
        FirebaseAuth.getInstance().currentUser?.let {
            FirebaseDatabase.getInstance().getReference("Users").child(it.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val department = snapshot.child("department").getValue(String::class.java) ?: ""
                        val email = snapshot.child("email").getValue(String::class.java) ?: ""
                        val institute = snapshot.child("institute").getValue(String::class.java) ?: ""
                        val joiningDate = snapshot.child("joiningDate").getValue(String::class.java) ?: ""
                        val lastBonusDate = snapshot.child("lastBonusDate").getValue(String::class.java) ?: ""
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val picture = snapshot.child("picture").getValue(String::class.java) ?: ""
                        val points = snapshot.child("points").getValue(Int::class.java) ?: 0
                        val pointsAll = snapshot.child("pointsAll").getValue(Int::class.java) ?: 0
                        val role = snapshot.child("role").getValue(String::class.java) ?: ""
                        val semester = snapshot.child("semester").getValue(String::class.java) ?: ""
                        val userId = snapshot.child("userId").getValue(String::class.java) ?: ""

                        updateDepartment(department)
                        updateEmail(email)
                        updateInstitute(institute)
                        updateJoiningDate(joiningDate)
                        updateLastBonusDate(lastBonusDate)
                        updateName(name)
                        updatePicture(picture)
                        updatePoints(points)
                        updatePointsAll(pointsAll)
                        updateRole(role)
                        updateSemester(semester)
                        updateUserId(userId)
                    } else Log.w("CurrentUserInfo", "User data not found.")
                }

                override fun onCancelled(error: DatabaseError) { Log.e("CurrentUserInfo", "Failed to fetch user data: ${error.message}") }
            })
        }
    }



//    fun listenToUserPoints() {
//        FirebaseAuth.getInstance().currentUser?.let {
//            FirebaseDatabase.getInstance().getReference("Users").child(it.uid).child("points")
//                .addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val updatedPoints = snapshot.getValue(Int::class.java) ?: 0
//                        updatePoints(updatedPoints)
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e("CurrentUserInfo", "Failed to fetch points: ${error.message}")
//                    }
//                })
//        }
//    }


}