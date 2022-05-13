package com.example.healthassistant.ui.home.experience

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthassistant.data.experiences.entities.ExperienceWithIngestions
import com.example.healthassistant.data.experiences.repositories.ExperienceRepository
import com.example.healthassistant.ui.main.routers.EXPERIENCE_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperienceViewModel @Inject constructor(
    repository: ExperienceRepository,
    state: SavedStateHandle
): ViewModel() {

    var experienceWithIngestions: ExperienceWithIngestions? = null

    init {
        val id = state.get<Int>(EXPERIENCE_ID_KEY)
        id?.let {
            viewModelScope.launch {
                experienceWithIngestions = repository.getExperienceWithIngestions(experienceId = it)
            }
        }
    }
}