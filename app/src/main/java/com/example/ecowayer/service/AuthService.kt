package com.example.ecowayer.service

import com.example.ecowayer.model.ApiResponse
import com.example.ecowayer.model.LoginRequest
import com.example.ecowayer.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("user/usuario/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse<LoginResponse>>

    @POST("user/usuario/logout")
    fun logout(): Call<Void>
}
