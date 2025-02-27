package com.shinh.mealody.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinh.mealody.data.database.entity.NoteEntity
import com.shinh.mealody.data.repository.MealodyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    internal val repository: MealodyRepository
) : ViewModel() {

    val notes = repository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _favoriteShops = MutableStateFlow<List<Any>>(emptyList())
    val favoriteShops: StateFlow<List<Any>> = _favoriteShops.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadFavoriteShops()
    }

    private fun loadFavoriteShops() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getFavoriteShops()
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )
                    .collect { shops ->
                        _favoriteShops.value = shops
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = "お気に入り情報の取得に失敗しました: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun createNewNote(name: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val noteId = repository.createNote(name)
                onComplete(noteId)
            } catch (e: Exception) {
                _errorMessage.value = "ノートの作成に失敗しました: ${e.message}"
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        if (!repository.isNoteDeletable(note.id)) return

        viewModelScope.launch {
            try {
                repository.deleteNote(note)
            } catch (e: Exception) {
                _errorMessage.value = "ノートの削除に失敗しました: ${e.message}"
            }
        }
    }
}