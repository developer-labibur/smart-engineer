@file:Suppress("SameParameterValue")

package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.pdfviewer.PDFView
import com.github.pdfviewer.util.FitPolicy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.ThemeManager
import com.rudhashi.smartengineer.AnimationManager
import com.rudhashi.smartengineer.adapter.UnitPDFAdapter
import com.rudhashi.smartengineer.databinding.ActivitySmartEbookBinding
import com.rudhashi.smartengineer.databinding.BottomSheetUnitsBinding
import com.rudhashi.smartengineer.fragment.AnimationModeBottomSheet
import com.rudhashi.smartengineer.model.BookUnit
import com.rudhashi.smartengineer.util.PdfAnimation
import com.rudhashi.smartengineer.util.SmartConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SmartEBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmartEbookBinding
    private lateinit var pdfView: PDFView
    private lateinit var loadPdf: String

    private lateinit var pdfUrl: String
    private lateinit var pdfName: String
    private lateinit var pdfNo: String
    private lateinit var bookCode: String
    private lateinit var bookName: String

    private var isPageNumberVisible = true
    private var isPortraitMode = false

    private var currentPage = 0  // Track the current page index
    private var isPageFling = true
    private var isHorizontal = true
    private var isAutoSpacing = true


    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme based on preference
        if (ThemeManager.isDarkMode(this)) {
            setTheme(R.style.AppTheme_Dark)
            window.decorView.post {
                setStatusBarFontColor(false)
            }
        } else {
            setTheme(R.style.AppTheme_Light)
            window.decorView.post {
                setStatusBarFontColor(true)
            }
        }

        super.onCreate(savedInstanceState)

        // Make status bar transparent
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding = ActivitySmartEbookBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Ensure the content fills the entire screen, including under the status bar
        setContentView(binding.root)
        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pdfView = binding.pdfView
        pdfName = intent.getStringExtra("UName").toString()
        pdfUrl = intent.getStringExtra("UPdf").toString()
        pdfNo = intent.getStringExtra("UNo").toString()
        bookCode = intent.getStringExtra("BCode").toString()
        bookName = intent.getStringExtra("BName").toString()
        loadPdf = "${SmartConfig.PDF_SERVER_LINK}$pdfUrl"

        binding.apply {
            isLoading(true)
            downloadAndDisplayPDF(loadPdf)
            tvUnitTitle.text = pdfName

            // Set up Back Button Function
            backButton.setOnClickListener { finish() }

            // Set up TaskBar Click Listener to show Unit List in Bottom Sheet
            taskbar.setOnClickListener { showUnitsBottomSheet(bookCode, bookName, pdfName) }

            // Set up Rotate Button Action
            rotateButton.setOnClickListener { toggleRotation() }  // Rotate Button Action

            // Set up Horizontal Button
            btnHorizontal.setOnClickListener {
                isLoading(true)
                isPageFling = false
                isHorizontal = true
                isAutoSpacing = false
                downloadAndDisplayPDF(loadPdf)
                setPdfSeekBarIcon(btnHorizontal)
                btnVertical.setBackgroundResource(R.color.transparent)
                btnSwipeVertical.setBackgroundResource(R.color.transparent)
                btnSwipeHorizontal.setBackgroundResource(R.color.transparent)
            }

            // Set up Vertical Button
            btnVertical.setOnClickListener {
                isLoading(true)
                isPageFling = false
                isHorizontal = false
                isAutoSpacing = false
                downloadAndDisplayPDF(loadPdf)
                setPdfSeekBarIcon(btnVertical)
                btnHorizontal.setBackgroundResource(R.color.transparent)
                btnSwipeVertical.setBackgroundResource(R.color.transparent)
                btnSwipeHorizontal.setBackgroundResource(R.color.transparent)
            }

            // Set up Horizontal Swipe Button
            btnSwipeHorizontal.setOnClickListener {
                isLoading(true)
                isPageFling = true
                isHorizontal = true
                isAutoSpacing = true
                downloadAndDisplayPDF(loadPdf)
                setPdfSeekBarIcon(btnSwipeHorizontal)
                btnVertical.setBackgroundResource(R.color.transparent)
                btnHorizontal.setBackgroundResource(R.color.transparent)
                btnSwipeVertical.setBackgroundResource(R.color.transparent)
            }

            // Set up Vertical Swipe Button
            btnSwipeVertical.setOnClickListener {
                isLoading(true)
                isPageFling = true
                isHorizontal = false
                isAutoSpacing = true
                downloadAndDisplayPDF(loadPdf)
                setPdfSeekBarIcon(btnSwipeVertical)
                btnVertical.setBackgroundResource(R.color.transparent)
                btnHorizontal.setBackgroundResource(R.color.transparent)
                btnSwipeHorizontal.setBackgroundResource(R.color.transparent)
            }

            // Set up PDF Viewer Theme (Dark Theme and Night Theme)
            themeButton.setOnClickListener { toggleTheme() }

            // Set up Animation Button to show Bottom Sheet
            animationButton.setOnClickListener { showAnimationOptionsDialog() }

            // Set up Previous Button to go to the previous page
            previousButton.setOnClickListener {
                if (currentPage > 0) {
                    currentPage--  // Decrease the page index
                    pdfView.jumpTo(currentPage, true)  // Jump to the previous page
                }
            }

            // Set up Next Button to go to the next page
            nextButton.setOnClickListener {
                val totalPages = pdfView.pageCount
                if (currentPage < totalPages - 1) {
                    currentPage++  // Increase the page index
                    pdfView.jumpTo(currentPage, true)  // Jump to the next page
                }
            }

        }
    }

    // === === === === === === === === === === === === === === === === === === === === === === === === === === === === === === ===


    // =====================================================================================================================
    // ===============================       Download PDF Temporary and Display      =======================================
    // =====================================================================================================================
    private fun downloadAndDisplayPDF(urlPDF: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(urlPDF).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                isLoading(false)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    val file = File(filesDir, "downloaded.pdf")
                    val fos = FileOutputStream(file)
                    fos.write(body.bytes())
                    fos.close()

                    runOnUiThread {
                        // Set up PDF Viewer
                        pdfView.fromFile(file)
                            .enableSwipe(true)  // allows to block changing pages using swipe
                            .swipeHorizontal(isHorizontal)  // Enable horizontal swipe
                            .enableDoubletap(true)
                            .defaultPage(currentPage)  // Start from the currentPage
                            .onLoad { handleOnLoad() }
                            .onError { handlesOnError() }
                            .onPageError { page, t -> handlesOnPageError(page, t) }
                            .onTap { handleOnTap() } // called on single tap, return true if handled, false to toggle scroll handle visibility
                            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                            .scrollHandle(null) // Adds a default scroll handle => .scrollHandle(DefaultScrollHandle(this))
                            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                            .spacing(if (isAutoSpacing) 0 else 10)//Adds consistent spacing between pages, so the layout looks cleaner.
                            .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
                            .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
                            .fitEachPage(true) // fit each page to the view, else smaller pages are scaled relative to largest page.
                            .pageSnap(isPageFling)
                            .autoSpacing(isAutoSpacing)
                            .pageFling(isPageFling)  // Page-flipping effect
                            .nightMode(ThemeManager.isDarkMode(this@SmartEBookActivity)) // toggle night mode
                            .onPageChange { page, pageCount -> handlePageChange(page, pageCount) }
                            .load()
                    }
                }
            }
        })
    }
    // =====================================================================================================================
    // ===================================       PDF Viewer Helper Functions      ==========================================
    // =====================================================================================================================

    // Handle onError ------------------------------------------------------------------------------------------------
    private fun handlesOnError(){
        Snackbar.make(binding.root, "Failed to load PDF", Snackbar.LENGTH_LONG).setAction("Retry") { downloadAndDisplayPDF(loadPdf) }.show()
    }

    // Handle onPageError ------------------------------------------------------------------------------------------------
    private fun handlesOnPageError(page: Int, t: Throwable){
        // Show Snackbar for error notification
        Snackbar.make(binding.root,"Failed to load page ${page + 1}: ${t.message}", Snackbar.LENGTH_LONG).setAction("Retry") {downloadAndDisplayPDF(loadPdf) }.show()
    }
    // Handle onTap ------------------------------------------------------------------------------------------------
    private fun handleOnTap(): Boolean {
        // Show or hide a toolbar and bottom menu on tap
        isPageNumberVisible = !isPageNumberVisible
        binding.taskbar.visibility = if (isPageNumberVisible) View.VISIBLE else View.GONE
        binding.buttonContainer.visibility = if (isPageNumberVisible) View.VISIBLE else View.GONE
        return true // Consume the tap event
    }
    // Handle onLoad ------------------------------------------------------------------------------------------------
    private fun handleOnLoad(){
        isLoading(false)
        centerFirstPage(pdfView, isHorizontal)
    }
    // Handle page change ------------------------------------------------------------------------------------------------
    private fun handlePageChange(page: Int, pageCount: Int) {
        runOnUiThread {
            val nextPage = page + 1
            val isForward = page > currentPage // Determine navigation direction

            currentPage = page  // Update currentPage whenever the user scrolls manually

            binding.pageNumberTextView.text = "${page + 1}/$pageCount"
            updateScrollBar(page, pageCount)
            setupSeekBar(pageCount) // Call this here after page count is known
            // Apply animation based on saved preference
            if (isPageFling) when (AnimationManager.getAnimationMode(this@SmartEBookActivity)) {
                1 -> PdfAnimation.applySlideAnimation(pdfView, isForward)
                2 -> PdfAnimation.applyFlipOverAnimation(pdfView, isHorizontal)
                3 -> pdfView.triggerRightFlipAnimation()
                4 -> PdfAnimation.applyCubeRotationAnimation(pdfView, isHorizontal)
                5 -> PdfAnimation.applyShrinkExpandAnimation(pdfView)
                6 -> PdfAnimation.applyCarouselAnimation(pdfView)
                else -> {
                    // No animation
                }
            }
        }
    }
    // =====================================================================================================================
    // =======================================       Other Helper Functions      ===========================================
    // =====================================================================================================================
    // Flipping Right Animation
    private fun PDFView.triggerRightFlipAnimation() = startAnimation(AnimationUtils.loadAnimation(this@SmartEBookActivity, R.anim.flip))
    // Center PDF Pages
    private fun centerFirstPage(pdfView: PDFView, isHorizontal: Boolean) {
        if (!isHorizontal) {
            // Wait until the PDF is rendered to ensure correct dimensions
            pdfView.post {
                val pageHeight = pdfView.height
                val viewHeight = pdfView.measuredHeight

                // Calculate the offset to center the first page vertically
                val verticalOffset = (pageHeight - viewHeight) / 2
                if (verticalOffset > 0) {
                    pdfView.scrollTo(0, verticalOffset)
                }
            }
        } else {
            // No need to adjust for horizontal scrolling as it's already centered
        }
    }
    // Update ScrollBar Progress
    private fun updateScrollBar(page: Int, pageCount: Int) {
        val progress = ((page.toFloat() / (pageCount - 1).toFloat()) * 100).toInt()
        binding.pageScrollBar.progress = progress
    }
    // SeekBar Listener for User Interaction ----------------------------------------------------
    private fun setPdfSeekBarIcon(imageView: ImageView) {
        val typedValue = TypedValue()
        val theme = imageView.context.theme

        // Resolve the attribute ?attr/pdfSeekBarIcon to get its resource ID
        if (theme.resolveAttribute(R.attr.pdfSeekBarIcon, typedValue, true)) {
            val drawableResId = typedValue.resourceId
            imageView.setBackgroundResource(drawableResId)
        }
    }
    private fun setupSeekBar(pageCount: Int) {
        binding.apply {
            pageScrollBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        val page = (progress * (pageCount - 1) / 100)
                        pdfView.jumpTo(page, true) // Jump to the selected page
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }
    // Toggle Theme (PDF page color)
    private fun toggleTheme() {
        val isCurrentlyDark = ThemeManager.isDarkMode(this)
        ThemeManager.setDarkMode(this, !isCurrentlyDark)
        // Recreate the activity to apply the new theme
        recreate()
    }
    // Rotate screen (Landscape/Portrait)
    private fun toggleRotation() {
        requestedOrientation = if (isPortraitMode) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        isPortraitMode = !isPortraitMode
    }
    // =====================================================================================================================
    // ==============================       Show Animation and Units Bottom Sheet      =====================================
    // =====================================================================================================================
    private fun showAnimationOptionsDialog() {
        val bottomSheetFragment = AnimationModeBottomSheet()

        // Set the listener to handle mode selection
        bottomSheetFragment.setOnAnimationModeSelectedListener { selectedMode ->
            AnimationManager.saveAnimationMode(selectedMode, this) // Save the selected mode
        }

        // Show the BottomSheet
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }
    @SuppressLint("InflateParams")
    private fun showUnitsBottomSheet(bookCode: String, bookName: String, selectedUnit: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_units, null)
        val bottomSheetBinding = BottomSheetUnitsBinding.bind(bottomSheetView)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Fetch units from Firestore and set up RecyclerView
        val unitAdapter = UnitPDFAdapter(selectedUnit) { unit ->
            val intent = Intent(this, SmartEBookActivity::class.java)
            intent.putExtra("UName", unit.UName)
            intent.putExtra("UPdf", unit.UPdf)
            intent.putExtra("UNo", unit.UNo)
            intent.putExtra("BCode", bookCode)
            intent.putExtra("BName", bookName)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.unitsRecyclerView.layoutManager = LinearLayoutManager(this)
        bottomSheetBinding.unitsRecyclerView.adapter = unitAdapter

        bottomSheetBinding.bookTitle.text =
            resources.getString(R.string.book_name_book_code, bookName, bookCode)

        // Fetch units from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("books")
            .document(bookCode)
            .collection("BUnit")
            .orderBy("UNo", Query.Direction.ASCENDING) // Or DESCENDING for reverse order
            .get()
            .addOnSuccessListener { querySnapshot ->
                val units = querySnapshot.documents.map { document ->
                    BookUnit(
                        UName = document.getString("UName") ?: "",
                        UNo = document.getString("UNo") ?: "",
                        UPdf = document.getString("UPdf") ?: ""
                    )
                }
                unitAdapter.submitList(units)
            }

        bottomSheetDialog.show()
    }

    // =====================================================================================================================
    // =====================================       Set Status Bar Font Color      ==========================================
    // =====================================================================================================================

    @SuppressLint("ObsoleteSdkInt")
    private fun setStatusBarFontColor(isDarkText: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 10 (API 29) and above, use InsetsController for status bar appearance
            val controller = window.insetsController
            controller?.let {
                if (isDarkText) {
                    // Light status bar text (dark theme)
                    it.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    // Dark status bar text (light theme)
                    it.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android 6 to 9 (API 23 - 28), use systemUiVisibility for status bar text color
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isDarkText) {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Light text color
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Dark text color
            }
        } else {
            // For Android versions below API 23, this approach will not work
            Log.w("NightModeThemes", "Status bar color cannot be changed on versions lower than API 23.")
        }
    }


    // =====================================================================================================================
    // =======================================       PDF Setting and View      =============================================
    // =====================================================================================================================
    fun isLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.bookLoading.visibility = View.VISIBLE
            binding.seekbarLoading.visibility = View.VISIBLE
            binding.booksTab.visibility = View.INVISIBLE
        } else {
            binding.bookLoading.visibility = View.GONE
            binding.seekbarLoading.visibility = View.GONE
            binding.booksTab.visibility = View.VISIBLE
        }
    }

}