package com.example.ecowayer.service

import com.mapbox.geojson.Point
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal data class RespostaRotas(val code: String, val routes: List<RotaApi> = emptyList())
internal data class RotaApi(val geometry: GeometriaRota)
internal data class GeometriaRota(val coordinates: List<List<Double>>)

internal interface RotasService {
    @GET("directions/v5/mapbox/driving/{coordenadas}")
    suspend fun buscar(
        @Path("coordenadas") coordenadas: String,
        @Query("access_token") token: String,
        @Query("geometries") geometria: String = "geojson",
        @Query("overview") detalhe: String = "full"
    ): RespostaRotas
}

internal class RotasRepository {
    private val service = Retrofit.Builder()
        .baseUrl("https://api.mapbox.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RotasService::class.java)

    suspend fun buscar(origem: Point, destino: Point, token: String): List<Point> {
        check(token.isNotBlank()) { "Configure o token do Mapbox para traçar rotas." }
        val resposta = service.buscar(
            "${origem.longitude()},${origem.latitude()};${destino.longitude()},${destino.latitude()}",
            token
        )
        check(resposta.code == "Ok") { "Não foi encontrada uma rota até este ponto." }
        val pontos = resposta.routes.firstOrNull()?.geometry?.coordinates.orEmpty().map {
            check(it.size >= 2) { "A rota recebida é inválida." }
            Point.fromLngLat(it[0], it[1])
        }
        check(pontos.size >= 2) { "Não foi encontrada uma rota até este ponto." }
        return pontos
    }
}
