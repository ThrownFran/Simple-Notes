package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.domain.use_cases.cross_categories_notes.AddCategoryToNoteUseCase
import brillembourg.notes.simple.domain.use_cases.cross_categories_notes.GetCategoriesForNoteUseCase
import brillembourg.notes.simple.domain.use_cases.cross_categories_notes.RemoveCategoryToNoteUseCase
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