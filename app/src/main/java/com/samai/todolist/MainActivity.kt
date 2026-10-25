package com.samai.todolist

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.samai.todolist.databinding.ActivityMainBinding
import com.samai.todolist.model.Category
import com.samai.todolist.model.Priority
import com.samai.todolist.model.TaskFilter
import com.samai.todolist.model.TaskItem
import com.samai.todolist.ui.adapter.TaskAdapter
import com.samai.todolist.ui.dialog.AddEditTaskBottomSheet
import com.samai.todolist.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFilterChips()
        setupSearchBar()
        setupActions()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskToggled = { task ->
                viewModel.toggleTask(task)
            },
            onTaskEdit = { task ->
                openEditTaskSheet(task)
            },
            onTaskDelete = { task ->
                confirmDeleteTask(task)
            }
        )

        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
            itemAnimator?.changeDuration = 200
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipPending -> TaskFilter.PENDING
                R.id.chipCompleted -> TaskFilter.COMPLETED
                else -> TaskFilter.ALL
            }
            viewModel.setFilter(filter)
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.doAfterTextChanged { text ->
            val query = text?.toString() ?: ""
            viewModel.setSearchQuery(query)
            binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    private fun setupActions() {
        binding.fabAddTask.setOnClickListener {
            openAddTaskSheet()
        }

        binding.btnEmptyAction.setOnClickListener {
            openAddTaskSheet()
        }
    }

    private fun observeViewModel() {
        // Observe filtered tasks list
        viewModel.filteredTasks.observe(this) { tasks ->
            taskAdapter.submitList(tasks)
            updateEmptyState(tasks.isEmpty())
        }

        // Observe task completion stats
        viewModel.stats.observe(this) { stats ->
            binding.chipAll.text = getString(
                R.string.filter_count_format,
                getString(R.string.filter_all),
                stats.total
            )
            binding.chipPending.text = getString(
                R.string.filter_count_format,
                getString(R.string.filter_pending),
                stats.pending
            )
            binding.chipCompleted.text = getString(
                R.string.filter_count_format,
                getString(R.string.filter_completed),
                stats.completed
            )

            if (stats.total > 0) {
                binding.cardProgress.visibility = View.VISIBLE
                binding.tvProgressRatio.text = getString(
                    R.string.progress_summary,
                    stats.completed,
                    stats.total,
                    stats.progressPercent
                )
                binding.progressIndicator.setProgressCompat(stats.progressPercent, true)
            } else {
                binding.cardProgress.visibility = View.GONE
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (!isEmpty) {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvTasks.visibility = View.VISIBLE
            return
        }

        binding.rvTasks.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE

        val currentFilter = viewModel.filter.value ?: TaskFilter.ALL
        val query = viewModel.searchQuery.value ?: ""

        when {
            query.isNotBlank() -> {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_search)
                binding.tvEmptyTitle.setText(R.string.empty_search_title)
                binding.tvEmptySubtitle.text = getString(R.string.empty_search_desc, query)
                binding.btnEmptyAction.visibility = View.GONE
            }
            currentFilter == TaskFilter.COMPLETED -> {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_tasks_empty)
                binding.tvEmptyTitle.setText(R.string.empty_completed_title)
                binding.tvEmptySubtitle.setText(R.string.empty_completed_desc)
                binding.btnEmptyAction.visibility = View.GONE
            }
            currentFilter == TaskFilter.PENDING -> {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_celebrate)
                binding.tvEmptyTitle.setText(R.string.empty_pending_title)
                binding.tvEmptySubtitle.setText(R.string.empty_pending_desc)
                binding.btnEmptyAction.visibility = View.VISIBLE
            }
            else -> {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_tasks_empty)
                binding.tvEmptyTitle.setText(R.string.empty_all_title)
                binding.tvEmptySubtitle.setText(R.string.empty_all_desc)
                binding.btnEmptyAction.visibility = View.VISIBLE
            }
        }
    }

    private fun openAddTaskSheet() {
        val sheet = AddEditTaskBottomSheet.newInstance()
        sheet.onSaveListener = { _, title, description, priority, category ->
            val success = viewModel.addTask(title, description, priority, category)
            if (success) {
                Snackbar.make(binding.rootLayout, R.string.task_added_success, Snackbar.LENGTH_SHORT).show()
            }
        }
        sheet.show(supportFragmentManager, AddEditTaskBottomSheet.TAG)
    }

    private fun openEditTaskSheet(task: TaskItem) {
        val sheet = AddEditTaskBottomSheet.newInstance(task)
        sheet.onSaveListener = { id, title, description, priority, category ->
            if (id != null) {
                val success = viewModel.updateTask(id, title, description, priority, category)
                if (success) {
                    Snackbar.make(binding.rootLayout, R.string.task_updated_success, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        sheet.show(supportFragmentManager, AddEditTaskBottomSheet.TAG)
    }

    private fun confirmDeleteTask(task: TaskItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, task.title))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTask(task)
                Snackbar.make(
                    binding.rootLayout,
                    R.string.task_deleted_message,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.undo) {
                    viewModel.undoDelete()
                }.show()
            }
            .show()
    }
}
