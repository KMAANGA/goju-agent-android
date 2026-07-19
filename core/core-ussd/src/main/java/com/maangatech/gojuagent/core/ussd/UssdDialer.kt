package com.maangatech.gojuagent.core.ussd

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kicks off the actual USSD session by dialing exactly like a teller would by hand — this
 * is the "no proprietary carrier API" approach the reference tools use: any code that works
 * when hand-dialed works here, with no per-carrier integration needed.
 */
@Singleton
class UssdDialer @Inject constructor(@ApplicationContext private val context: Context) {

    fun dial(dialCode: String) {
        // '#' must be percent-encoded or Android truncates the tel: URI at the first '#'.
        val encoded = URLEncoder.encode(dialCode, "UTF-8").replace("+", "%20")
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encoded")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ContextCompat.startActivity(context, intent, null)
    }
}
