package edu.ap.project_mobile_dev.ui.add

data class Location(
    val id: String? = null,
    val name: String,
    val city: String,
    val description: String,
    val category: Category,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)