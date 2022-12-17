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

    @Test
    fun name() = runTest(UnconfinedTestDispatcher()) {
        DebugProbes.install()

        val grandFatherJob = Job()
        val coroutineScope = CoroutineScope(grandFatherJob)

//        coroutineScope.launch(SupervisorJob()) {
        val me = coroutineScope.launch {
            val startTime = System.currentTimeMillis()
            work("Fran job", startTime, this)
//            delay(20)
            throw CancellationException("Did illegal thing")
        }
        val bro = coroutineScope.launch {
            delay(30)
            val startTime = System.currentTimeMillis()
//            work("Bro job", startTime, this)
            if (isActive) {
                println("Bro did job")
            }
        }
        val sis = coroutineScope.launch {
            delay(40)
            val startTime = System.currentTimeMillis()
            work("Sis job", startTime, this)
//            if (isActive) {
//                println("Sis did job")
//            }
        }

//        me.cancel()
//        bro.join()
//        sis.join()
//        me.join()
        listOf(me,bro,sis).joinAll()
        advanceUntilIdle()



//        }
//
//        val coroutineScope = CoroutineScope(SupervisorJob())
//        val grandpaJob = coroutineScope.launch() {
//            val popsJob = coroutineScope.launch() {
//
//
//                delay(50)
//                println("Pops did job")
//            }
//            delay(100)
//            println("Grandpa did job")
//        }
//        advanceUntilIdle()
    }

    @Test
    fun `second coroutine`() = runTest {
//        val context = CoroutineName("") + testScheduler
//        val coroutineScope = CoroutineScope(context)

        val job = launch() {
            val startTime = System.currentTimeMillis()
            var nextPrintTime = startTime
            var i = 0
            while (i < 5) {
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println(coroutineContext.job)
                    println("Fran job ${i++}")
                    nextPrintTime += 500L
                }
            }
//            work("Fran job", startTime, this)
        }
//        job.join()
        delay(750)
//        coroutineScope.cancel()
        job.cancel()
        println("Canceled")
    }

    private fun work(work: String, startTime: Long, coroutineScope: CoroutineScope) =
        coroutineScope.launch {
            var nextPrintTime = startTime
            var i = 0
            while (i < 5) {
                ensureActive()
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println(coroutineContext.job)
                    println("$work ${i++}")
                    nextPrintTime += 1000L
                }
            }
        }
}
