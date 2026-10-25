package com.samai.todolist.model

import androidx.annotation.ColorRes
import com.samai.todolist.R

enum class Priority(
    val displayName: String,
    val level: Int,
    @ColorRes val colorRes: Int,
    @ColorRes val bgRes: Int
) {
    LOW("Low", 1, R.color.priority_low, R.color.priority_low_bg),
    MEDIUM("Medium", 2, R.color.priority_medium, R.color.priority_medium_bg),
    HIGH("High", 3, R.color.priority_high, R.color.priority_high_bg);

    companion object {
        fun fromString(value: String?): Priority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
