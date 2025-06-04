package com.taloscore.guessapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.data.model.CategoryResponse
import com.taloscore.guessapp.data.model.TopicResponse
import com.taloscore.guessapp.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(private val categoryRepository: CategoryRepository):
    ViewModel(){

    private val _categories = MutableStateFlow<ApiState<List<CategoryResponse>>?>(null)
    val categories = _categories

    private val _topics = MutableStateFlow<ApiState<List<TopicResponse>>?>(null)
    val topics = _topics

    fun categoryList(token: String){
        viewModelScope.launch {
            _categories.value = ApiState.Loading
            categoryRepository.list(token).collect {
                _categories.value = it
            }
        }
    }

    fun topicList(id:String, token: String){
        viewModelScope.launch {
            _topics.value = ApiState.Loading
            categoryRepository.listTopics(id, token).collect {
                _topics.value = it 
            }
        }
    }

}