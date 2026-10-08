package com.bharatfile.app.data

import android.content.Context
import android.content.SharedPreferences
import com.bharatfile.app.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class HistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val jsonStr = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val list = mutableListOf<HistoryItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    HistoryItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        fileName = obj.optString("fileName", "Unknown File"),
                        originalSizeBytes = obj.optLong("originalSizeBytes", 0L),
                        outputSizeBytes = obj.optLong("outputSizeBytes", 0L),
                        operationType = obj.optString("operationType", "File Operation"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        filePath = obj.optString("filePath", null.toString()).let { if (it == "null") null else it },
                        mimeType = obj.optString("mimeType", "*/*")
                    )
                )
            }
        } catch (_: Exception) {}
        _history.value = list.sortedByDescending { it.timestamp }
    }

    private fun saveHistory(list: List<HistoryItem>) {
        val jsonArray = JSONArray()
        list.take(100).forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("fileName", item.fileName)
                put("originalSizeBytes", item.originalSizeBytes)
                put("outputSizeBytes", item.outputSizeBytes)
                put("operationType", item.operationType)
                put("timestamp", item.timestamp)
                put("filePath", item.filePath)
                put("mimeType", item.mimeType)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
        _history.value = list
    }

    fun addItem(item: HistoryItem) {
        val updated = mutableListOf<HistoryItem>().apply {
            add(item)
            addAll(_history.value.filter { it.id != item.id })
        }
        saveHistory(updated)
    }

    fun deleteItem(id: String) {
        val updated = _history.value.filter { it.id != id }
        saveHistory(updated)
    }

    fun clearHistory() {
        saveHistory(emptyList())
    }

    companion object {
        private const val PREFS_NAME = "bharatfile_history_prefs"
        private const val KEY_HISTORY = "history_items_json"

        @Volatile
        private var instance: HistoryManager? = null

        fun getInstance(context: Context): HistoryManager {
            return instance ?: synchronized(this) {
                instance ?: HistoryManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
