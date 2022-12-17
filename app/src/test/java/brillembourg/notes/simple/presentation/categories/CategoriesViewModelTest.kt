package brillembourg.notes.simple.presentation.categories

import androidx.lifecycle.SavedStateHandle
import brillembourg.notes.simple.CoroutineTestRule
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.use_cases.categories.*
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
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

    private lateinit var SUT: CategoriesViewModel

    private val CATEGORIES = listOf(
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

    private fun setupSUT() {
        SUT = CategoriesViewModel(
            savedStateHandle = savedStateHandle,
            getCategoriesUseCase = getCategoriesUseCase,
            createCategoryUseCase = createCategoryUseCase,
            saveCategoryUseCase = saveCategoriesUseCase,
            deleteCategoriesUseCase = deleteCategoriesUseCase,
            reorderCategoriesUseCase = reorderCategoriesUseCase,
            messageManager = messageManager
        )
    }

    @Test
    fun `on init observe get categories use case`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        setupSUT()
        //Act

        //Assert
        coVerify { getCategoriesUseCase.invoke(any()) }
    }

    @Test
    fun `get categories is success, update list state with sorted and reversed list`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        setupSUT()
        //Act
        testScheduler.advanceUntilIdle()
        //Assert
        val expectedList = CATEGORIES
            .map { it.toPresentation() }
            .sortedBy { it.order }
            .asReversed()

        val actualList = SUT.categoryUiState.value.categoryList.data
        assertEquals(expectedList, actualList)
    }

    @Test
    fun `get categories, has error, show error`() = runTest {
        //Arrange
        mockGetCategoriesError()
        setupSUT()
        //Act
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify { messageManager.showError(any()) }
    }

    @Test
    fun `create categories, if message is null or empty, show message`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        setupSUT()
        //Act
        SUT.onCreateCategory(null)
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify { messageManager.showMessage(UiText.CategoryNameEmpty) }
        coVerify { createCategoryUseCase.invoke(any()) wasNot called }
    }

    @Test
    fun `create categories, message is valid, execute create category use case with correct name`() =
        runTest {
            //Arrange
            val newName = "New category"
            mockGetCategoriesSuccess()
            mockCreateCategorySuccess(newName)
            setupSUT()
            //Act
            SUT.onCreateCategory(newName)
            testScheduler.advanceUntilIdle()
            //Assert
            coVerify {
                createCategoryUseCase.invoke(match { params -> params.name == newName })
            }
        }

    @Test
    fun `create categories, with valid name, disable create button and clear name`() = runTest {
        //Arrange
        val newName = "New category"
        mockGetCategoriesSuccess()
        mockCreateCategorySuccess(newName)
        setupSUT()
        //Act
        SUT.onCreateCategory(newName)
        testScheduler.advanceUntilIdle()
        //Assert
        assertFalse(SUT.categoryUiState.value.createCategory.isEnabled)
        assertEquals(SUT.categoryUiState.value.createCategory.name, "")
    }

    @Test
    fun `create categories, is success, show message`() = runTest {
        //Arrange
        val newName = "New category"
        mockGetCategoriesSuccess()
        mockCreateCategorySuccess(newName)
        setupSUT()
        //Act
        SUT.onCreateCategory(newName)
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify { messageManager.showMessage(UiText.CategoryCreated) }
    }

    @Test
    fun `create categories, is error, show error`() = runTest {
        //Arrange
        val newName = "New category"
        mockGetCategoriesSuccess()
        mockCreateCategoryError()
        setupSUT()
        //Act
        SUT.onCreateCategory(newName)
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify { messageManager.showError(any()) }
    }

    @Test
    fun `on selection, update selection mode state`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        setupSUT()
        //2 items selected
        //Act
        testScheduler.advanceUntilIdle()
        selectTwoItems()
        SUT.onSelection()
        //Assert
        assertNotNull(SUT.categoryUiState.value.selectionMode)
        assertEquals(2, SUT.categoryUiState.value.selectionMode?.size)
    }

    private fun selectTwoItems(ids: List<Long> = listOf(1L, 3L)) {
        SUT.categoryUiState.value.categoryList.data.forEachIndexed { index, categoryPresentationModel ->
            ids.forEach {
                if (it == categoryPresentationModel.id) {
                    categoryPresentationModel.isSelected = true
                }
            }
        }
    }

    @Test
    fun `on selection dismissed, disable selection mode state`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        setupSUT()
        //Act
        testScheduler.advanceUntilIdle()
        selectTwoItems()
        SUT.onSelection()
        SUT.onSelectionDismissed()
        //Assert
        assertNull(SUT.categoryUiState.value.selectionMode)
    }

    @Test
    fun `on delete confirm, selecting two items, show delete categories confirmation with size`() =
        runTest {
            //Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            //Act
            testScheduler.advanceUntilIdle()
            selectTwoItems()
            SUT.onSelection()
            SUT.onDeleteConfirmCategories()
            //Assert
            assertNotNull(SUT.categoryUiState.value.deleteConfirmation)
            assertEquals(2, SUT.categoryUiState.value.deleteConfirmation?.tasksToDeleteSize)
        }

    @Test
    fun `on dismiss or cancel delete confirmation, update delete confirmation state to null`() =
        runTest {
            //Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            //Act
            testScheduler.advanceUntilIdle()
            selectTwoItems()
            SUT.onSelection()
            SUT.onDeleteConfirmCategories()
            SUT.onDismissConfirmDeleteShown()
            //Assert
            assertNull(SUT.categoryUiState.value.deleteConfirmation)
        }

    @Test
    fun `delete categories, selection mode state and delete confirmation state must be null`() =
        runTest {
            //Arrange
            mockGetCategoriesSuccess()
            setupSUT()
            //Act
            testScheduler.advanceUntilIdle()
            selectTwoItems()
            SUT.onSelection()
            SUT.onDeleteConfirmCategories()
            SUT.onDeleteCategories()
            //Assert
            assertNull(SUT.categoryUiState.value.deleteConfirmation)
            assertNull(SUT.categoryUiState.value.selectionMode)
        }

    @Test
    fun `delete categories use case, pass correct params to delete use case`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        val tasksIdsToDelete = listOf(1L, 4L)
        mockDeleteCategorySuccess(tasksIdsToDelete)
        setupSUT()
        //Act
        testScheduler.advanceUntilIdle()
        selectTwoItems(tasksIdsToDelete)
        SUT.onSelection()
        SUT.onDeleteConfirmCategories()
        SUT.onDeleteCategories()
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify {
            deleteCategoriesUseCase.invoke(match { params ->
                params.ids == tasksIdsToDelete
            })
        }
    }

    @Test
    fun `delete categories, is success, show message categories deleted`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        val tasksIdsToDelete = listOf(1L, 4L)
        mockDeleteCategorySuccess(tasksIdsToDelete)
        setupSUT()
        //Act
        testScheduler.advanceUntilIdle()
        selectTwoItems(tasksIdsToDelete)
        SUT.onSelection()
        SUT.onDeleteConfirmCategories()
        SUT.onDeleteCategories()
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify {
            messageManager.showMessage(UiText.CategoriesDeleted)
        }
    }

    @Test
    fun `delete categories, is success, show message category deleted`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        val tasksIdsToDelete = listOf(1L, 4L)
        mockDeleteCategorySuccess(tasksIdsToDelete)
        setupSUT()
        //Act
        testScheduler.advanceUntilIdle()
        selectTwoItems(tasksIdsToDelete)
        SUT.onSelection()
        SUT.onDeleteConfirmCategories()
        SUT.onDeleteCategories()
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify {
            messageManager.showMessage(any())
        }
    }

    @Test
    fun `delete categories, is error ,show error`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        val tasksIdsToDelete = listOf(1L, 4L)
        mockDeleteCategoryError()
        setupSUT()
        //Act
        testScheduler.advanceUntilIdle()
        selectTwoItems(tasksIdsToDelete)
        SUT.onSelection()
        SUT.onDeleteConfirmCategories()
        SUT.onDeleteCategories()
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify {
            messageManager.showError(any())
        }
    }

    @Test
    fun `on reordered categories, unselect every item`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        mockReorderSuccess()
        setupSUT()
        //Act
        val reorderedList = reorderList()
        SUT.onReorderedCategories(reorderedList)
        advanceUntilIdle()
        //Assert
        SUT.categoryUiState.value.categoryList.data.forEach {
            assertFalse(it.isSelected)
        }
    }

    private fun reorderList(): List<CategoryPresentationModel> {
        val newList = CATEGORIES.map { it.toPresentation() }
        newList.shuffled()
            .mapIndexed { index, item -> item.copy(order = index, isSelected = true) }
        return newList
    }

    @Test
    fun `on reordered categories, selection mode state null`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        mockReorderSuccess()
        setupSUT()
        //Act
        advanceUntilIdle()
        val reorderedList: List<CategoryPresentationModel> = reorderList()
        SUT.onReorderedCategories(reorderedList)
        advanceUntilIdle()
        //Assert
        assertNull(SUT.categoryUiState.value.selectionMode)
    }

    @Test
    fun `on reordered categories, category list state must not render in update`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        mockReorderSuccess()
        setupSUT()
        //Act
        advanceUntilIdle()
        val reorderedList: List<CategoryPresentationModel> = reorderList()
        SUT.onReorderedCategories(reorderedList)
        advanceUntilIdle()
        //Assert
        assertFalse(SUT.categoryUiState.value.categoryList.mustRender)
    }

    @Test
    fun `on reordered categories, success, show message`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        mockReorderSuccess()
        setupSUT()
        //Act
        advanceUntilIdle()
        val reorderedList: List<CategoryPresentationModel> = reorderList()
        SUT.onReorderedCategories(reorderedList)
        advanceUntilIdle()
        //Assert
        coVerify { messageManager.showMessage(UiText.NotesReordered) }
    }

    @Test
    fun `on reordered categories, success, show error`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        mockReorderError()
        setupSUT()
        //Act
        advanceUntilIdle()
        val reorderedList: List<CategoryPresentationModel> = reorderList()
        SUT.onReorderedCategories(reorderedList)
        advanceUntilIdle()
        //Assert
        coVerify { messageManager.showError(any()) }
    }

    @Test
    fun `on Save, execute save use case with correct params`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        setupSUT()
        //Act
        advanceUntilIdle()
        val savedItem = SUT.categoryUiState.value.categoryList.data[2]
        val newName = "New name"
        SUT.onSave(newName, savedItem)
        advanceUntilIdle()
        //Assert
        coVerify { saveCategoriesUseCase.invoke(match { params -> params.category.name == newName }) }
        coVerify { saveCategoriesUseCase.invoke(match { params -> params.category.order == savedItem.order }) }
        coVerify { saveCategoriesUseCase.invoke(match { params -> params.category.id == savedItem.id }) }
    }

    @Test
    fun `on Save, is success, show message`() = runTest {
        //Arrange
        mockSaveSuccess()
        mockGetCategoriesSuccess()
        setupSUT()
        //Act
        advanceUntilIdle()
        val savedItem = SUT.categoryUiState.value.categoryList.data[2]
        val newName = "New name"
        SUT.onSave(newName, savedItem)
        advanceUntilIdle()
        //Assert
        coVerify { messageManager.showMessage(any()) }
    }

    @Test
    fun `on Save, is error, show error`() = runTest {
        //Arrange
        mockGetCategoriesSuccess()
        mockSaveError()
        setupSUT()
        //Act
        advanceUntilIdle()
        val savedItem = SUT.categoryUiState.value.categoryList.data[2]
        val newName = "New name"
        SUT.onSave(newName, savedItem)
        advanceUntilIdle()
        //Assert
        coVerify { messageManager.showError(any()) }
    }

    //region Mocks

    private fun mockReorderSuccess() {
        coEvery { reorderCategoriesUseCase.invoke(any()) }.returns(
            Resource.Success(ReorderCategoriesUseCase.Result(UiText.NotesReordered))
        )
    }

    private fun mockSaveSuccess() {
        coEvery { saveCategoriesUseCase.invoke(any()) }.returns(
            Resource.Success(SaveCategoryUseCase.Result(UiText.NoteUpdated))
        )
    }

    private fun mockSaveError() {
        coEvery { saveCategoriesUseCase.invoke(any()) }.returns(
            Resource.Error(Exception())
        )
    }

    private fun mockReorderError() {
        coEvery { reorderCategoriesUseCase.invoke(any()) }.returns(
            Resource.Error(Exception())
        )
    }

    private fun mockDeleteCategorySuccess(ids: List<Long>) {
        coEvery { deleteCategoriesUseCase.invoke(match { params -> ids == params.ids }) }.returns(
            Resource.Success(
                DeleteCategoriesUseCase.Result(
                    if (ids.size > 1) UiText.CategoriesDeleted else UiText.CategoryDeleted
                )
            )
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
                        UiText.CategoryCreated
                    )
                )
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
                                CATEGORIES
                            )
                        )
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