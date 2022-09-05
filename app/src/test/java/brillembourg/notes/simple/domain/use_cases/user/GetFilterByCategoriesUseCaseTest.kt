package brillembourg.notes.simple.domain.use_cases.user

import brillembourg.notes.simple.CoroutineTestRule
import brillembourg.notes.simple.TestSchedulers
import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import brillembourg.notes.simple.domain.use_cases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class GetFilterByCategoriesUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var repository: UserPrefRepository

    @MockK
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase

    private lateinit var SUT: GetFilterByCategoriesUseCase

    @Before
    fun setUp() {
        SUT = GetFilterByCategoriesUseCase(repository, getCategoriesUseCase, TestSchedulers())
    }

    private fun mockGetFilterRepositorySuccess() {
        coEvery { repository.getFilter(any()) } returns flow {
            emit(Resource.Success(GetFilterByCategoriesUseCase.CategoriesIds(filteredCategoriesIds())))
        }
    }

    private fun mockGetFilterRepositoryError() {
        coEvery { repository.getFilter(any()) } returns flow {
            emit(Resource.Error(Exception("Error")))
        }
    }

    private fun filteredCategoriesIds() = listOf(1L, 2L, 3L)

    private fun mockGetCategoriesUseCaseError() {
        coEvery { getCategoriesUseCase.invoke(ofType(GetCategoriesUseCase.Params::class)) } returns flow {
            emit(Resource.Error(Exception("Error")))
        }
    }

    private fun mockGetCategoriesUseCaseSuccess() {
        coEvery { getCategoriesUseCase.invoke(ofType(GetCategoriesUseCase.Params::class)) } returns flow {
            emit(
                Resource.Success(
                    GetCategoriesUseCase.Result(
                        listOfCategories()
                    )
                )
            )
        }
    }

    private fun listOfCategories() = listOf(
        Category(1L, "Cat 1", 0),
        Category(2L, "Cat 2", 1),
        Category(3L, "Cat 3", 2),
        Category(4L, "Cat 3", 3),
        Category(5L, "Cat 3", 4)
    )

    @Test
    fun `execute invokes getCategoriesUseCase`() = runTest {
        //Arrange
        mockGetCategoriesUseCaseSuccess()
        val params = GetFilterByCategoriesUseCase.Params()
        //Act
        val result = SUT.invoke(params)
        //Assert
        coVerify { getCategoriesUseCase.invoke(any()) }
    }

    @Test
    fun `execute invokes getFilterIds from repository`() = runTest {
        //Arrange
        mockGetCategoriesUseCaseSuccess()
        mockGetFilterRepositorySuccess()
        //Act
        val result = SUT.invoke(GetFilterByCategoriesUseCase.Params()).collect()
        //Assert
        coVerify { repository.getFilter(any()) }
    }

    @Test
    fun `execute success, returns filtered categories ids as Category model`() = runTest {
        //Arrange
        mockGetCategoriesUseCaseSuccess()
        mockGetFilterRepositorySuccess()
        //Act
        val result = SUT.invoke(GetFilterByCategoriesUseCase.Params()).first() as Resource.Success

        //Assert

        //Size of filtered ids is equal to filtered categories
        assertEquals(filteredCategoriesIds().size, result.data.categories.size)

        //Every filtered id is match with available categories
        result.data.categories.sortedBy { it.id }
            .zip(filteredCategoriesIds())
            .forEach {
                val categoryResultId = it.first.id
                val filteredCategoryId = it.second
                assertEquals(categoryResultId, filteredCategoryId)
            }
    }

    @Test
    fun `execute get categories error, returns Error result`() = runTest {
        //Arrange
        mockGetCategoriesUseCaseError()
        mockGetFilterRepositorySuccess()
        //Act
        val result = SUT.invoke(GetFilterByCategoriesUseCase.Params()).first() as Resource.Error
        //Assert
        assertNotNull(result.exception)
    }

    @Test
    fun `execute get filter error, returns Error result`() = runTest {
        //Arrange
        mockGetCategoriesUseCaseSuccess()
        mockGetFilterRepositoryError()
        //Act
        val result = SUT.invoke(GetFilterByCategoriesUseCase.Params()).first() as Resource.Error
        //Assert
        assertNotNull(result.exception)
    }

}