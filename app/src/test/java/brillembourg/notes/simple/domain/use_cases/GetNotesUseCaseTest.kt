package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetNotesUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var repository: NotesRepository

    @MockK
    private lateinit var schedulers: Schedulers

    private lateinit var SUT: GetNotesUseCase

    @Before
    fun setUp() {
        SUT = GetNotesUseCase(repository, schedulers)
        mockScheduler()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun mockScheduler() {
        coEvery { schedulers.defaultDispatcher() } returns UnconfinedTestDispatcher()
    }

    private fun mockGetTaskSuccess() {
        coEvery { repository.getTaskList(ofType(GetNotesUseCase.Params::class)) }.coAnswers {
            flow {
                emit(Resource.Success(GetNotesUseCase.Result(listOf())))
            }
        }
    }

    @Test
    fun `get task list, verify execution with correct params`() {
        //Arrange
        mockGetTaskSuccess()
        val params = GetNotesUseCase.Params(listOf())
        //Act
        val result = SUT(params)
        //Assert
        coVerify { repository.getTaskList(params) }
    }


}