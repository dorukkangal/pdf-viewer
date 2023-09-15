package com.mobrix.pdfviewer.ui

import android.content.Intent
import android.net.Uri
import android.util.Size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.ImageLoader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PdfScreen(
    viewModel: MainViewModel,
    imageLoader: ImageLoader
) {
    val context = LocalContext.current

    val state = viewModel.state.collectAsState()

    val pagerState = rememberPagerState {
        state.value.pageCount
    }

    val scrollEnabled = rememberSaveable { mutableStateOf(true) }

    var componentWidth by remember { mutableIntStateOf(0) }
    var componentHeight by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier
            .onGloballyPositioned {
                componentWidth = it.size.width
                componentHeight = it.size.height
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = scrollEnabled.value,
            ) { pageIndex ->
                if (componentWidth <= 0 || componentHeight <= 0) return@HorizontalPager

                PdfPageComponent(
                    viewModel = viewModel,
                    imageLoader = imageLoader,
                    scrollEnabled = scrollEnabled,
                    pageIndex = pageIndex,
                    size = Size(componentWidth, componentHeight),
                    onLinkClick = {
                        val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.uri))
                        ContextCompat.startActivity(context, browseIntent, null)
                    }
                )
            }
        }
    }
}
