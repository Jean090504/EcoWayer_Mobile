package com.example.ecowayer.ui.screens.ecopontos

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecowayer.ui.components.FiltrosPainel
import com.example.ecowayer.ui.components.FiltrosState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosBottomSheet(
    filtrosAtuais: FiltrosState,
    onFechar: () -> Unit,
    onAplicar: (FiltrosState) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var rascunho by remember { mutableStateOf(filtrosAtuais) }

    fun fecharAnimado(aoTerminar: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            aoTerminar()
            onFechar()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        FiltrosPainel(
            filtros = rascunho,
            onFiltrosChange = { rascunho = it },
            onFechar = { fecharAnimado() },
            onAplicar = { fecharAnimado { onAplicar(rascunho) } }
        )
    }
}