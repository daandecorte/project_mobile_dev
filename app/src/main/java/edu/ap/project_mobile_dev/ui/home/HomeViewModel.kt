package edu.ap.project_mobile_dev.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import edu.ap.project_mobile_dev.ui.model.Activity

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadActivities()
    }

    private fun loadActivities() {
        val activities = listOf(
            Activity(
                id = 1,
                title = "Atomium",
                description = "Het Atomium is een 102 meter hoog bouwwerk in Brussel, ontworpen voor de Wereldtentoonstelling van 1958. Het vertegenwoordigt een ijzerkristal dat 165 miljard keer is vergroot.",
                imageUrl = "https://example.com/atomium.jpg",
                category = "Monument",
                location = "Brussel",
                city = "Brussel"
            ),
            Activity(
                id = 2,
                title = "Grote Markt",
                description = "De Grote Markt van Antwerpen is een van de mooiste pleinen van Europa, omringd door historische gildehuizen. Het plein wordt gedomineerd door het stadhuis en de Brabofontein.",
                imageUrl = "https://example.com/grote-markt.jpg",
                category = "Historisch",
                location = "Antwerpen Centrum",
                city = "Antwerpen"
            ),
            Activity(
                id = 3,
                title = "MAS Museum",
                description = "Museum aan de Stroom is een museum in Antwerpen over de stad, de haven en de internationale scheepvaart. Het gebouw zelf is een architectonisch meesterwerk met een panoramisch uitzicht.",
                imageUrl = "https://example.com/mas.jpg",
                category = "Museum",
                location = "Eilandje",
                city = "Antwerpen"
            ),
            Activity(
                id = 4,
                title = "Gravensteen",
                description = "Het Gravensteen is een middeleeuwse burcht in het centrum van Gent, gebouwd door Filips van de Elzas in 1180. Het is een van de best bewaarde waterburgen van Europa.",
                imageUrl = "https://example.com/gravensteen.jpg",
                category = "Monument",
                location = "Gent",
                city = "Gent"
            ),
            Activity(
                id = 5,
                title = "Manneken Pis",
                description = "De wereldberoemde bronzen fontein met het beeldje van een plassend ventje, symbool van Brussel. Het beeld heeft een garderobe van meer dan 1000 kostuums.",
                imageUrl = "https://example.com/manneken-pis.jpg",
                category = "Monument",
                location = "Brussel Centrum",
                city = "Brussel"
            ),
            Activity(
                id = 6,
                title = "Rubenshuis",
                description = "Het voormalige woonhuis en atelier van de barokke schilder Peter Paul Rubens in Antwerpen. Het museum toont werken van Rubens en zijn tijdgenoten in authentieke omgeving.",
                imageUrl = "https://example.com/rubenshuis.jpg",
                category = "Museum",
                location = "Antwerpen",
                city = "Antwerpen"
            ),
            Activity(
                id = 7,
                title = "Minnewater",
                description = "Het Minnewater, ook wel het 'Meer der Liefde' genoemd, is een romantisch park in Brugge met een pittoresk meer en historische sluizen.",
                imageUrl = "https://example.com/minnewater.jpg",
                category = "Natuur",
                location = "Brugge",
                city = "Brugge"
            ),
            Activity(
                id = 8,
                title = "Belfort van Brugge",
                description = "De middeleeuwse klokkentoren van 83 meter hoog is een van de belangrijkste symbolen van Brugge en biedt een prachtig uitzicht over de stad.",
                imageUrl = "https://example.com/belfort.jpg",
                category = "Historisch",
                location = "Brugge Centrum",
                city = "Brugge"
            )
        )

        _uiState.update {
            it.copy(
                activities = activities,
                filteredActivities = activities
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
        filterActivities()
    }

    fun toggleCategory(category: String) {
        _uiState.update { currentState ->
            val newCategories = if (currentState.selectedCategories.contains(category)) {
                currentState.selectedCategories - category
            } else {
                currentState.selectedCategories + category
            }
            currentState.copy(selectedCategories = newCategories)
        }
        filterActivities()
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun filterActivities() {
        _uiState.update { currentState ->
            val filtered = currentState.activities.filter { activity ->
                val matchesCategory = currentState.selectedCategories.isEmpty() ||
                        currentState.selectedCategories.contains(activity.category)
                val matchesSearch = currentState.searchQuery.isEmpty() ||
                        activity.location.contains(currentState.searchQuery, ignoreCase = true) ||
                        activity.city.contains(currentState.searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
            currentState.copy(filteredActivities = filtered)
        }
    }
}
