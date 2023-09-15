package com.mobrix.pdfviewer.domain

import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import com.mobrix.pdfviewer.ui.MainViewModel
import timber.log.Timber

class PdfFetcher(
    private val data: PdfFetcherData,
    private val options: Options
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        Timber.d("fetch: ${data.page}")
        val bitmap = data.viewModel.getPage(data.page, data.width, data.height, data.density)
        if (bitmap == null) {
            Timber.d("fetch: bitmap is null")
            return null
        }
        return DrawableResult(
            drawable = BitmapDrawable(options.context.resources, bitmap),
            isSampled = false,
            dataSource = DataSource.MEMORY
        )
    }

    class Factory : Fetcher.Factory<PdfFetcherData> {
        override fun create(
            data: PdfFetcherData,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return PdfFetcher(data, options)
        }
    }
}

data class PdfFetcherData(
    val page: Int,
    val width: Int,
    val height: Int,
    val density: Int,
    val viewModel: MainViewModel
)
