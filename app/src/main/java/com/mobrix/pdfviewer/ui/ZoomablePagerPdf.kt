package com.mobrix.pdfviewer.ui

import android.util.Size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.toRectF
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.mobrix.pdfviewer.domain.PdfFetcherData
import io.legere.pdfiumandroid.PdfDocument
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomablePagerPdf(
    viewModel: MainViewModel,
    imageLoader: ImageLoader,
    scrollEnabled: MutableState<Boolean>,
    maxScale: Float = 5f,
    pageIndex: Int,
    size: Size,
    onLinkClick: (PdfDocument.Link) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val zoomState = rememberZoomState(
        maxScale = maxScale,
    )

    LaunchedEffect(zoomState.scale) {
        scrollEnabled.value = zoomState.scale == 1f
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(
                PdfFetcherData(
                    page = pageIndex,
                    width = size.width,
                    height = size.height,
                    density = density.density.roundToInt(),
                    viewModel = viewModel
                )
            )
            .memoryCacheKey("page_$pageIndex")
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = "Page $pageIndex",
        contentScale = ContentScale.Fit,
        imageLoader = imageLoader,
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onDoubleClick = {},
                onClick = {}
            )
            .zoomable(
                zoomState = zoomState,
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { it ->
                        val x = it.x
                        val y = it.y

                        val pdfPage = viewModel.state.value.pdfPages.getOrNull(pageIndex)
                            ?: return@detectTapGestures

                        val links = pdfPage
                            .getPageLinks()
                            .map { link ->
                                PdfDocument.Link(
                                    uri = link.uri,
                                    destPageIdx = link.destPageIdx,
                                    bounds = pdfPage
                                        .mapRectToDevice(
                                            startX = 0,
                                            startY = 0,
                                            sizeX = size.width,
                                            sizeY = size.height,
                                            rotate = 0,
                                            coords = link.bounds
                                        )
                                        .toRectF(),
                                )
                            }

                        val clickedLink = links.firstOrNull { link ->
                            val rect = link.bounds
                            rect.left < x && x < rect.right
                                    && rect.top < y && y < rect.bottom
                        }

                        clickedLink?.let {
                            onLinkClick(it)
                        }
                    }
                )
            }
    )
}
