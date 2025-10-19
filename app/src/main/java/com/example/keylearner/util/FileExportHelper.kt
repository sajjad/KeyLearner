package com.example.keylearner.util

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Helper class for file import/export operations using Storage Access Framework
 */
object FileExportHelper {

    /**
     * Write CSV content to a file URI
     *
     * @param context Android context
     * @param uri The file URI to write to (from Storage Access Framework)
     * @param csvContent The CSV content to write
     * @return Result indicating success or error
     */
    fun writeCSVToUri(context: Context, uri: Uri, csvContent: String): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(csvContent)
                }
            } ?: return Result.failure(Exception("Failed to open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to write file: ${e.message}"))
        }
    }

    /**
     * Read CSV content from a file URI
     *
     * @param context Android context
     * @param uri The file URI to read from (from Storage Access Framework)
     * @return Result containing the CSV content or error
     */
    fun readCSVFromUri(context: Context, uri: Uri): Result<String> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val content = reader.readText()
                    Result.success(content)
                }
            } ?: Result.failure(Exception("Failed to open input stream"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read file: ${e.message}"))
        }
    }
}
