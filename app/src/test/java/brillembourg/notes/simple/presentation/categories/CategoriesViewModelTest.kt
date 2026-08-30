package brillembourg.notes.simple.presentation.categories

import androidx.lifecycle.SavedStateHandle
import brillembourg.notes.simple.CoroutineTestRule
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.usecases.categories.CreateCategoryUseCase
import brillembourg.notes.simple.domain.usecases.categories.DeleteCategoriesUseCase
import brillembourg.notes.simple.domain.usecases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.domain.usecases.categories.ReorderCategoriesUseCase
import brillembourg.notes.simple.domain.usecases.categories.SaveCategoryUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class CategoriesViewModelTest {
    @get:Rule
    val rule = MockKRule(this)

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(StandardTestDispatcher())

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    @MockK
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase

    @MockK
    private lateinit var createCategoryUseCase: CreateCategoryUseCase

    @MockK
    private lateinit var saveCategoriesUseCase: SaveCategoryUseCase

    @MockK
    private lateinit var deleteCategoriesUseCase: DeleteCategoriesUseCase

    @MockK
    private lateinit var reorderCategoriesUseCase: ReorderCategoriesUseCase

    @MockK
    private lateinit var messageManager: MessageManager

    private lateinit var sut: CategoriesViewModel

    private val categories =
        listOf(
            Category(1L, "Cat 1", 3),
            Category(2L, "Cat 2", 4),
            Category(3L, "Cat 3", 1),
            Category(4L, "Cat 4", 2),
            Category(5L, "Cat 5", 0),
        )

    @Before
    public fun setUp() {
        mockMessageManager()
    }

    private fun mockMessageManager() {
        every { messageManager.showMessage(any()) } coAnswers { Unit }
        every { messageManager.showError(any()) } coAnswers { Unit }
    }

    private fun TestScope.setupSUT() {
        sut =
            CategoriesViewModel(
                savedStateHandle = savedStateHandle,
                getCategoriesUseCase = getCategoriesUseCase,
                createCategoryUseCase = createCategoryUseCase,
                saveCategoryUseCase = saveCategoriesUseCase,
                deleteCategoriesUseCase = deleteCategoriesUseCase,
                reorderCategoriesUseCase = reorderCategoriesUseCase,
                messageManager = messageManager,
            )
        // categoryUiState/categoryList use SharingStarted.WhileSubscribed, so they only
        // emit while collected; keep a collector alive for the duration of each test.
        backgroundScope.launch { sut.categoryUiState.collect {} }
    }

    @Test
    fun `on init observe get categories use case`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            // Act

            // Assert
            coVerify { getCategoriesUseCase.invoke(any()) }
        }

    @Test
    fun `get categories is success, update list state with sorted and reversed list`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            // Assert
            val expectedList =
                categories
                    .map { it.toPresentation() }
                    .sortedBy { it.order }
                    .asReversed()

            val actualList = sut.categoryUiState.value.categoryList.data
            assertEquals(expectedList, actualList)
        }

    @Test
    fun `get categories, has error, show error`() =
        runTest {
            // Arrange
            mockGetCategoriesError()
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify { messageManager.showError(any()) }
        }

    @Test
    fun `create categories, if message is null or empty, show message`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            // Act
            sut.onCreateCategory(null)
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify { messageManager.showMessage(UiText.CategoryNameEmpty) }
            coVerify(exactly = 0) { createCategoryUseCase.invoke(any()) }
        }

    @Test
    fun `create categories, message is valid, execute create category use case with correct name`() =
        runTest {
            // Arrange
            val newName = "New category"
            mockGetCategoriesSuccess()
            mockCreateCategorySuccess(newName)
            setupSUT()
            // Act
            sut.onCreateCategory(newName)
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify {
                createCategoryUseCase.invoke(match { params -> params.name == newName })
            }
        }

    @Test
    fun `create categories, with valid name, disable create button and clear name`() =
        runTest {
            // Arrange
            val newName = "New category"
            mockGetCategoriesSuccess()
            mockCreateCategorySuccess(newName)
            setupSUT()
            // Act
            sut.onCreateCategory(newName)
            testScheduler.advanceUntilIdle()
            // Assert
            assertFalse(sut.categoryUiState.value.createCategory.isEnabled)
            assertEquals(sut.categoryUiState.value.createCategory.name, "")
        }

    @Test
    fun `create categories, is success, show message`() =
        runTest {
            // Arrange
            val newName = "New category"
            mockGetCategoriesSuccess()
            mockCreateCategorySuccess(newName)
            setupSUT()
            // Act
            sut.onCreateCategory(newName)
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify { messageManager.showMessage(UiText.CategoryCreated) }
        }

    @Test
    fun `create categories, is error, show error`() =
        runTest {
            // Arrange
            val newName = "New category"
            mockGetCategoriesSuccess()
            mockCreateCategoryError()
            setupSUT()
            // Act
            sut.onCreateCategory(newName)
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify { messageManager.showError(any()) }
        }

    @Test
    fun `on selection, update selection mode state`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            // 2 items selected
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems()
            testScheduler.advanceUntilIdle()
            // Assert
            assertTrue(sut.categoryUiState.value.selectionMode.isActive)
            assertEquals(2, sut.categoryUiState.value.selectionMode.size)
        }

    private fun selectTwoItems(ids: List<Long> = listOf(1L, 3L)) {
        ids.forEach { sut.onSelection(true, it) }
    }

    @Test
    fun `on selection dismissed, disable selection mode state`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems()
            sut.onSelectionDismissed()
            testScheduler.advanceUntilIdle()
            // Assert
            assertFalse(sut.categoryUiState.value.selectionMode.isActive)
        }

    @Test
    fun `on delete confirm, selecting two items, show delete categories confirmation with size`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems()
            sut.onDeleteConfirmCategories()
            testScheduler.advanceUntilIdle()
            // Assert
            assertNotNull(sut.categoryUiState.value.deleteConfirmation)
            assertEquals(2, sut.categoryUiState.value.deleteConfirmation?.tasksToDeleteSize)
        }

    @Test
    fun `on dismiss or cancel delete confirmation, update delete confirmation state to null`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems()
            sut.onDeleteConfirmCategories()
            sut.onDismissConfirmDeleteShown()
            testScheduler.advanceUntilIdle()
            // Assert
            assertNull(sut.categoryUiState.value.deleteConfirmation)
        }

    @Test
    fun `delete categories, selection mode state and delete confirmation state must be null`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            mockDeleteCategorySuccess(listOf(1L, 3L))
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems()
            sut.onDeleteConfirmCategories()
            sut.onDeleteCategories()
            testScheduler.advanceUntilIdle()
            // Assert
            assertNull(sut.categoryUiState.value.deleteConfirmation)
            assertFalse(sut.categoryUiState.value.selectionMode.isActive)
        }

    @Test
    fun `delete categories use case, pass correct params to delete use case`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            val tasksIdsToDelete = listOf(1L, 4L)
            mockDeleteCategorySuccess(tasksIdsToDelete)
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems(tasksIdsToDelete)
            sut.onDeleteConfirmCategories()
            sut.onDeleteCategories()
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify {
                deleteCategoriesUseCase.invoke(
                    match { params ->
                        params.ids == tasksIdsToDelete
                    },
                )
            }
        }

    @Test
    fun `delete categories, is success, show message categories deleted`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            val tasksIdsToDelete = listOf(1L, 4L)
            mockDeleteCategorySuccess(tasksIdsToDelete)
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems(tasksIdsToDelete)
            sut.onDeleteConfirmCategories()
            sut.onDeleteCategories()
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify {
                messageManager.showMessage(UiText.CategoriesDeleted)
            }
        }

    @Test
    fun `delete categories, is success, show message category deleted`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            val tasksIdsToDelete = listOf(1L, 4L)
            mockDeleteCategorySuccess(tasksIdsToDelete)
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems(tasksIdsToDelete)
            sut.onDeleteConfirmCategories()
            sut.onDeleteCategories()
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify {
                messageManager.showMessage(any())
            }
        }

    @Test
    fun `delete categories, is error ,show error`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            val tasksIdsToDelete = listOf(1L, 4L)
            mockDeleteCategoryError()
            setupSUT()
            // Act
            testScheduler.advanceUntilIdle()
            selectTwoItems(tasksIdsToDelete)
            sut.onDeleteConfirmCategories()
            sut.onDeleteCategories()
            testScheduler.advanceUntilIdle()
            // Assert
            coVerify {
                messageManager.showError(any())
            }
        }

    @Test
    fun `on reordered categories, unselect every item`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            mockReorderSuccess()
            setupSUT()
            // Act
            val reorderedList = reorderList()
            sut.onReorderedCategories(reorderedList)
            advanceUntilIdle()
            // Assert
            sut.categoryUiState.value.categoryList.data.forEach {
                assertFalse(it.isSelected)
            }
        }

    private fun reorderList(): List<CategoryPresentationModel> {
        val newList = categories.map { it.toPresentation() }
        newList.shuffled()
            .mapIndexed { index, item -> item.copy(order = index, isSelected = true) }
        return newList
    }

    @Test
    fun `on reordered categories, selection mode state null`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            mockReorderSuccess()
            setupSUT()
            // Act
            advanceUntilIdle()
            val reorderedList: List<CategoryPresentationModel> = reorderList()
            sut.onReorderedCategories(reorderedList)
            advanceUntilIdle()
            // Assert
            assertFalse(sut.categoryUiState.value.selectionMode.isActive)
        }

    @Test
    fun `on reordered categories, category list state still marked for render`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            mockReorderSuccess()
            setupSUT()
            // Act
            advanceUntilIdle()
            val reorderedList: List<CategoryPresentationModel> = reorderList()
            sut.onReorderedCategories(reorderedList)
            advanceUntilIdle()
            // Assert
            assertTrue(sut.categoryUiState.value.categoryList.mustRender)
        }

    @Test
    fun `on reordered categories, success, show message`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            mockReorderSuccess()
            setupSUT()
            // Act
            advanceUntilIdle()
            val reorderedList: List<CategoryPresentationModel> = reorderList()
            sut.onReorderedCategories(reorderedList)
            advanceUntilIdle()
            // Assert
            coVerify { messageManager.showMessage(UiText.NotesReordered) }
        }

    @Test
    fun `on reordered categories, success, show error`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            mockReorderError()
            setupSUT()
            // Act
            advanceUntilIdle()
            val reorderedList: List<CategoryPresentationModel> = reorderList()
            sut.onReorderedCategories(reorderedList)
            advanceUntilIdle()
            // Assert
            coVerify { messageManager.showError(any()) }
        }

    @Test
    fun `on Save, execute save use case with correct params`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            mockSaveSuccess()
            setupSUT()
            // Act
            advanceUntilIdle()
            val savedItem = sut.categoryUiState.value.categoryList.data[2]
            val newName = "New name"
            sut.onSave(newName, savedItem)
            advanceUntilIdle()
            // Assert
            coVerify { saveCategoriesUseCase.invoke(match { params -> params.category.name == newName }) }
            coVerify { saveCategoriesUseCase.invoke(match { params -> params.category.order == savedItem.order }) }
            coVerify { saveCategoriesUseCase.invoke(match { params -> params.category.id == savedItem.id }) }
        }

    @Test
    fun `on Save, is success, show message`() =
        runTest {
            // Arrange
            mockSaveSuccess()
            mockGetCategoriesSuccess()
            setupSUT()
            // Act
            advanceUntilIdle()
            val savedItem = sut.categoryUiState.value.categoryList.data[2]
            val newName = "New name"
            sut.onSave(newName, savedItem)
            advanceUntilIdle()
            // Assert
            coVerify { messageManager.showMessage(any()) }
        }

    @Test
    fun `on Save, is error, show error`() =
        runTest {
            // Arrange
            mockGetCategoriesSuccess()
            mockSaveError()
            setupSUT()
            // Act
            advanceUntilIdle()
            val savedItem = sut.categoryUiState.value.categoryList.data[2]
            val newName = "New name"
            sut.onSave(newName, savedItem)
            advanceUntilIdle()
            // Assert
            coVerify { messageManager.showError(any()) }
        }

    //region Mocks

    private fun mockReorderSuccess() {
        coEvery { reorderCategoriesUseCase.invoke(any()) }.returns(
            Resource.Success(ReorderCategoriesUseCase.Result(UiText.NotesReordered)),
        )
    }

    private fun mockSaveSuccess() {
        coEvery { saveCategoriesUseCase.invoke(any()) }.returns(
            Resource.Success(SaveCategoryUseCase.Result(UiText.NoteUpdated)),
        )
    }

    private fun mockSaveError() {
        coEvery { saveCategoriesUseCase.invoke(any()) }.returns(
            Resource.Error(Exception()),
        )
    }

    private fun mockReorderError() {
        coEvery { reorderCategoriesUseCase.invoke(any()) }.returns(
            Resource.Error(Exception()),
        )
    }

    private fun mockDeleteCategorySuccess(ids: List<Long>) {
        coEvery { deleteCategoriesUseCase.invoke(match { params -> ids == params.ids }) }.returns(
            Resource.Success(
                DeleteCategoriesUseCase.Result(
                    if (ids.size > 1) UiText.CategoriesDeleted else UiText.CategoryDeleted,
                ),
            ),
        )
    }

    private fun mockDeleteCategoryError() {
        coEvery { deleteCategoriesUseCase.invoke(any()) }
            .returns(Resource.Error(Exception("error")))
    }

    private fun mockCreateCategorySuccess(categoryName: String) {
        coEvery { createCategoryUseCase.invoke(any()) }
            .returns(
                Resource.Success(
                    CreateCategoryUseCase.Result(
                        Category(Math.random().toLong(), categoryName, 20),
                        UiText.CategoryCreated,
                    ),
                ),
            )
    }

    private fun mockCreateCategoryError() {
        coEvery { createCategoryUseCase.invoke(any()) }
            .returns(Resource.Error(Exception("error")))
    }

    private fun mockGetCategoriesSuccess() {
        coEvery { getCategoriesUseCase.invoke(any()) }
            .coAnswers {
                flow {
                    emit(
                        Resource.Success(
                            GetCategoriesUseCase.Result(
                                categories,
                            ),
                        ),
                    )
                }
            }
    }

    private fun mockGetCategoriesError() {
        coEvery { getCategoriesUseCase.invoke(any()) }
            .coAnswers {
                flow { emit(Resource.Error(Exception("Error"))) }
            }
    }

    //endregion
}
