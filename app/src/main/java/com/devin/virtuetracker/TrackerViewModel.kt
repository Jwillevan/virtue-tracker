package com.devin.virtuetracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devin.virtuetracker.data.DefaultVirtues
import com.devin.virtuetracker.data.TrackedItem
import com.devin.virtuetracker.data.TrackerRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TrackerViewModel(private val repo: TrackerRepository) : ViewModel() {

    var items by mutableStateOf<List<TrackedItem>>(emptyList())
        private set

    var selectedDate by mutableStateOf<LocalDate>(LocalDate.now())
        private set

    // date string -> set of completed item ids
    private var completions: MutableMap<String, MutableSet<String>> = mutableMapOf()

    // Bumped whenever completions change so Compose recomposes.
    var completionVersion by mutableStateOf(0)
        private set

    init {
        if (!repo.isSeeded()) {
            // First launch: ship with Franklin's 13 virtues as the default list.
            items = DefaultVirtues.list
            repo.saveItems(items)
            repo.markSeeded()
        } else {
            items = repo.loadItems()
        }
        completions = repo.loadCompletions()
    }

    private fun key(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun isCompleted(itemId: String): Boolean {
        completionVersion // read so callers recompose on change
        return completions[key(selectedDate)]?.contains(itemId) == true
    }

    fun completedCount(): Int {
        completionVersion
        return completions[key(selectedDate)]?.count { id -> items.any { it.id == id && it.active } } ?: 0
    }

    fun toggle(itemId: String) {
        val k = key(selectedDate)
        val set = completions.getOrPut(k) { mutableSetOf() }
        if (!set.add(itemId)) set.remove(itemId)
        if (set.isEmpty()) completions.remove(k)
        repo.saveCompletions(completions)
        completionVersion++
    }

    fun goToPreviousDay() { selectedDate = selectedDate.minusDays(1) }

    fun goToNextDay() {
        if (selectedDate.isBefore(LocalDate.now())) selectedDate = selectedDate.plusDays(1)
    }

    fun goToToday() { selectedDate = LocalDate.now() }

    val isToday: Boolean get() = selectedDate == LocalDate.now()

    fun addItem(name: String, description: String, subItems: List<String> = emptyList(), active: Boolean = true, completed: Boolean = false) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val item = TrackedItem(name = trimmed, description = description.trim(), subItems = cleanSubItems(subItems), active = active)
        items = items + item
        repo.saveItems(items)
        if (completed) toggle(item.id)
    }

    fun updateItem(id: String, name: String, description: String, subItems: List<String>, active: Boolean, completed: Boolean) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        items = items.map {
            if (it.id == id) it.copy(name = trimmed, description = description.trim(), subItems = cleanSubItems(subItems), active = active) else it
        }
        repo.saveItems(items)
        if (isCompleted(id) != completed) toggle(id)
    }

    private fun cleanSubItems(subItems: List<String>) = subItems.map { it.trim() }.filter { it.isNotEmpty() }

    fun deleteItem(id: String) {
        items = items.filterNot { it.id == id }
        repo.saveItems(items)
        var changed = false
        for (set in completions.values) {
            if (set.remove(id)) changed = true
        }
        if (changed) {
            repo.saveCompletions(completions)
            completionVersion++
        }
    }

    fun resetToDefaultVirtues() {
        items = DefaultVirtues.list
        repo.saveItems(items)
    }

    class Factory(private val repo: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackerViewModel(repo) as T
        }
    }
}
