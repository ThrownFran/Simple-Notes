package brillembourg.notes.simple.presentation.categories

import androidx.lifecycle.SavedStateHandle
import brillembourg.notes.simple.CoroutineTestRule
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.use_cases.categories.*
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun `get categories error, show message`() = runTest {
        //Arrange
        mockGetCategoriesError()
        setupSUT()
        //Act
        testScheduler.advanceUntilIdle()
        //Assert
        coVerify { messageManager.showError(any()) }
    }

    @Test
    fun `on create categories, if messa`() {
        TODO("Not yet implemented")
    }
}