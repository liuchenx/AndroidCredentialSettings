package me.liuyichen.android.credential

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_certificate_settings.*
import java.io.File


class CertificateSettingsActivity : AppCompatActivity(), KeyChainAliasCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificate_settings)

        Thread {
            if (CertificateHelper.isKeyChainAccessible(this@CertificateSettingsActivity, getAlias())) {
                disableKeyChainButton()
                printInfo()
            } else {
                log(TAG, "Key Chain is not accessible")
            }
        }.start()

        button.setOnClickListener {

            Thread {
                var file = File("${getExternalFilesDir("cert")}/")
                file.mkdirs()

                val pfxPath = "${getExternalFilesDir("cert")}/ROOTCA.pfx"
                val crtPath = "${getExternalFilesDir("cert")}/ROOTCA.crt"

                CertificateHelper.createRootCert(pfxPath, crtPath)

                installPkcsCredential(this, File(crtPath).readBytes())
            }.start()
        }
    }

    private fun printInfo() {
        val alias = getAlias()
        val certs = CertificateHelper.getCertificateChain(this, alias)
        val privateKey = CertificateHelper.getPrivateKey(this, alias)
        val sb = StringBuffer()
        for (cert in certs!!) {
            sb.append(cert.getIssuerDN())
            sb.append("\n")
        }
        runOnUiThread {
            textView.text = sb.toString()
        }
    }

    override fun alias(alias: String?) {
        if (alias != null) {
            setAlias(alias)
            disableKeyChainButton()
            printInfo()
        } else {
            log(TAG, "User hit Disallow")
        }
    }

    private fun setAlias(alias: String) {
        val pref = getSharedPreferences("KEYCHAIN_PREF",
                Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("KEYCHAIN_PREF_ALIAS", alias)
        editor.commit()
    }

    private fun getAlias(): String {
        val pref = getSharedPreferences("KEYCHAIN_PREF",
                Context.MODE_PRIVATE)
        return pref.getString("KEYCHAIN_PREF_ALIAS", DEFAULT_ALIAS)
    }

    private fun chooseCert() {
        KeyChain.choosePrivateKeyAlias(this, this, // Callback
                arrayOf(), // Any key types.
                null, // Any issuers.
                "localhost", // Any host
                -1, // Any port
                DEFAULT_ALIAS)
    }

    private fun disableKeyChainButton() {
        runOnUiThread {
            button.text = "installed"
            button.isEnabled = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        log(TAG, "requestCode: $requestCode resultCode: $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INSTALL_KEYCHAIN_CODE) {
                Toast.makeText(this, "安装成功", Toast.LENGTH_SHORT).show()
                chooseCert()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    companion object {
        const val TAG = "CertificateSettingsActivity"

        private const val INSTALL_KEYCHAIN_CODE = 1
        private const val DEFAULT_ALIAS = "liuyichen Key Chain"

        fun launch(activity: Activity) {
            activity.startActivity(Intent(activity, CertificateSettingsActivity::class.java))
        }



        fun installPkcsCredential(context : Activity, credentialByte : ByteArray, requestCode : Int = INSTALL_KEYCHAIN_CODE) {
            val intent = KeyChain.createInstallIntent()
            intent.putExtra(KeyChain.EXTRA_PKCS12, credentialByte)
            intent.putExtra(KeyChain.EXTRA_NAME, DEFAULT_ALIAS);
            context.startActivityForResult(intent, requestCode);
        }

        @Deprecated("no used")
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

        @Deprecated("no used")
        fun installCertCredential(context : Activity, credentialByte : ByteArray, requestCode : Int) {
            val intent = KeyChain.createInstallIntent()
            intent.putExtra(KeyChain.EXTRA_CERTIFICATE, credentialByte)
            intent.putExtra(KeyChain.EXTRA_NAME, DEFAULT_ALIAS);
            context.startActivityForResult(intent, requestCode);
        }
    }
}
