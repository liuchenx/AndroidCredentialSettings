package me.liuyichen.android.credential

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.security.KeyChain
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_certificate_settings.*

import java.io.File

class CertificateSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificate_settings)

        button.setOnClickListener {

            Thread {
                var file = File("${getExternalFilesDir("cert")}/")
                file.mkdirs()

                val pfxPath = "${getExternalFilesDir("cert")}/ROOTCA.pfx"
                val crtPath = "${getExternalFilesDir("cert")}/ROOTCA.crt"

                CertificateHelper.createRootCert(pfxPath, crtPath)

                file = File("${getExternalFilesDir("cert")}/ROOTCA.pfx")
                installPkcsCredential(this, file.readBytes(), 1)
            }.start()
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        log(TAG, "requestCode: $requestCode resultCode: $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Toast.makeText(this, "安装成功", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    companion object {
        const val TAG = "CertificateSettingsActivity"

        fun launch(activity: Activity) {
            activity.startActivity(Intent(activity, CertificateSettingsActivity::class.java))
        }

        fun installPkcsCredential(context : Context, credentialFile : File) {
            val intent = Intent(Intent.ACTION_VIEW)
            lateinit var uri : Uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                uri = FileProvider.getUriForFile(context, BuildConfig.CERTIFICATE_FILE_PROVIDER, credentialFile)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(credentialFile);
            }
            intent.setDataAndType(uri, "application/x-pkcs12")
            context.startActivity(intent)
        }

        fun installPkcsCredential(context : Activity, credentialByte : ByteArray, requestCode : Int) {
            val intent = KeyChain.createInstallIntent()
            intent.putExtra(KeyChain.EXTRA_PKCS12, credentialByte)
            context.startActivityForResult(intent, requestCode);
        }
    }
}
