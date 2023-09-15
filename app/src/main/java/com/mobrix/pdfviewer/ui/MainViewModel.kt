package com.mobrix.pdfviewer.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.legere.pdfiumandroid.suspend.PdfDocumentKt
import io.legere.pdfiumandroid.suspend.PdfPageKt
import io.legere.pdfiumandroid.suspend.PdfiumCoreKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    val state: StateFlow<UiState>
    val accept: (UiAction) -> Unit

    private val pdfiumCore = PdfiumCoreKt(Dispatchers.Default)
    private var pdfDocument: PdfDocumentKt? = null

    init {
        val actionStateFlow = MutableStateFlow<UiAction>(UiAction.Init())

        state = actionStateFlow.flatMapLatest { action ->
            flow {
                when (action) {
                    is UiAction.Init -> emit(UiState(loadState = LoadStatus.Init))
                    is UiAction.LoadDoc -> {
                        emit(UiState(loadState = LoadStatus.Loading))
                        val fd = application.contentResolver.openFileDescriptor(action.uri, "r")
                        if (fd != null) {
                            pdfDocument = pdfiumCore.newDocument(fd)
                            val pageCount = pdfDocument?.getPageCount() ?: 0
                            Timber.d("pageCount: $pageCount")

                            val pdfPages = (0 until pageCount).map {
                                pdfDocument?.openPage(it)
                            }

                            emit(
                                UiState(
                                    loadState = LoadStatus.Success,
                                    pageCount = pageCount,
                                    pdfPages = pdfPages.filterNotNull()
                                )
                            )
                        } else {
                            emit(UiState(loadState = LoadStatus.Error))
                        }
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, UiState())

        accept = { action ->
            viewModelScope.launch {
                actionStateFlow.emit(action)
            }
        }
    }

    @Suppress("NestedBlockDepth", "TooGenericExceptionCaught")
    suspend fun getPage(pageNum: Int, width: Int, height: Int, density: Int): Bitmap? {
        Timber.d("getPage: pageNum: $pageNum, width: $width, height: $height, density: $density")
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            pdfDocument?.openPage(pageNum)?.use { page ->
                page.renderPageBitmap(bitmap, 0, 0, width, height, density)
                page.openTextPage().use { textPage ->
                    val charCount = textPage.textPageCountChars()
                    if (charCount > 0) {
                        val text = textPage.textPageGetText(0, charCount)
                        Timber.d("text: $text")
                    }
                }
            }
            Timber.d("finsished getPage $pageNum")
            return bitmap
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    sealed class UiAction {
        data class LoadDoc(val uri: Uri) : UiAction()
        data class Init(@Suppress("unused") val s: String = "Init") : UiAction()
    }

    enum class LoadStatus {
        Init,
        Loading,
        Success,
        Error
    }

    data class UiState(
        val loadState: LoadStatus = LoadStatus.Init,
        val pageCount: Int = 0,
        val pdfPages: List<PdfPageKt> = arrayListOf()
    )
}
