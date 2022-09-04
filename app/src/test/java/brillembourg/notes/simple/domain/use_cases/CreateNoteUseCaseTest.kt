package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Note
import brillembourg.notes.simple.domain.models.NoteWithCategories
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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

@RunWith(JUnit4::class)
class CreateNoteUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var repository: NotesRepository

    @MockK
    private lateinit var schedulers: Schedulers

    private lateinit var SUT: CreateNoteUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        SUT = CreateNoteUseCase(repository, schedulers)
        every { schedulers.defaultDispatcher() }.returns(
            UnconfinedTestDispatcher()
//            Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        )
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