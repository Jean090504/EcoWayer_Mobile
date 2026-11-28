package com.example.ecowayer.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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
            val tipoOriginal = resolver.getType(uri)?.lowercase()
            val bytesOriginais = resolver.openInputStream(uri)?.use { entrada ->
                val saida = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var tamanho = entrada.read(buffer)
                while (tamanho > 0 && saida.size() <= LimiteFoto) {
                    saida.write(buffer, 0, tamanho)
                    tamanho = entrada.read(buffer)
                }
                check(saida.size() > 0) { "Selecione uma foto que não esteja vazia." }
                saida.toByteArray()
            } ?: error("Não foi possível abrir a foto selecionada.")
            val tiposDiretos = mapOf("image/jpeg" to "jpg", "image/png" to "png", "image/webp" to "webp")
            val tipo: String
            val extensao: String
            val bytes: ByteArray
            if (bytesOriginais.size <= LimiteFoto && tiposDiretos.containsKey(tipoOriginal)) {
                tipo = tipoOriginal!!
                extensao = tiposDiretos.getValue(tipo)
                bytes = bytesOriginais
            } else {
                // Fotos HEIC ou maiores que 5 MB são convertidas antes do upload.
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                        val maiorLado = maxOf(info.size.width, info.size.height)
                        decoder.setTargetSampleSize(maxOf(1, (maiorLado + 2047) / 2048))
                    }
                } else {
                    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
                    val maiorLado = maxOf(limites.outWidth, limites.outHeight)
                    val opcoes = BitmapFactory.Options().apply {
                        inSampleSize = maxOf(1, (maiorLado + 2047) / 2048)
                    }
                    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes) }
                } ?: error("Não foi possível processar essa foto. Escolha outra imagem.")
                val imagem = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                Canvas(imagem).apply { drawColor(Color.WHITE); drawBitmap(bitmap, 0f, 0f, null) }
                val saida = ByteArrayOutputStream()
                for (qualidade in listOf(85, 70, 55)) {
                    saida.reset()
                    imagem.compress(Bitmap.CompressFormat.JPEG, qualidade, saida)
                    if (saida.size() <= LimiteFoto) break
                }
                check(saida.size() <= LimiteFoto) { "Não foi possível reduzir a foto para 5 MB." }
                tipo = "image/jpeg"
                extensao = "jpg"
                bytes = saida.toByteArray()
            }
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
