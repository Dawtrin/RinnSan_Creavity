package com.rinnsan.creavity.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ═══════════════════════════════════════════════════════════════════
 * CLOUDINARY API SERVICE
 * ═══════════════════════════════════════════════════════════════════
 *
 * Upload images to Cloudinary CDN
 *
 * Setup:
 * 1. Create account at cloudinary.com
 * 2. Get: cloud_name, upload_preset
 * 3. Add to local.properties:
 *    CLOUDINARY_CLOUD_NAME=your_cloud_name
 *    CLOUDINARY_UPLOAD_PRESET=your_preset
 */

@Singleton
class CloudinaryApi @Inject constructor(
    private val context: Context
) {

    // TODO: Replace with your Cloudinary credentials
    // Get from: https://cloudinary.com/console
    private val cloudName = "dsdhckzwo"
    private val uploadPreset = "noteapp_unsigned"


    private val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Upload single image
     */
    suspend fun uploadImage(
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Compress image
            val compressedFile = compressImage(uri)

            // Create multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", compressedFile.name,
                    compressedFile.asRequestBody("image/*".toMediaType())
                )
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("folder", "signal_posts")  // Organize in folder
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            // Execute upload
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("Upload failed: ${response.code}")
            }

            // Parse response
            val responseBody = response.body?.string() ?: throw IOException("Empty response")
            val json = JSONObject(responseBody)
            val imageUrl = json.getString("secure_url")

            // Cleanup temp file
            compressedFile.delete()

            Result.success(imageUrl)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload multiple images
     */
    suspend fun uploadImages(
        uris: List<Uri>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val uploadedUrls = mutableListOf<String>()

            uris.forEachIndexed { index, uri ->
                val result = uploadImage(uri) { progress ->
                    onProgress(index, (progress * 100).toInt())
                }

                if (result.isSuccess) {
                    uploadedUrls.add(result.getOrThrow())
                } else {
                    throw result.exceptionOrNull() ?: Exception("Upload failed")
                }
            }

            Result.success(uploadedUrls)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Compress image before upload
     * Target: Max 1MB, Max 1920px width
     */
    private suspend fun compressImage(uri: Uri): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open image")

        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Resize if too large
        val maxWidth = 1920
        val maxHeight = 1920

        val scale = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height,
            1f
        )

        val scaledBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        // Compress to JPEG
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)

        // Start with 90% quality
        var quality = 90
        var compressed: ByteArray

        do {
            val tempOutputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, tempOutputStream)
            compressed = tempOutputStream.toByteArray()
            quality -= 10
        } while (compressed.size > 1_000_000 && quality > 10)  // Max 1MB

        outputStream.write(compressed)
        outputStream.close()

        scaledBitmap.recycle()
        if (scaledBitmap != bitmap) bitmap.recycle()

        tempFile
    }

    /**
     * Delete image from Cloudinary (optional)
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/{public_id}.jpg
            val publicId = imageUrl.substringAfter("/upload/").substringBeforeLast(".")

            // Note: Deletion requires API signature (server-side)
            // For now, images will remain in Cloudinary
            // Implement server-side deletion for production

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Upload Result
 */
data class UploadProgress(
    val uploadedCount: Int,
    val totalCount: Int,
    val currentProgress: Int
) {
    val percentage: Int
        get() = if (totalCount > 0) {
            ((uploadedCount * 100 + currentProgress) / totalCount)
        } else 0
}