package com.samai.todolist

import com.samai.todolist.model.Category
import com.samai.todolist.model.Priority
import com.samai.todolist.model.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskItemTest {

    @Test
    fun testSerializationAndDeserialization() {
        val original = TaskItem(
            id = "test-123",
            title = "Build Android App",
            description = "Make sure tests pass",
            isCompleted = false,
            priority = Priority.HIGH,
            category = Category.WORK,
            createdAt = 1700000000000L
        )

        val json = original.toJson()
        val restored = TaskItem.fromJson(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.title, restored.title)
        assertEquals(original.description, restored.description)
        assertEquals(original.isCompleted, restored.isCompleted)
        assertEquals(original.priority, restored.priority)
        assertEquals(original.category, restored.category)
        assertEquals(original.createdAt, restored.createdAt)
    }

    @Test
    fun testLegacyStringMigration_incompleteTask() {
        val legacy = "Buy groceries"
        val migrated = TaskItem.fromLegacyString(legacy)

        assertEquals("Buy groceries", migrated.title)
        assertFalse(migrated.isCompleted)
        assertEquals(Priority.MEDIUM, migrated.priority)
        assertEquals(Category.GENERAL, migrated.category)
    }

    @Test
    fun testLegacyStringMigration_completedTask() {
        val legacy = "✓ Buy groceries"
        val migrated = TaskItem.fromLegacyString(legacy)

        assertEquals("Buy groceries", migrated.title)
        assertTrue(migrated.isCompleted)
    }

    @Test
    fun testEmptyLegacyStringFallback() {
        val legacy = "   "
        val migrated = TaskItem.fromLegacyString(legacy)

        assertEquals("Untitled Task", migrated.title)
    }
}
