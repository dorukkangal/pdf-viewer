package com.mobrix.pdfviewer.ui

import android.util.Size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import coil.ImageLoader
import io.legere.pdfiumandroid.PdfDocument

@Composable
fun PdfPageComponent(
    viewModel: MainViewModel,
    imageLoader: ImageLoader,
    scrollEnabled: MutableState<Boolean>,
    pageIndex: Int,
    size: Size,
    onLinkClick: (PdfDocument.Link) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ZoomablePagerPdf(
            viewModel = viewModel,
            imageLoader = imageLoader,
            scrollEnabled = scrollEnabled,
            pageIndex = pageIndex,
            size = size,
            onLinkClick = onLinkClick
        )
    }
}
