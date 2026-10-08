package com.bharatfile.app.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fullName: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val avatarUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val filesProcessedCount: Int = 0,
    val totalBytesSaved: Long = 0L
) {
    val formattedMemberSince: String
        get() {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            return "Member since ${sdf.format(Date(createdAt))}"
        }

    val formattedTotalSaved: String
        get() = ProcessedFileResult.formatFileSize(totalBytesSaved)

    val initials: String
        get() {
            val parts = fullName.trim().split("\\s+".toRegex())
            return when {
                parts.isEmpty() || parts[0].isEmpty() -> "BF"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first()}${parts[1].first()}".uppercase()
            }
        }
}
