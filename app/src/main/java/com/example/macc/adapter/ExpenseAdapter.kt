package com.example.macc.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.R
import com.example.macc.model.Expense


private const val TAG = "Expense Adapter"

class ExpenseAdapter : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val expensesList : ArrayList<Expense> = arrayListOf()

    class ExpenseViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val expenseName : TextView = view.findViewById(R.id.expense_name)
        val expenseAmount: TextView = view.findViewById(R.id.total)
        val expensePlace: TextView = view.findViewById(R.id.place)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(itemView)
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
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setExpensesList(expenseList : List<Expense>){
        this.expensesList.clear()
        this.expensesList.addAll(expenseList)
        notifyDataSetChanged()
    }

}