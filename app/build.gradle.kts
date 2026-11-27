plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

import java.util.Properties

abstract class ValidateReleaseApiBaseUrl : org.gradle.api.DefaultTask() {
    @get:org.gradle.api.tasks.Input
    abstract val apiBaseUrl: org.gradle.api.provider.Property<String>

    @org.gradle.api.tasks.TaskAction
    fun validate() {
        val url = apiBaseUrl.get()
        check(url.startsWith("https://")) {
            "Builds de release exigem -PAPI_BASE_URL com uma URL HTTPS de produção."
        }
    }
}

val localProperties = Properties()
rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
    localProperties.load(it)
}



android {
    namespace = "com.example.ecowayer"
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileSdk {
        version = release(36)
    }

    val uploadKeyStore = providers.environmentVariable("ECOWAYER_UPLOAD_KEYSTORE").orNull
    if (!uploadKeyStore.isNullOrBlank()) {
        signingConfigs {
            create("production") {
                storeFile = file(uploadKeyStore)
                storePassword = providers.environmentVariable("ECOWAYER_UPLOAD_STORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("ECOWAYER_UPLOAD_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("ECOWAYER_UPLOAD_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        debug {
            val devAutoLogin = providers.gradleProperty("DEV_AUTO_LOGIN")
                .orElse(localProperties.getProperty("DEV_AUTO_LOGIN", "false"))
                .get()
            val devAutoLoginEmail = providers.gradleProperty("DEV_AUTO_LOGIN_EMAIL")
                .orElse(localProperties.getProperty("DEV_AUTO_LOGIN_EMAIL", ""))
                .get()
            val devAutoLoginPassword = providers.gradleProperty("DEV_AUTO_LOGIN_PASSWORD")
                .orElse(localProperties.getProperty("DEV_AUTO_LOGIN_PASSWORD", ""))
                .get()
            buildConfigField("Boolean", "DEV_AUTO_LOGIN", devAutoLogin.toBoolean().toString())
            buildConfigField("String", "DEV_AUTO_LOGIN_EMAIL", "\"${devAutoLoginEmail.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
            buildConfigField("String", "DEV_AUTO_LOGIN_PASSWORD", "\"${devAutoLoginPassword.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        }
        release {
            if (!uploadKeyStore.isNullOrBlank()) signingConfig = signingConfigs.getByName("production")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        applicationId = "com.example.ecowayer"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val apiBaseUrl = providers.gradleProperty("API_BASE_URL")
            .orElse(localProperties.getProperty("API_BASE_URL", "http://10.0.2.2:3000/meraki/ecowayer/"))
            .get()
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("Boolean", "DEV_AUTO_LOGIN", "false")
        buildConfigField("String", "DEV_AUTO_LOGIN_EMAIL", "\"\"")
        buildConfigField("String", "DEV_AUTO_LOGIN_PASSWORD", "\"\"")
        // Estes valores públicos identificam o upload sem assinatura usado também pelo frontend.
        listOf("CLOUDINARY_CLOUD_NAME", "CLOUDINARY_UPLOAD_PRESET").forEach { nome ->
            val valor = providers.gradleProperty(nome).orElse(localProperties.getProperty(nome, "")).get()
            require(valor.isEmpty() || valor.matches(Regex("[A-Za-z0-9_-]+"))) { "Confira a configuração de $nome." }
            buildConfigField("String", nome, "\"$valor\"")
        }
        val googleMapsApiKey = providers.gradleProperty("GOOGLE_MAPS_API_KEY")
            .orElse(localProperties.getProperty("GOOGLE_MAPS_API_KEY", ""))
            .get()
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$googleMapsApiKey\"")
    }
}

val validateReleaseApiBaseUrl = tasks.register<ValidateReleaseApiBaseUrl>("validateReleaseApiBaseUrl") {
    apiBaseUrl.set(providers.gradleProperty("API_BASE_URL").orElse(localProperties.getProperty("API_BASE_URL", "")))
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(validateReleaseApiBaseUrl)
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // Google Maps
    implementation("com.google.maps.android:maps-compose:6.4.1")

    // Imagens remotas (Gift Cards, anúncios e perfis)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // CameraX + leitura de QR/código de barras
    val cameraxVersion = "1.6.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.zxing:core:3.5.3")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("com.google.android.gms:play-services-location:21.3.0")
}
