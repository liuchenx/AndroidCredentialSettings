package me.liuyichen.android.credential.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.liuyichen.android.credential.CertificateSettingsActivity

class MainActivity : AppCompatActivity() {

    private val TEST_SSL_URL = "https://localhost:8080"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {

            CertificateSettingsActivity.launch(this@MainActivity)
        }

        test.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri
                    .parse(TEST_SSL_URL))
            startActivity(i)
        }

        start.setOnClickListener {

            if (start.getText() == "start") {
                start.text = "stop"
                startServer()
            } else {
                start.text = "start"
                stopServer()
            }
        }
    }

    private fun startServer() {
        val secureWebServerIntent = Intent(this,
                SecureWebServerService::class.java)
        startService(secureWebServerIntent)
    }

    /**
     * This method stops the background service of the simple SSL web server
     */
    private fun stopServer() {
        val secureWebServerIntent = Intent(this,
                SecureWebServerService::class.java)
        stopService(secureWebServerIntent)
    }
}
