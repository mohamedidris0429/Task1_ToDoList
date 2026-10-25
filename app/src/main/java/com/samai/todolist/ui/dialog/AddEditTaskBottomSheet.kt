package com.samai.todolist.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.samai.todolist.R
import com.samai.todolist.databinding.DialogAddEditTaskBinding
import com.samai.todolist.model.Category
import com.samai.todolist.model.Priority
import com.samai.todolist.model.TaskItem

class AddEditTaskBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private var taskId: String? = null
    private var initialTitle: String = ""
    private var initialDescription: String = ""
    private var initialPriority: Priority = Priority.MEDIUM
    private var initialCategory: Category = Category.GENERAL

    var onSaveListener: ((
        id: String?,
        title: String,
        description: String,
        priority: Priority,
        category: Category
    ) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskId = it.getString(ARG_TASK_ID)
            initialTitle = it.getString(ARG_TITLE, "")
            initialDescription = it.getString(ARG_DESCRIPTION, "")
            initialPriority = Priority.fromString(it.getString(ARG_PRIORITY))
            initialCategory = Category.fromString(it.getString(ARG_CATEGORY))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val isEditMode = taskId != null

        // Configure Header
        binding.tvDialogTitle.setText(
            if (isEditMode) R.string.edit_task else R.string.new_task
        )
        binding.btnSave.setText(
            if (isEditMode) R.string.save_task else R.string.add_task
        )

        // Populate initial values
        binding.etTitle.setText(initialTitle)
        binding.etDescription.setText(initialDescription)

        // Set cursor to end of text
        if (initialTitle.isNotEmpty()) {
            binding.etTitle.setSelection(initialTitle.length)
        }

        // Setup Priority Selection
        when (initialPriority) {
            Priority.LOW -> binding.chipPriorityLow.isChecked = true
            Priority.MEDIUM -> binding.chipPriorityMedium.isChecked = true
            Priority.HIGH -> binding.chipPriorityHigh.isChecked = true
        }

        // Setup Category Selection
        when (initialCategory) {
            Category.GENERAL -> binding.chipCatGeneral.isChecked = true
            Category.WORK -> binding.chipCatWork.isChecked = true
            Category.PERSONAL -> binding.chipCatPersonal.isChecked = true
            Category.STUDY -> binding.chipCatStudy.isChecked = true
        }

        // Clear error when user starts typing
        binding.etTitle.doAfterTextChanged {
            if (!it.isNullOrBlank()) {
                binding.tilTitle.error = null
            }
        }

        // Actions
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun saveTask() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""

        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.error_title_empty)
            binding.etTitle.requestFocus()
            return
        }

        if (title.length > 80) {
            binding.tilTitle.error = getString(R.string.error_title_too_long)
            binding.etTitle.requestFocus()
            return
        }

        val description = binding.etDescription.text?.toString()?.trim() ?: ""

        val selectedPriority = when (binding.chipGroupPriority.checkedChipId) {
            R.id.chipPriorityLow -> Priority.LOW
            R.id.chipPriorityHigh -> Priority.HIGH
            else -> Priority.MEDIUM
        }

        val selectedCategory = when (binding.chipGroupCategory.checkedChipId) {
            R.id.chipCatWork -> Category.WORK
            R.id.chipCatPersonal -> Category.PERSONAL
            R.id.chipCatStudy -> Category.STUDY
            else -> Category.GENERAL
        }

        onSaveListener?.invoke(
            taskId,
            title,
            description,
            selectedPriority,
            selectedCategory
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddEditTaskBottomSheet"

        private const val ARG_TASK_ID = "arg_task_id"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"
        private const val ARG_PRIORITY = "arg_priority"
        private const val ARG_CATEGORY = "arg_category"

        fun newInstance(task: TaskItem? = null): AddEditTaskBottomSheet {
            return AddEditTaskBottomSheet().apply {
                arguments = Bundle().apply {
                    task?.let {
                        putString(ARG_TASK_ID, it.id)
                        putString(ARG_TITLE, it.title)
                        putString(ARG_DESCRIPTION, it.description)
                        putString(ARG_PRIORITY, it.priority.name)
                        putString(ARG_CATEGORY, it.category.name)
                    }
                }
            }
        }
    }
}
