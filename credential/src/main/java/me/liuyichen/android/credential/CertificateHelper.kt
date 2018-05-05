package me.liuyichen.android.credential

import android.content.Context
import android.security.KeyChain
import android.security.KeyChainException
import sun.security.x509.CertAndKeyGen
import sun.security.x509.X500Name
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

/**
 * CertificateHepler
 *
 * @author Liu Yichen
 * @date 2018/5/5
 */
object CertificateHelper {

    public const val TYPE_PKCS12 = "PKCS12"

    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class, CertificateException::class, IOException::class)
    private fun createKeyStore(alias: String, key: Key, password: CharArray,
                               chain: Array<Certificate>, filePath: String) {

        val keyStore = KeyStore.getInstance(TYPE_PKCS12)
        keyStore.load(null, password)
        keyStore.setKeyEntry(alias, key, password, chain)
        FileOutputStream(filePath).use {
            keyStore.store(it, password)
        }
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidKeyException::class, IOException::class, CertificateException::class, SignatureException::class, KeyStoreException::class)
    private fun createRootCert(pfxPath: String, crtPath: String,
                       x500Name: X500Name, password : String) {

        val secureRandom = SecureRandom.getInstance("SHA1PRNG")
        val rootCertAndKeyGen = CertAndKeyGen("RSA",
                "MD5WithRSA", null)

        rootCertAndKeyGen.setRandom(secureRandom)

        rootCertAndKeyGen.generate(1024)

        val rootCertificate = rootCertAndKeyGen.getSelfCertificate(
                x500Name, 3650 * 24L * 60L * 60L)

        val x509Certificates = arrayOf<Certificate>(rootCertificate)

        createKeyStore("RootCA", rootCertAndKeyGen.privateKey, password
                .toCharArray(), x509Certificates, pfxPath)

        File(crtPath).writeBytes(rootCertificate.encoded)
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidKeyException::class, IOException::class, CertificateException::class, SignatureException::class, KeyStoreException::class)
    fun createRootCert(issuePfxPath: String, issueCrtPath: String) {
        createRootCert(issuePfxPath, issueCrtPath, X500Name(
                "CN=RootCA,OU=hackwp,O=wp,L=BJ,S=BJ,C=CN"), "123456")
    }

    fun isKeyChainAccessible(context: Context, alias: String): Boolean {
        return getCertificateChain(context, alias) != null && getPrivateKey(context, alias) != null
    }

    fun getPrivateKey(context: Context, alias: String): PrivateKey? {
        try {
            return KeyChain.getPrivateKey(context, alias)
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return null
    }

    fun getCertificateChain(context: Context, alias: String): Array<X509Certificate>? {
        try {
            return KeyChain.getCertificateChain(context, alias)
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return null
    }
}