package com.shinh.mealody.ui.screens.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinh.mealody.data.database.entity.ShopEntity
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
class NoteViewModel @Inject constructor(
    private val repository: MealodyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Int = checkNotNull(savedStateHandle["noteId"])
    // お気に入りノートかどうか
    private val _isFavoriteNote = MutableStateFlow(noteId == MealodyRepository.FAVORITES_NOTE_ID)
    val isFavoriteNote: StateFlow<Boolean> = _isFavoriteNote.asStateFlow()

    val note = repository.getNoteById(noteId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _shops = MutableStateFlow<List<ShopEntity>>(emptyList())
    val shops: StateFlow<List<ShopEntity>> = _shops.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editingName = MutableStateFlow("")
    val editingName: StateFlow<String> = _editingName.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadShopsForNote()
    }

    private fun loadShopsForNote() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getShopsForNote(noteId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )
                    .collect { shopList ->
                        _shops.value = shopList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = "ノート内のお店情報の取得に失敗しました: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ノート名編集開始
    fun startEditing() {
        // お気に入りノートは編集不可
        if (_isFavoriteNote.value) return

        note.value?.let { currentNote ->
            _editingName.value = currentNote.name
            _isEditing.value = true
        }
    }

    fun cancelEditing() {
        _isEditing.value = false
    }

    fun updateNoteName(name: String) {
        _editingName.value = name
    }

    fun saveNoteName() {
        // お気に入りノートは編集不可
        if (_isFavoriteNote.value) return

        val currentName = _editingName.value
        if (currentName.isBlank()) {
            _errorMessage.value = "ノート名を入力してください"
            return
        }

        viewModelScope.launch {
            try {
                note.value?.let { currentNote ->
                    val updatedNote = currentNote.copy(name = currentName)
                    repository.updateNote(updatedNote)
                    _isEditing.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "ノート名の更新に失敗しました: ${e.message}"
            }
        }
    }

    fun removeShopFromNote(shopId: String) {
        viewModelScope.launch {
            try {
                repository.removeShopFromNote(noteId, shopId)
            } catch (e: Exception) {
                _errorMessage.value = "お店の削除に失敗しました: ${e.message}"
            }
        }
    }
}