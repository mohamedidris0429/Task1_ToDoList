package com.samai.todolist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.samai.todolist.data.TaskPreferencesRepository
import com.samai.todolist.model.Category
import com.samai.todolist.model.Priority
import com.samai.todolist.model.TaskFilter
import com.samai.todolist.model.TaskItem
import com.samai.todolist.model.TaskStats

class TaskViewModel(
    application: Application,
    private val repository: TaskPreferencesRepository
) : AndroidViewModel(application) {

    // Mandatory public 1-argument constructor for default AndroidViewModelFactory
    constructor(application: Application) : this(
        application,
        TaskPreferencesRepository(application)
    )

    private val _tasks = MutableLiveData<List<TaskItem>>()
    val allTasks: LiveData<List<TaskItem>> = _tasks

    private val _filter = MutableLiveData(TaskFilter.ALL)
    val filter: LiveData<TaskFilter> = _filter

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private var lastDeletedTaskInfo: Pair<TaskItem, Int>? = null

    val filteredTasks = MediatorLiveData<List<TaskItem>>()
    val stats = MediatorLiveData<TaskStats>()

    init {
        filteredTasks.addSource(_tasks) { recomputeFilteredTasks() }
        filteredTasks.addSource(_filter) { recomputeFilteredTasks() }
        filteredTasks.addSource(_searchQuery) { recomputeFilteredTasks() }

        stats.addSource(_tasks) { taskList ->
            val list = taskList ?: emptyList()
            val total = list.size
            val completed = list.count { it.isCompleted }
            val pending = total - completed
            stats.value = TaskStats(total = total, completed = completed, pending = pending)
        }

        loadTasks()
    }

    fun loadTasks() {
        _tasks.value = repository.loadTasks()
    }

    private fun recomputeFilteredTasks() {
        val currentTasks = _tasks.value ?: emptyList()
        val currentFilter = _filter.value ?: TaskFilter.ALL
        val query = (_searchQuery.value ?: "").trim().lowercase()

        val filtered = currentTasks.filter { task ->
            // Filter by completion status
            val statusMatches = when (currentFilter) {
                TaskFilter.ALL -> true
                TaskFilter.PENDING -> !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
            }

            // Filter by search query
            val queryMatches = if (query.isEmpty()) {
                true
            } else {
                task.title.lowercase().contains(query) ||
                        task.description.lowercase().contains(query) ||
                        task.category.displayName.lowercase().contains(query) ||
                        task.priority.displayName.lowercase().contains(query)
            }

            statusMatches && queryMatches
        }

        // Sort: Incomplete tasks first, then by priority level descending, then latest first
        val sorted = filtered.sortedWith(
            compareBy<TaskItem> { it.isCompleted }
                .thenByDescending { it.priority.level }
                .thenByDescending { it.createdAt }
        )

        filteredTasks.value = sorted
    }

    fun addTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        category: Category = Category.GENERAL
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return false

        val newTask = TaskItem(
            title = cleanTitle,
            description = description.trim(),
            priority = priority,
            category = category
        )
        _tasks.value = repository.addTask(newTask)
        return true
    }

    fun updateTask(
        id: String,
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        category: Category = Category.GENERAL
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return false

        val currentTasks = _tasks.value ?: emptyList()
        val existing = currentTasks.firstOrNull { it.id == id } ?: return false

        val updated = existing.copy(
            title = cleanTitle,
            description = description.trim(),
            priority = priority,
            category = category
        )
        _tasks.value = repository.updateTask(updated)
        return true
    }

    fun toggleTask(task: TaskItem) {
        val (updatedList, _) = repository.toggleTask(task.id)
        _tasks.value = updatedList
    }

    fun deleteTask(task: TaskItem): TaskItem {
        val (updatedList, deletedInfo) = repository.deleteTask(task.id)
        lastDeletedTaskInfo = deletedInfo
        _tasks.value = updatedList
        return task
    }

    fun undoDelete(): Boolean {
        val info = lastDeletedTaskInfo ?: return false
        val restoredList = repository.restoreTask(info.first, info.second)
        lastDeletedTaskInfo = null
        _tasks.value = restoredList
        return true
    }

    fun setFilter(newFilter: TaskFilter) {
        if (_filter.value != newFilter) {
            _filter.value = newFilter
        }
    }

    fun setSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
        }
    }
}
