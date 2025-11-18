package edu.ap.project_mobile_dev.retrofit

data class Entry(
    val display_name: String?,
    val lat: String,
    val lon: String,
    val address: NominatimAddress?=null
)
data class NominatimAddress(
    val house_number: String? = null,
    val road: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null
)