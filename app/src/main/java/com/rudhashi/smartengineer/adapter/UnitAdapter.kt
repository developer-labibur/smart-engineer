package com.rudhashi.smartengineer.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.rudhashi.smartengineer.databinding.ItemBookUnitBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.model.BookUnit
import com.rudhashi.smartengineer.util.ConvertFB
import com.rudhashi.smartengineer.view.BookDetailsActivity
import com.rudhashi.smartengineer.view.SmartEBookActivity

class UnitAdapter(
    private val context: Context,
    private val bookUnitList: List<BookUnit>,
    private val bookCode: String
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    inner class UnitViewHolder(binding: ItemBookUnitBinding) : RecyclerView.ViewHolder(binding.root) {
        val unitName: TextView = binding.unitName
        val viewPdfButton: ConstraintLayout = binding.clickUnitItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemBookUnitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unit = bookUnitList[position]

        holder.unitName.text = unit.UName

        FirebaseFirestore.getInstance().collection("books").document(bookCode).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val book = document.toObject(Book::class.java)
                    if (book != null) {
                        // Open PDF link when the button is clicked
                        holder.viewPdfButton.setOnClickListener {
                            val intent = Intent(context, SmartEBookActivity::class.java)
                            intent.putExtra("UName", unit.UName)
                            intent.putExtra("UPdf", unit.UPdf)
                            intent.putExtra("UNo", unit.UNo)
                            intent.putExtra("BCode", book.BCode)
                            intent.putExtra("BName", book.BName)
                            context.startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(context, "Book not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to fetch book details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = bookUnitList.size
}