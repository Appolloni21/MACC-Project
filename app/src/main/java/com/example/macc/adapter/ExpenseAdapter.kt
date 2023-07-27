package com.example.macc.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.databinding.ItemExpenseBinding
import com.example.macc.model.Expense


private const val TAG = "Expense Adapter"

class ExpenseAdapter(private val onDeleteCallback: (Expense) -> Unit,
                     private val onEditeCallback: (String) -> Unit) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val expensesList : ArrayList<Expense> = arrayListOf()

    class ExpenseViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root){
        val expenseName : TextView = binding.expenseName
        val expenseAmount: TextView = binding.total
        val expensePlace: TextView = binding.place
        val expenseOwner: TextView = binding.tagExpense
        val deleteIcon: ImageView = binding.imageView4
        val editIcon: ImageView = binding.expenseEditIcon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return expensesList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expensesList[position]
        Log.d(TAG, "expense adapter")

        holder.expenseName.text = item.name
        holder.expenseAmount.text = "Tot: $" + item.amount.toString()
        holder.expensePlace.text = item.place

        if(!item.owner.equals("Group")){
            holder.expenseOwner.text = "Pers"
            holder.expenseOwner.setBackgroundColor(Color.parseColor("#FFF44336"))
        }

        holder.deleteIcon.setOnClickListener{
            onDeleteCallback(item)
        }

        holder.editIcon.setOnClickListener {
            onEditeCallback(item.expenseID!!)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setExpensesList(expenseList : List<Expense>){
        this.expensesList.clear()
        this.expensesList.addAll(expenseList)
        notifyDataSetChanged()
    }

}