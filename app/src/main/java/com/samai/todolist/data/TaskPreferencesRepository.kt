package com.samai.todolist.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.samai.todolist.model.TaskItem
import org.json.JSONArray
import org.json.JSONObject

class TaskPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val lock = Any()

    fun loadTasks(): List<TaskItem> = synchronized(lock) {
        val saved = prefs.getString(KEY_TASKS, "[]") ?: "[]"
        val taskList = mutableListOf<TaskItem>()
        try {
            val jsonArray = JSONArray(saved)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.get(i)
                when (item) {
                    is JSONObject -> {
                        val task = TaskItem.fromJson(item)
                        if (task.title.isNotEmpty()) {
                            taskList.add(task)
                        }
                    }
                    is String -> {
                        // Seamless backward compatibility with legacy starter app
                        val task = TaskItem.fromLegacyString(item)
                        if (task.title.isNotEmpty()) {
                            taskList.add(task)
                        }
                    }
                    else -> {
                        val task = TaskItem.fromLegacyString(item.toString())
                        if (task.title.isNotEmpty()) {
                            taskList.add(task)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saved tasks JSON: ${e.message}", e)
        }
        return taskList
    }

    fun saveTasks(tasks: List<TaskItem>): Unit = synchronized(lock) {
        try {
            val jsonArray = JSONArray()
            for (task in tasks) {
                jsonArray.put(task.toJson())
            }
            prefs.edit().putString(KEY_TASKS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting tasks to SharedPreferences: ${e.message}", e)
        }
    }

    fun addTask(task: TaskItem): List<TaskItem> = synchronized(lock) {
        val current = loadTasks().toMutableList()
        current.add(0, task) // New tasks appear at the top
        saveTasks(current)
        current
    }

    fun updateTask(task: TaskItem): List<TaskItem> = synchronized(lock) {
        val current = loadTasks().toMutableList()
        val index = current.indexOfFirst { it.id == task.id }
        if (index != -1) {
            current[index] = task
            saveTasks(current)
        }
        current
    }

    fun toggleTask(id: String): Pair<List<TaskItem>, TaskItem?> = synchronized(lock) {
        val current = loadTasks().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        var toggledTask: TaskItem? = null
        if (index != -1) {
            val existing = current[index]
            toggledTask = existing.copy(isCompleted = !existing.isCompleted)
            current[index] = toggledTask
            saveTasks(current)
        }
        Pair(current, toggledTask)
    }

    fun deleteTask(id: String): Pair<List<TaskItem>, Pair<TaskItem, Int>?> = synchronized(lock) {
        val current = loadTasks().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        var deletedInfo: Pair<TaskItem, Int>? = null
        if (index != -1) {
            val removed = current.removeAt(index)
            deletedInfo = Pair(removed, index)
            saveTasks(current)
        }
        Pair(current, deletedInfo)
    }

    fun restoreTask(task: TaskItem, preferredIndex: Int = -1): List<TaskItem> = synchronized(lock) {
        val current = loadTasks().toMutableList()
        // Prevent duplicate IDs
        if (current.none { it.id == task.id }) {
            val safeIndex = if (preferredIndex in 0..current.size) preferredIndex else 0
            current.add(safeIndex, task)
            saveTasks(current)
        }
        current
    }

    companion object {
        private const val TAG = "TaskPreferencesRepo"
        private const val PREFS_NAME = "todo"
        private const val KEY_TASKS = "tasks"
    }
}
