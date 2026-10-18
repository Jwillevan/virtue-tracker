package com.devin.virtuetracker

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devin.virtuetracker.data.TrackedItem
import com.devin.virtuetracker.data.TrackerRepository
import com.devin.virtuetracker.ui.TrackerScreen
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackerScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var context: Context
    private lateinit var repo: TrackerRepository

    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("virtue_tracker_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        repo = TrackerRepository(context)
        repo.markSeeded()
    }

    private fun open(count: Int = 2) {
        repo.saveItems(List(count) { TrackedItem(id = "task_$it", name = "Task $it") })
        val viewModel = TrackerViewModel(repo)
        compose.setContent { MaterialTheme { TrackerScreen(viewModel) } }
    }

    @Test fun overviewRestrictsEditingToTasksTabAndEditor() {
        open()
        compose.onNodeWithText("0 / 2").assertExists()
        compose.onNodeWithContentDescription("Add task").assertDoesNotExist()
        compose.onNodeWithText("Task 0").assertHasNoClickAction()
        compose.onNodeWithText("Tasks").performClick()
        compose.onNodeWithText("Task 0").performClick()
        compose.onNodeWithText("Name").performTextReplacement("Updated task")
        compose.onNodeWithText("Sub-items (one per line)").performTextReplacement("One\nTwo")
        compose.onNodeWithContentDescription("Task completed").performScrollTo().performClick()
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("1 / 2").assertExists()
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.5f, 0f..1f))).assertExists()
        assertEquals(listOf("One", "Two"), repo.loadItems().first().subItems)
        compose.onNodeWithText("Updated task").performClick()
        compose.onNodeWithContentDescription("Task active").performScrollTo().performClick()
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("0 / 1").assertExists()
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0f, 0f..1f))).assertExists()
        compose.onNodeWithText("Overview").performClick()
        compose.onNodeWithText("Updated task").assertDoesNotExist()
        compose.onNodeWithText("Tasks").performClick()
        compose.onNodeWithText("Updated task").performClick()
        compose.onNodeWithText("Delete task").performScrollTo().performClick()
        compose.onNodeWithText("Delete", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Updated task").assertDoesNotExist()
        assertEquals(1, repo.loadItems().size)
    }

    @Test fun overviewIconTogglesCompletionAndProgressWithoutOpeningEditor() {
        open()
        compose.onNodeWithContentDescription("Mark Task 0 complete").performClick()
        compose.onNodeWithText("1 / 2").assertExists()
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.5f, 0f..1f))).assertExists()
        compose.onNodeWithText("Edit task").assertDoesNotExist()
        compose.onNodeWithText("Task 0").assertHasNoClickAction()
        compose.onNodeWithContentDescription("Add task").assertDoesNotExist()
        assertTrue(repo.loadCompletions().values.any { it.contains("task_0") })
        compose.onNodeWithContentDescription("Mark Task 0 incomplete").performClick()
        compose.onNodeWithText("0 / 2").assertExists()
        assertTrue(repo.loadCompletions().values.none { it.contains("task_0") })
    }

    @Test fun addButtonKeepsItsSizeWithManyTasksAndInactiveItemsAreRetained() {
        open(100)
        compose.onNodeWithText("Tasks").performClick()
        val before = compose.onNodeWithContentDescription("Add task").fetchSemanticsNode().boundsInRoot
        assertEquals(with(compose.density) { 56.dp.toPx() }, before.width, 0.1f)
        compose.onNodeWithContentDescription("Add task").performClick()
        compose.onNodeWithText("Name").performTextInput("Inactive new task")
        compose.onNodeWithContentDescription("Task active").performScrollTo().performClick()
        compose.onNodeWithText("Save").performClick()
        val after = compose.onNodeWithContentDescription("Add task").fetchSemanticsNode().boundsInRoot
        assertEquals(before.width, after.width, 0.1f)
        assertEquals(before.height, after.height, 0.1f)
        assertEquals(101, repo.loadItems().size)
        assertFalse(repo.loadItems().last().active)
        compose.onNodeWithText("0 / 100").assertExists()
    }
}
