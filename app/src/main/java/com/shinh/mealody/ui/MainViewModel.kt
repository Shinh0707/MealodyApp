package com.shinh.mealody.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinh.mealody.data.repository.MealodyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MealodyRepository
) : ViewModel() {

}