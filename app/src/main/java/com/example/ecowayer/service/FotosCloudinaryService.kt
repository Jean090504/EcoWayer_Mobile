package com.example.ecowayer.service

import android.content.Context
import android.net.Uri
import com.example.ecowayer.BuildConfig
import com.google.gson.annotations.SerializedName
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

internal interface FotosCloudinaryApi {
    @Multipart
    @POST("v1_1/{cloud}/image/upload")
    suspend fun enviar(
        @Path("cloud") cloud: String,
        @Part arquivo: MultipartBody.Part,
        @Part("upload_preset") preset: RequestBody
    ): Response<FotoEnviada>
}

internal data class FotoEnviada(@SerializedName("secure_url") val url: String?)

internal object FotosCloudinaryService {
    private const val LimiteFoto = 5 * 1024 * 1024
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/")
            .client(OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FotosCloudinaryApi::class.java)
    }

    suspend fun enviar(contexto: Context, uri: Uri): String {
        val cloud = BuildConfig.CLOUDINARY_CLOUD_NAME
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        check(cloud.isNotBlank() && preset.isNotBlank()) { "O envio de novas fotos ainda não está configurado." }
        val arquivo = withContext(Dispatchers.IO) {
            val resolver = contexto.contentResolver
            val tipo = resolver.getType(uri)
            val extensao = when (tipo) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> error("Selecione uma foto JPG, PNG ou WebP.")
            }
            val bytes = resolver.openInputStream(uri)?.use { entrada ->
                val saida = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var tamanho = entrada.read(buffer)
                while (tamanho >= 0) {
                    check(saida.size() + tamanho <= LimiteFoto) { "Cada foto pode ter até 5 MB." }
                    saida.write(buffer, 0, tamanho)
                    tamanho = entrada.read(buffer)
                }
                check(saida.size() > 0) { "Selecione uma foto que não esteja vazia." }
                saida.toByteArray()
            } ?: error("Não foi possível abrir a foto selecionada.")
            MultipartBody.Part.createFormData("file", "anuncio.$extensao", bytes.toRequestBody(tipo.toMediaType()))
        }
        val resposta = api.enviar(cloud, arquivo, preset.toRequestBody("text/plain".toMediaType()))
        val url = resposta.body()?.url
        check(resposta.isSuccessful && url != null && url.startsWith("https://res.cloudinary.com/$cloud/image/upload/")) {
            "Não foi possível enviar a foto. Confira sua conexão e tente novamente."
        }
        return url
    }
}
