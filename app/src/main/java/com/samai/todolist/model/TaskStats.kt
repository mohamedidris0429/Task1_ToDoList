package com.samai.todolist.model

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0
) {
    val progressPercent: Int
        get() = if (total > 0) ((completed.toFloat() / total.toFloat()) * 100).toInt() else 0
}
