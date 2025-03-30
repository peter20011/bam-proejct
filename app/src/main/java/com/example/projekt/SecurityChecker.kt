package com.example.projekt

import android.util.Log
import java.io.File

object SecurityChecker {
    fun isDeviceRooted(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )

        return paths.any { path -> File(path).exists() }
    }

    fun isFridaRunning(): Boolean {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "netstat -an | grep 27042"))
            val input = process.inputStream.bufferedReader().readText()
            return input.contains("27042");
        } catch (e: Exception) {
            return false
        }
    }


    fun isUnsafe(): String? {
        return when {
            isFridaRunning() -> "działa frida-server"
            isDeviceRooted() -> "urządzenie jest zrootowane"
            else -> null
        }
    }
}