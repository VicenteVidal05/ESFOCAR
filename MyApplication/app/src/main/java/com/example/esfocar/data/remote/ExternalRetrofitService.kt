package com.example.esfocar.data.remote

import com.example.esfocar.data.remote.dto.FeriadoExternoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ExternalRetrofitService {

    @GET("api/v3/NextPublicHolidays/{countryCode}")
    suspend fun obtenerProximosFeriados(
        @Path("countryCode") countryCode: String = "CL"
    ): List<FeriadoExternoDto>
}