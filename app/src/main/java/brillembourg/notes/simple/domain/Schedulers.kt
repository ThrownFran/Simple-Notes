package brillembourg.notes.simple.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface Schedulers {
    fun uiDispatcher(): CoroutineDispatcher
    fun ioDispatcher(): CoroutineDispatcher
    fun defaultDispatcher(): CoroutineDispatcher
}

class SchedulersImp : Schedulers {

    override fun uiDispatcher(): CoroutineDispatcher {
        return Dispatchers.Main.immediate
    }

    override fun ioDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    override fun defaultDispatcher(): CoroutineDispatcher {
        return Dispatchers.Main
    }

}