package com.example.ecowayer.ui.screens.entrega

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
internal fun CameraQrEntrega(modifier: Modifier, aoLer: () -> Unit, aoFalhar: (String) -> Unit) {
    val contexto = LocalContext.current
    val dono = LocalLifecycleOwner.current
    val previewView = remember(contexto) {
        PreviewView(contexto).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val leituraAtual = rememberUpdatedState(aoLer)
    val falhaAtual = rememberUpdatedState(aoFalhar)

    AndroidView(factory = { previewView }, modifier = modifier)

    DisposableEffect(contexto, dono, previewView) {
        val executor = ContextCompat.getMainExecutor(contexto)
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
        val preview = Preview.Builder().build()
        val analise = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        val futuro = ProcessCameraProvider.getInstance(contexto)
        var provedor: ProcessCameraProvider? = null
        var ativo = true
        var concluido = false

        analise.setAnalyzer(executor) { quadro ->
            if (!ativo || concluido) {
                quadro.close()
            } else {
                lerQuadro(
                    scanner,
                    quadro,
                    aoLer = {
                        if (ativo && !concluido) {
                            concluido = true
                            leituraAtual.value()
                        }
                    },
                    aoFalhar = {
                        if (ativo && !concluido) {
                            concluido = true
                            falhaAtual.value("Não foi possível ler o QR Code. Tente novamente.")
                        }
                    }
                )
            }
        }

        futuro.addListener({
            if (ativo) {
                try {
                    val camera = futuro.get()
                    provedor = camera
                    val seletor = if (camera.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    preview.surfaceProvider = previewView.surfaceProvider
                    camera.bindToLifecycle(dono, seletor, preview, analise)
                } catch (_: Exception) {
                    falhaAtual.value("Câmera indisponível neste dispositivo.")
                }
            }
        }, executor)

        onDispose {
            ativo = false
            analise.clearAnalyzer()
            provedor?.unbind(preview, analise)
            scanner.close()
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun lerQuadro(
    scanner: BarcodeScanner,
    quadro: ImageProxy,
    aoLer: () -> Unit,
    aoFalhar: () -> Unit
) {
    val imagem = quadro.image
    if (imagem == null) {
        quadro.close()
        return
    }

    try {
        val entrada = InputImage.fromMediaImage(imagem, quadro.imageInfo.rotationDegrees)
        scanner.process(entrada)
            .addOnSuccessListener { codigos ->
                if (codigos.any { !it.rawValue.isNullOrBlank() }) aoLer()
            }
            .addOnFailureListener { aoFalhar() }
            .addOnCompleteListener { quadro.close() }
    } catch (_: Exception) {
        quadro.close()
        aoFalhar()
    }
}
