/*
 * Copyright (c) 2022.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 3.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.journal

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsAndCompanions
import com.isaakhanimann.journal.ui.addingestion.time.hourLimitToSeparateIngestions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject


@HiltViewModel
class JournalViewModel @Inject constructor(
    experienceRepo: ExperienceRepository
) : ViewModel() {

    val isFavoriteEnabledFlow = MutableStateFlow(false)

    val isSearchEnabled = mutableStateOf(false)

    fun onChangeOfIsSearchEnabled(newValue: Boolean) {
        if (newValue) {
            isSearchEnabled.value = true
        } else {
            isSearchEnabled.value = false
            viewModelScope.launch {
                searchTextFlow.emit("")
            }
        }
    }

    fun onChangeFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            isFavoriteEnabledFlow.emit(isFavorite)
        }
    }

    val searchTextFlow = MutableStateFlow("")

    fun search(newSearchText: String) {
        viewModelScope.launch {
            searchTextFlow.emit(newSearchText)
        }
    }

    val currentAndPreviousExperiences =
        experienceRepo.getSortedExperiencesWithIngestionsAndCompanionsFlow()
            .combine(searchTextFlow) { experiencesWithIngestions, searchText ->
                Pair(first = experiencesWithIngestions, second = searchText)
            }
            .combine(isFavoriteEnabledFlow) { pair, isFavoriteEnabled ->
                val experiencesWithIngestions = pair.first
                val searchText = pair.second
                val filtered = if (searchText.isEmpty() && !isFavoriteEnabled) {
                    if (isFavoriteEnabled) {
                        experiencesWithIngestions.filter { it.experience.isFavorite }
                    } else {
                        experiencesWithIngestions
                    }
                } else {
                    if (isFavoriteEnabled) {
                        experiencesWithIngestions.filter {
                            it.experience.isFavorite && it.experience.title.contains(
                                other = searchText,
                                ignoreCase = true
                            )
                        }
                    } else {
                        experiencesWithIngestions.filter {
                            it.experience.title.contains(
                                other = searchText,
                                ignoreCase = true
                            )
                        }
                    }
                }
                val current = filtered.firstOrNull { experience ->
                    experience.ingestionsWithCompanions.any {
                        it.ingestion.time > Instant.now().minus(
                            hourLimitToSeparateIngestions, ChronoUnit.HOURS
                        )
                    }
                }
                val previous = if (current != null) filtered.drop(1) else filtered
                return@combine CurrentAndPreviousExperiences(
                    currentExperience = current,
                    previousExperiences = previous
                )
            }
            .stateIn(
                initialValue = CurrentAndPreviousExperiences(
                    currentExperience = null,
                    previousExperiences = emptyList()
                ),
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000)
            )
}

data class CurrentAndPreviousExperiences(
    val currentExperience: ExperienceWithIngestionsAndCompanions?,
    val previousExperiences: List<ExperienceWithIngestionsAndCompanions>
)