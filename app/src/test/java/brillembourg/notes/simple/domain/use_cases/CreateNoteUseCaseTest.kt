package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.CoroutineTestRule
import brillembourg.notes.simple.TestSchedulers
import brillembourg.notes.simple.domain.models.Note
import brillembourg.notes.simple.domain.models.NoteWithCategories
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.domain.use_cases.notes.CreateNoteUseCase
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class CreateNoteUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var repository: NotesRepository

    private lateinit var SUT: CreateNoteUseCase

    @Before
    fun setUp() {
        SUT = CreateNoteUseCase(repository, TestSchedulers(coroutineTestRule.testDispatcher))
    }

    private fun mockRepositorySuccess(params: CreateNoteUseCase.Params) {
        coEvery { repository.createTask(any()) }.coAnswers {
            Resource.Success(
                CreateNoteUseCase.Result(
                    note = NoteWithCategories(
                        note = Note(1L, params.title, params.content, "date", 1),
                        categories = listOf()
                    ),
                    message = UiText.DynamicString("Create succcess")
                )
            )
        }
    }

    @Test
    fun `create note, repository invoked with correct params`() = runTest {
        print(this.testScheduler)
        print(coroutineTestRule.testDispatcher)
        //Arrange
        val title = "My new note"
        val content = "My content"
        val params = CreateNoteUseCase.Params(content, title)
        mockRepositorySuccess(params)

        //Act
        val result = SUT.invoke(params)

        //Assert
        coVerify {
            repository.createTask(match { params -> params.title == title && params.content == content })
        }
    }
}
