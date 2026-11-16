package com.brahma.assistant.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.brahma.assistant.MainActivity

class BrahmaAssistActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch main dashboard
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(mainIntent)
        finish()
    }
}
