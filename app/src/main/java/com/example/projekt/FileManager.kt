package com.example.projekt

import android.content.Context
import java.io.File

object FileManager {
    private const val FILE_NAME = "secure_data"

    fun saveEncrypted(context: Context, encrypted: ByteArray) {
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(encrypted)
        }
    }

    fun loadEncrypted(context: Context): ByteArray? {
        return try {
            context.openFileInput(FILE_NAME).use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }

    fun clearData(context: Context) {
        context.deleteFile(FILE_NAME)
    }

    fun exportData(context: Context): File {
        return File(context.filesDir, "export.dat")
    }
}