/*
 * Copyright (c) 2022. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.journal

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsAndCompanions
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.time.hourLimitToSeparateIngestions
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
    val experienceRepo: ExperienceRepository
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

    fun createALotOfExperiencesAndIngestions() {
        viewModelScope.launch {
            val mdma = SubstanceCompanion(
                substanceName = "MDMA",
                color = AdaptiveColor.BLUE
            )
            experienceRepo.insert(mdma)
            val keta = SubstanceCompanion(
                substanceName = "Ketamine",
                color = AdaptiveColor.RED
            )
            experienceRepo.insert(keta)
            val cocaine = SubstanceCompanion(
                substanceName = "Cocaine",
                color = AdaptiveColor.GREEN
            )
            experienceRepo.insert(cocaine)
            experienceRepo.insert(mdma)
            var currentTime = Instant.now()
            for (i in 0..4000) {
                val experienceId = 1000 + i
                currentTime = currentTime.minus(1, ChronoUnit.DAYS)
                val experience = Experience(
                    title = "My random title $i",
                    text = "My random text $i",
                    creationDate = currentTime,
                    sortDate = currentTime,
                    isFavorite = false,
                    id = experienceId
                )
                experienceRepo.insert(experience)
                val ingestionTime1 = currentTime.plus(1, ChronoUnit.HOURS)
                val ingestion1 = Ingestion(
                    substanceName = "MDMA",
                    time = ingestionTime1,
                    creationDate = ingestionTime1,
                    administrationRoute = AdministrationRoute.ORAL,
                    isDoseAnEstimate = false,
                    units = "mg",
                    experienceId = experienceId,
                    notes = "Some note $i",
                    dose = 80.0
                )
                experienceRepo.insert(ingestion1)
                val ingestionTime2 = currentTime.plus(2, ChronoUnit.HOURS)
                val ingestion2 = Ingestion(
                    substanceName = "Ketamine",
                    time = ingestionTime2,
                    creationDate = ingestionTime2,
                    administrationRoute = AdministrationRoute.INSUFFLATED,
                    isDoseAnEstimate = false,
                    units = "mg",
                    experienceId = experienceId,
                    notes = "Some note $i",
                    dose = 20.0
                )
                experienceRepo.insert(ingestion2)
                val ingestionTime3 = currentTime.plus(3, ChronoUnit.HOURS)
                val ingestion3 = Ingestion(
                    substanceName = "Cocaine",
                    time = ingestionTime3,
                    creationDate = ingestionTime3,
                    administrationRoute = AdministrationRoute.INSUFFLATED,
                    isDoseAnEstimate = false,
                    units = "mg",
                    experienceId = experienceId,
                    notes = "Some note $i",
                    dose = 30.0
                )
                experienceRepo.insert(ingestion3)
                val ingestionTime4 = currentTime.plus(4, ChronoUnit.HOURS)
                val ingestion4 = Ingestion(
                    substanceName = "MDMA",
                    time = ingestionTime4,
                    creationDate = ingestionTime4,
                    administrationRoute = AdministrationRoute.ORAL,
                    isDoseAnEstimate = false,
                    units = "mg",
                    experienceId = experienceId,
                    notes = "Some note $i",
                    dose = 180.0
                )
                experienceRepo.insert(ingestion4)
                val ingestionTime5 = currentTime.plus(5, ChronoUnit.HOURS)
                val ingestion5 = Ingestion(
                    substanceName = "MDMA",
                    time = ingestionTime5,
                    creationDate = ingestionTime5,
                    administrationRoute = AdministrationRoute.ORAL,
                    isDoseAnEstimate = false,
                    units = "mg",
                    experienceId = experienceId,
                    notes = "Some note $i",
                    dose = 80.0
                )
                experienceRepo.insert(ingestion5)
            }
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