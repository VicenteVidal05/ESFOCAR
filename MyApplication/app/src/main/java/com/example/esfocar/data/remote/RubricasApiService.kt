package com.example.esfocar.data.remote

import com.example.esfocar.model.EvaluacionRubrica
import com.example.esfocar.model.PlantillaRubrica
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RubricasApiService {

    // Plantillas
    @GET("api/v1/plantillas")
    suspend fun obtenerPlantillas(@Query("correo") correo: String): List<PlantillaRubrica>

    @POST("api/v1/plantillas")
    suspend fun crearPlantilla(@Body plantilla: PlantillaRubrica): Response<PlantillaRubrica>

    @PUT("api/v1/plantillas/{id}")
    suspend fun actualizarPlantilla(
        @Path("id") id: Int,
        @Body plantilla: PlantillaRubrica
    ): Response<PlantillaRubrica>

    @DELETE("api/v1/plantillas/{id}")
    suspend fun eliminarPlantilla(@Path("id") id: Int): Response<Unit>

    // Evaluaciones
    @GET("api/v1/evaluaciones")
    suspend fun obtenerEvaluaciones(@Query("correo") correo: String): List<EvaluacionRubrica>

    @POST("api/v1/evaluaciones")
    suspend fun crearEvaluacion(@Body evaluacion: EvaluacionRubrica): Response<EvaluacionRubrica>

    @PUT("api/v1/evaluaciones/{id}")
    suspend fun actualizarEvaluacion(
        @Path("id") id: Int,
        @Body evaluacion: EvaluacionRubrica
    ): Response<EvaluacionRubrica>

    @DELETE("api/v1/evaluaciones/{id}")
    suspend fun eliminarEvaluacion(@Path("id") id: Int): Response<Unit>
}