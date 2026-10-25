package com.samai.todolist.model

import org.json.JSONObject
import java.util.UUID

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.GENERAL,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("description", description)
            put("isCompleted", isCompleted)
            put("priority", priority.name)
            put("category", category.name)
            put("createdAt", createdAt)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): TaskItem {
            return TaskItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "").trim(),
                description = json.optString("description", "").trim(),
                isCompleted = json.optBoolean("isCompleted", false),
                priority = Priority.fromString(json.optString("priority", Priority.MEDIUM.name)),
                category = Category.fromString(json.optString("category", Category.GENERAL.name)),
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        }

        fun fromLegacyString(raw: String): TaskItem {
            val trimmed = raw.trim()
            val isCompleted = trimmed.startsWith("✓ ")
            val title = if (isCompleted) trimmed.removePrefix("✓ ").trim() else trimmed
            return TaskItem(
                title = title.ifEmpty { "Untitled Task" },
                isCompleted = isCompleted
            )
        }
    }
}
