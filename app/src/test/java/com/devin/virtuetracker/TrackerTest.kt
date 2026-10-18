package com.devin.virtuetracker

import android.content.Context
import com.devin.virtuetracker.data.TrackerRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TrackerTest {
    private lateinit var context: Context
    private lateinit var repository: TrackerRepository

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("virtue_tracker_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        repository = TrackerRepository(context)
    }

    @Test fun legacyTasksLoadAsActiveWithoutLosingHistory() {
        context.getSharedPreferences("virtue_tracker_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("seeded", true)
            .putString("items", """[{"id":"old","name":"Original","description":"Keep me"}]""")
            .putString("completions", """{"2026-10-17":["old"]}""").commit()
        val item = repository.loadItems().single()
        assertTrue(item.active)
        assertEquals(emptyList<String>(), item.subItems)
        assertEquals("Keep me", item.description)
        assertTrue(repository.loadCompletions()["2026-10-17"]!!.contains("old"))
        repository.saveItems(listOf(item.copy(subItems = listOf("One", "Two"), active = false)))
        val reloaded = TrackerRepository(context).loadItems().single()
        assertEquals("old", reloaded.id)
        assertEquals(listOf("One", "Two"), reloaded.subItems)
        assertFalse(reloaded.active)
    }

    @Test fun inactiveTasksLeaveProgressButKeepCompletionAndCanBeReactivated() {
        val vm = TrackerViewModel(repository)
        val item = vm.items.first()
        vm.updateItem(item.id, "Renamed", "Details", listOf(" First ", "", "Second"), true, true)
        assertEquals(1, vm.completedCount())
        vm.updateItem(item.id, "Renamed", "Details", listOf("First", "Second"), false, true)
        assertEquals(0, vm.completedCount())
        assertTrue(vm.isCompleted(item.id))
        val reopened = TrackerViewModel(TrackerRepository(context))
        assertFalse(reopened.items.first().active)
        assertEquals(listOf("First", "Second"), reopened.items.first().subItems)
        reopened.updateItem(item.id, "Renamed", "Details", listOf("First", "Second"), true, true)
        assertEquals(1, reopened.completedCount())
        reopened.goToPreviousDay()
        assertEquals(0, reopened.completedCount())
        reopened.goToToday()
        assertEquals(1, reopened.completedCount())
    }

    @Test fun addDeleteAndResetKeepTheirExistingBehavior() {
        val vm = TrackerViewModel(repository)
        vm.addItem(" New task ", " Description ", listOf(" Child "), active = true, completed = true)
        val item = vm.items.last()
        assertEquals("New task", item.name)
        assertEquals(listOf("Child"), item.subItems)
        assertTrue(vm.isCompleted(item.id))
        vm.deleteItem(item.id)
        assertFalse(vm.items.any { it.id == item.id })
        assertFalse(vm.isCompleted(item.id))
        assertTrue(repository.loadCompletions().values.none { it.contains(item.id) })
        val original = vm.items.first()
        vm.toggle(original.id)
        vm.resetToDefaultVirtues()
        assertEquals(13, vm.items.size)
        assertTrue(vm.isCompleted(original.id))
        vm.addItem(" ", "")
        assertEquals(13, vm.items.size)
    }
}
