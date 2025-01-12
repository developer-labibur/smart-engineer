package com.rudhashi.smartengineer.manager

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object CUserManager {
    private var department: String = ""
    private var email: String = ""
    private var institute: String = ""
    private var joiningDate: String = ""
    private var lastBonusDate: String = ""
    private var name: String = ""
    private var picture: String = ""
    private var points: Int = 0
    private var semester: String = ""

    private var firestoreListener: ListenerRegistration? = null

    // Current User Department ------------------------------------------------------------------------------------------------
    private val listenersDepartments = mutableListOf<(String) -> Unit>()
    private fun updateDepartment(department: String) { CUserManager.department = department; notifyDepartmentListeners() }
    fun department(listeners: (String) -> Unit) { listenersDepartments.add(listeners) }
    private fun notifyDepartmentListeners() { for (listener in listenersDepartments) { listener(department) } }

    // Current User Institute ------------------------------------------------------------------------------------------------
    private val listenersInstitutes = mutableListOf<(String) -> Unit>()
    private fun updateInstitute(institute: String) { CUserManager.institute = institute; notifyInstituteListeners() }
    fun institute(listeners: (String) -> Unit) { listenersInstitutes.add(listeners) }
    private fun notifyInstituteListeners() { for (listener in listenersInstitutes) { listener(institute) } }

    // Current User Email ------------------------------------------------------------------------------------------------
    private val listenersEmails = mutableListOf<(String) -> Unit>()
    private fun updateEmail(email: String) { CUserManager.email = email; notifyEmailListeners() }
    fun email(listeners: (String) -> Unit) { listenersEmails.add(listeners) }
    private fun notifyEmailListeners() { for (listener in listenersEmails) { listener(email) } }

    // Current User JoiningDate ------------------------------------------------------------------------------------------------
    private val listenersJoiningDates = mutableListOf<(String) -> Unit>()
    private fun updateJoiningDate(joiningDate: String) { CUserManager.joiningDate = joiningDate; notifyJoiningDateListeners() }
    fun joiningDate(listeners: (String) -> Unit) { listenersJoiningDates.add(listeners) }
    private fun notifyJoiningDateListeners() { for (listener in listenersJoiningDates) { listener(joiningDate) } }

    // Current User LastBonusDate ------------------------------------------------------------------------------------------------
    private val listenersLastBonusDates = mutableListOf<(String) -> Unit>()
    private fun updateLastBonusDate(lastBonusDate: String) { CUserManager.lastBonusDate = lastBonusDate; notifyLastBonusDateListeners() }
    fun lastBonusDate(listeners: (String) -> Unit) { listenersLastBonusDates.add(listeners) }
    private fun notifyLastBonusDateListeners() { for (listener in listenersLastBonusDates) { listener(lastBonusDate) } }

    // Current User Name ------------------------------------------------------------------------------------------------
    private val listenersNames = mutableListOf<(String) -> Unit>()
    private fun updateName(name: String) { CUserManager.name = name; notifyNameListeners() }
    fun name(listeners: (String) -> Unit) { listenersNames.add(listeners) }
    private fun notifyNameListeners() { for (listener in listenersNames) { listener(name) } }

    // Current User Picture ------------------------------------------------------------------------------------------------
    private val listenersPictures = mutableListOf<(String) -> Unit>()
    private fun updatePicture(picture: String) { CUserManager.picture = picture; notifyPictureListeners() }
    fun picture(listeners: (String) -> Unit) { listenersPictures.add(listeners) }
    private fun notifyPictureListeners() { for (listener in listenersPictures) { listener(picture) } }
    fun removePictureListener(listener: (String) -> Unit) { listenersPictures.remove(listener) }

    // Current User Points ------------------------------------------------------------------------------------------------
    private val listenersPoints = mutableListOf<(Int) -> Unit>()
    private fun updatePoints(points: Int) { CUserManager.points = points; notifyPointListeners() }
    fun points(listeners: (Int) -> Unit) { listenersPoints.add(listeners) }
    private fun notifyPointListeners() { for (listener in listenersPoints) { listener(points) } }

    // Current User Semester ------------------------------------------------------------------------------------------------
    private val listenersSemester = mutableListOf<(String) -> Unit>()
    private fun updateSemester(semester: String) { CUserManager.semester = semester; notifySemesterListeners() }
    fun semester(listeners: (String) -> Unit) { listenersSemester.add(listeners) }
    private fun notifySemesterListeners() { for (listener in listenersSemester) { listener(semester) } }

    // Initialize User Data ------------------------------------------------------------------------------------------------
    fun initialize() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)

            firestoreListener?.remove() // Remove previous listener if any
            firestoreListener = userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CUserManager", "Failed to fetch user data: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val department = data?.get("department") as? String ?: ""
                    val email = data?.get("email") as? String ?: ""
                    val institute = data?.get("institute") as? String ?: ""
                    val joiningDate = data?.get("joiningDate") as? String ?: ""
                    val lastBonusDate = data?.get("lastBonusDate") as? String ?: ""
                    val name = data?.get("name") as? String ?: ""
                    val picture = data?.get("picture") as? String ?: ""
                    val points = (data?.get("points") as? Long)?.toInt() ?: 0
                    val semester = data?.get("semester") as? String ?: ""

                    updateDepartment(department)
                    updateEmail(email)
                    updateInstitute(institute)
                    updateJoiningDate(joiningDate)
                    updateLastBonusDate(lastBonusDate)
                    updateName(name)
                    updatePicture(picture)
                    updatePoints(points)
                    updateSemester(semester)
                } else {
                    Log.w("CUserManager", "User data not found.")
                }
            }
        }
    }

    // Clean up listener to avoid memory leaks
    fun cleanup() {
        firestoreListener?.remove()
        firestoreListener = null
    }
}