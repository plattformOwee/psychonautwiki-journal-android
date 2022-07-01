package com.example.healthassistant.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthassistant.data.room.experiences.ExperienceRepository
import com.example.healthassistant.data.substances.Substance
import com.example.healthassistant.data.substances.repositories.SubstanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    experienceRepo: ExperienceRepository,
    substanceRepo: SubstanceRepository
) : ViewModel() {

    private val allSubstancesFlow: Flow<List<Substance>> = substanceRepo.getAllSubstances()

    private val recentlyUsedNamesFlow: Flow<List<Substance>> =
        experienceRepo.getLastUsedSubstanceNamesFlow(limit = 10).map { lastUsedSubstanceNames ->
            lastUsedSubstanceNames.mapNotNull { substanceRepo.getSubstance(substanceName = it) }
        }

    private val commonNames = listOf(
        "Alcohol",
        "Caffeine",
        "Nicotine",
        "Cannabis",
        "MDMA",
        "Cocaine",
        "Amphetamine",
        "LSD",
        "Psilocybin mushrooms",
        "Ketamine",
        "Nitrous",
        "2C-B",
        "Methamphetamine",
        "GHB",
        "GBL",
        "Poppers",
        "Heroin",
        "Fentanyl",
        "Hydrocodone",
        "Oxycodone",
        "Alprazolam",
        "2C-I",
        "DMT",
        "5-MeO-DMT",
        "DOM",
        "DOI",
        "DOB",
        "Ephedrine",
        "MDA",
        "MDEA",
        "Mephedrone",
        "Mescaline",
        "Methcathinone",
        "Methylone",
        "Kratom",
        "Dextromethorphan",
        "PCP",
        "Salvia divinorum",
        "Oxymorphone",
        "Morphine",
        "Methadone",
        "Codeine",
        "Methylphenidate",
        "Pentobarbital",
        "Chlorodiazepoxide",
        "Diazepam",
        "Lorazepam",
        "Triazolam",
        "Zaleplon",
        "Zolpidem",
        "Pethidine",
        "MDPV",
    )

    private val commonSubstancesWithoutRecentsFlow: Flow<List<Substance>> =
        allSubstancesFlow.combine(recentlyUsedNamesFlow) { allSubstances, recents ->
            allSubstances.filter { substance ->
                val name = substance.name
                commonNames.contains(name) && !recents.any { it.name == name }
            }
        }

    private val allWithoutRecentsFlow =
        allSubstancesFlow.combine(recentlyUsedNamesFlow) { allSubstances, recents ->
            allSubstances.filter { !recents.contains(it) }
        }

    private val otherSubstancesFlow =
        allWithoutRecentsFlow.combine(commonSubstancesWithoutRecentsFlow) { allWithoutRecents, commons ->
            allWithoutRecents.filter { !commons.contains(it) }
        }

    private val _searchTextFlow = MutableStateFlow("")
    val searchTextFlow = _searchTextFlow.asStateFlow()

    val filteredRecentlyUsed: StateFlow<List<Substance>> =
        recentlyUsedNamesFlow.combine(searchTextFlow) { recents, searchText ->
            getMatchingSubstances(searchText, recents)
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    val filteredCommonlyUsed =
        commonSubstancesWithoutRecentsFlow.combine(searchTextFlow) { commons, searchText ->
            getMatchingSubstances(searchText, commons)
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    val filteredOthers = otherSubstancesFlow.combine(searchTextFlow) { others, searchText ->
        getMatchingSubstances(searchText, others)
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun filterSubstances(searchText: String) {
        viewModelScope.launch {
            _searchTextFlow.emit(searchText)
        }
    }

    companion object {
        fun getMatchingSubstances(
            searchText: String,
            substances: List<Substance>
        ): List<Substance> {
            return if (searchText.isEmpty()) {
                substances
            } else {
                substances.filter { substance ->
                    if (searchText.length < 3) {
                        substance.name.startsWith(prefix = searchText, ignoreCase = true) ||
                                substance.commonNames.any { commonName ->
                                    commonName.startsWith(prefix = searchText, ignoreCase = true)
                                }
                    } else {
                        substance.name.contains(other = searchText, ignoreCase = true) ||
                                substance.commonNames.any { commonName ->
                                    commonName.contains(other = searchText, ignoreCase = true)
                                }
                    }
                }
            }
        }
    }
}
