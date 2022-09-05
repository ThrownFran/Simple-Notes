package brillembourg.notes.simple

import brillembourg.notes.simple.domain.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class TestSchedulers(val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()): Schedulers {

    override fun uiDispatcher(): CoroutineDispatcher {
        return testDispatcher
    }

    override fun ioDispatcher(): CoroutineDispatcher {
        return testDispatcher
    }

    override fun defaultDispatcher(): CoroutineDispatcher {
        return testDispatcher
    }
}