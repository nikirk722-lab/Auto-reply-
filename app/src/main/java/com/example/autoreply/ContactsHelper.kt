package com.example.autoreply

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object ContactsHelper {
    /** True when the number is saved in the user's contacts. Needs READ_CONTACTS. */
    fun isSavedContact(context: Context, number: String): Boolean {
        if (number.isBlank()) return false
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            context.contentResolver.query(
                uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null
            )?.use { it.moveToFirst() } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
