package com.bharatfile.app.engine

import android.content.Context
import java.io.File

object FileCleaner {

    fun getCacheSizeBytes(context: Context): Long {
        return getFolderSize(context.cacheDir)
    }

    fun clearCache(context: Context): Long {
        val initialSize = getFolderSize(context.cacheDir)
        deleteFolderContents(context.cacheDir)
        return initialSize
    }

    private fun getFolderSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            total += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return total
    }

    private fun deleteFolderContents(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return false
        var success = true
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteFolderContents(file)
                file.delete()
            } else {
                if (!file.delete()) success = false
            }
        }
        return success
    }
}
