package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.usecases.crosscategoriesnotes.AddCategoryToNoteUseCase
import brillembourg.notes.simple.domain.usecases.crosscategoriesnotes.GetCategoriesForNoteUseCase
import brillembourg.notes.simple.domain.usecases.crosscategoriesnotes.RemoveCategoryToNoteUseCase
import brillembourg.notes.simple.domain.usecases.notes.ArchiveNotesUseCase
import brillembourg.notes.simple.domain.usecases.notes.CreateNoteUseCase
import brillembourg.notes.simple.domain.usecases.notes.DeleteNotesUseCase
import brillembourg.notes.simple.domain.usecases.notes.GetArchivedNotesUseCase
import brillembourg.notes.simple.domain.usecases.notes.GetNotesUseCase
import brillembourg.notes.simple.domain.usecases.notes.ReorderNotesUseCase
import brillembourg.notes.simple.domain.usecases.notes.SaveNoteUseCase
import brillembourg.notes.simple.domain.usecases.notes.UnArchiveNotesUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    suspend fun createTask(params: CreateNoteUseCase.Params): Resource<CreateNoteUseCase.Result>

    suspend fun saveTask(params: SaveNoteUseCase.Params): Resource<SaveNoteUseCase.Result>

    suspend fun reorderTaskList(params: ReorderNotesUseCase.Params): Resource<ReorderNotesUseCase.Result>

    suspend fun archiveTasks(params: ArchiveNotesUseCase.Params): Resource<ArchiveNotesUseCase.Result>

    suspend fun unArchiveTasks(params: UnArchiveNotesUseCase.Params): Resource<UnArchiveNotesUseCase.Result>

    suspend fun deleteTask(params: DeleteNotesUseCase.Params): Resource<DeleteNotesUseCase.Result>

    fun getTaskList(params: GetNotesUseCase.Params): Flow<Resource<GetNotesUseCase.Result>>

    fun getArchivedTasks(params: GetArchivedNotesUseCase.Params): Flow<Resource<GetArchivedNotesUseCase.Result>>

    suspend fun addCategoryToNote(params: AddCategoryToNoteUseCase.Params): Resource<AddCategoryToNoteUseCase.Result>

    suspend fun removeCategoryToNote(params: RemoveCategoryToNoteUseCase.Params): Resource<RemoveCategoryToNoteUseCase.Result>

    fun getCategoriesForNote(params: GetCategoriesForNoteUseCase.Params): Flow<Resource<GetCategoriesForNoteUseCase.Result>>
}
