package edu.ap.project_mobile_dev.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
interface NominatimService {
    @Headers("User-Agent: Firefox/43.4")
    @GET("search")
    suspend fun getAddresses(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressdetails: Int = 1,
        @Query("limit") limit: Int = 5
    ): List<Entry>
}