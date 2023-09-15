package com.mobrix.pdfviewer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil.ComponentRegistry
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.mobrix.pdfviewer.R
import com.mobrix.pdfviewer.domain.PdfFetcher
import com.mobrix.pdfviewer.theme.PdfViewerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val openFileContract =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.accept(MainViewModel.UiAction.LoadDoc(uri))
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageLoader = ImageLoader.Builder(this)
            .components(fun ComponentRegistry.Builder.() {
                add(PdfFetcher.Factory())
            })
            .allowRgb565(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.1)
                    .build()
            }
            .build()

        setContent {
            PdfViewerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(text = "Pdf Viewer")
                                },
                                actions = {
                                    IconButton(onClick = {
                                        openFileContract.launch(arrayOf("application/pdf"))
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.outline_file_open_24),
                                            contentDescription = "Open"
                                        )
                                    }
                                }
                            )
                        }
                    ) { contentPadding ->
                        Box(modifier = Modifier.padding(contentPadding)) {
                            MainContent(viewModel, imageLoader)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel, imageLoader: ImageLoader) {
    val state = viewModel.state.collectAsState()
    when (state.value.loadState) {
        MainViewModel.LoadStatus.Loading -> MaxSizeCenterBox { CircularProgressIndicator() }
        MainViewModel.LoadStatus.Success -> PdfScreen(viewModel, imageLoader)
        MainViewModel.LoadStatus.Error -> Message("Error")
        MainViewModel.LoadStatus.Init -> Message("Load PDF")
    }
}

@Composable
private fun MaxSizeCenterBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun Message(text: String = "Error") {
    MaxSizeCenterBox {
        Text(text = text)
    }
}
