package com.samai.todolist.ui.adapter

import android.graphics.Paint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samai.todolist.R
import com.samai.todolist.databinding.ItemTaskBinding
import com.samai.todolist.model.TaskItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onTaskToggled: (TaskItem) -> Unit,
    private val onTaskEdit: (TaskItem) -> Unit,
    private val onTaskDelete: (TaskItem) -> Unit
) : ListAdapter<TaskItem, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val context = binding.root.context
        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        fun bind(task: TaskItem) {
            val isCompleted = task.isCompleted

            // Title styling with strike-through and subtle alpha
            binding.tvTaskTitle.text = task.title
            if (isCompleted) {
                binding.tvTaskTitle.paintFlags =
                    binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.text_strikethrough)
                )
                binding.cardTask.alpha = 0.72f
            } else {
                binding.tvTaskTitle.paintFlags =
                    binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.text_primary)
                )
                binding.cardTask.alpha = 1.0f
            }

            // Description
            if (task.description.isBlank()) {
                binding.tvTaskDescription.visibility = View.GONE
            } else {
                binding.tvTaskDescription.visibility = View.VISIBLE
                binding.tvTaskDescription.text = task.description
                if (isCompleted) {
                    binding.tvTaskDescription.setTextColor(
                        ContextCompat.getColor(context, R.color.text_strikethrough)
                    )
                } else {
                    binding.tvTaskDescription.setTextColor(
                        ContextCompat.getColor(context, R.color.text_secondary)
                    )
                }
            }

            // Checkbox state handling
            binding.cbComplete.setOnCheckedChangeListener(null)
            binding.cbComplete.isChecked = isCompleted
            binding.cbComplete.setOnClickListener {
                onTaskToggled(task)
            }

            // Priority stripe & badge
            val priorityColor = ContextCompat.getColor(context, task.priority.colorRes)
            binding.viewPriorityStripe.setBackgroundColor(priorityColor)
            binding.tvPriorityBadge.text = task.priority.displayName
            binding.tvPriorityBadge.setTextColor(priorityColor)

            // Category badge
            val categoryColor = ContextCompat.getColor(context, task.category.colorRes)
            binding.tvCategoryBadge.text = task.category.displayName
            binding.tvCategoryBadge.setTextColor(categoryColor)

            // Date / Timestamp formatting
            binding.tvCreatedAt.text = formatTimestamp(task.createdAt)

            // Actions
            binding.btnEditTask.setOnClickListener { onTaskEdit(task) }
            binding.btnDeleteTask.setOnClickListener { onTaskDelete(task) }
            binding.cardTask.setOnClickListener { onTaskEdit(task) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            return if (DateUtils.isToday(timestamp)) {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Today, ${timeFormat.format(Date(timestamp))}"
            } else {
                dateFormat.format(Date(timestamp))
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem == newItem
        }
    }
}
