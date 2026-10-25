package com.samai.todolist.model

import androidx.annotation.ColorRes
import com.samai.todolist.R

enum class Category(
    val displayName: String,
    @ColorRes val colorRes: Int,
    @ColorRes val bgRes: Int
) {
    GENERAL("General", R.color.category_general, R.color.category_general_bg),
    WORK("Work", R.color.category_work, R.color.category_work_bg),
    PERSONAL("Personal", R.color.category_personal, R.color.category_personal_bg),
    STUDY("Study", R.color.category_study, R.color.category_study_bg);

    companion object {
        fun fromString(value: String?): Category {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: GENERAL
        }
    }
}
