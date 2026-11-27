package com.example.esfocar.data.remote

import com.example.esfocar.data.remote.dto.LoginRequestDto
import com.example.esfocar.model.Profesor
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsuariosApiService {

    @POST("api/v1/profesores/login")
    suspend fun login(@Body request: LoginRequestDto): Response<Profesor>

    @POST("api/v1/profesores/registro")
    suspend fun registrar(@Body profesor: Profesor): Response<Profesor>
}