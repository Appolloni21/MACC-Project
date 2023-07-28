package com.example.macc.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.databinding.ItemExpenseBinding
import com.example.macc.model.Expense
import java.text.SimpleDateFormat
import java.util.Locale


private const val TAG = "Expense Adapter"

class ExpenseAdapter(private val onDeleteCallback: (Expense) -> Unit,
                     private val onEditeCallback: (String) -> Unit) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val expensesList : ArrayList<Expense> = arrayListOf()
    private var travelMembers: Int = 0

    class ExpenseViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root){
        val expenseName : TextView = binding.expenseName
        val expenseAmount: TextView = binding.total
        val expenseAmountEach: TextView = binding.eachAmount
        val expenseDateNum: TextView = binding.dayNumberBlack
        val expenseDateStr: TextView = binding.dayText
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
        holder.expensePlace.text = item.place

        val date = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).parse(item.date!!)
        //holder.expenseDateNum.text = item.date!!.split("/")[0]
        holder.expenseDateNum.text = date!!.toString().split(" ")[2]
        holder.expenseDateStr.text = date.toString().split(" ")[0]

        if(item.owner.equals("Group")){
            val eachAmount = item.amount?.toFloat()?.div(travelMembers)
            holder.expenseAmountEach.text = "Each: $" + eachAmount.toString()
            holder.expenseAmount.text = "Tot: $" + item.amount.toString()
        } else {
            holder.expenseOwner.text = "Personal"
            holder.expenseOwner.setBackgroundColor(Color.parseColor("#FFF44336"))
            holder.expenseAmount.text = "Tot: $" + item.amount.toString()
            holder.expenseAmountEach.isVisible = false
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

    @SuppressLint("NotifyDataSetChanged")
    fun setTravelMembers(travelMembers: Int){
        this.travelMembers = travelMembers
        notifyDataSetChanged()
    }

}