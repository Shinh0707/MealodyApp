package com.shinh.mealody.ui

import androidx.lifecycle.ViewModel
import com.shinh.mealody.data.repository.MealodyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MealodyRepository
) : ViewModel() {

}