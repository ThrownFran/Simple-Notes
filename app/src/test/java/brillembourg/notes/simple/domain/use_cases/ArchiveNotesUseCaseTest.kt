package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.CoroutineTestRule
import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.domain.use_cases.notes.ArchiveNotesUseCase
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class ArchiveNotesUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val coroutineRule = CoroutineTestRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var repository: NotesRepository

    @MockK
    private lateinit var schedulers: Schedulers

    private lateinit var SUT: ArchiveNotesUseCase

    @Before
    fun setUp() {
        mockRepositorySuccess()
        coEvery { schedulers.defaultDispatcher() }.returns(coroutineRule.testDispatcher)
        SUT = ArchiveNotesUseCase(repository, schedulers)
    }

    private fun mockRepositorySuccess() {
        coEvery { repository.archiveTasks(any()) } returns Resource.Success(
            ArchiveNotesUseCase.Result(
                UiText.DynamicString("Success")
            )
        )
    }

    @Test
    fun `execute calls repository with correct params`() = runTest {
        //Arrange
        mockRepositorySuccess()
        val params = ArchiveNotesUseCase.Params(listOf(1L, 2L, 3L))
        //Act
        val result = SUT.invoke(params)
        //Assert
        coVerify { repository.archiveTasks(params) }
    }


}