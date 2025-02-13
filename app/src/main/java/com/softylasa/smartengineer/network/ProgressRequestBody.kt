package com.softylasa.smartengineer.network

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val onProgressUpdate: (Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? {
        return contentType.toMediaTypeOrNull()
    }

    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inputStream = file.inputStream()
        var uploaded = 0L

        inputStream.use { input ->
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                uploaded += read
                onProgressUpdate((100 * uploaded / fileLength).toInt())
                sink.write(buffer, 0, read)
            }
        }
    }

    override fun contentLength(): Long {
        return file.length()
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
