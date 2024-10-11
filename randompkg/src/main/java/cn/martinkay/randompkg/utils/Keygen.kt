package cn.martinkay.randompkg.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Base64OutputStream
import cn.martinkay.randompkg.signing.CryptoUtils.readCertificate
import cn.martinkay.randompkg.signing.CryptoUtils.readPrivateKey
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private interface CertKeyProvider {
    val cert: X509Certificate
    val key: PrivateKey
}

@Suppress("DEPRECATION")
class Keygen(context: Context) : CertKeyProvider {

    companion object {
        private const val ALIAS = "magisk"
        private val PASSWORD get() = "magisk".toCharArray()
        private const val TESTKEY_CERT = "61ed377e85d386a8dfee6b864bd85b0bfaa5af81"
        private const val DNAME = "C=US,ST=California,L=Mountain View,O=Google Inc.,OU=Android,CN=Android"
        private const val BASE64_FLAG = Base64.NO_PADDING or Base64.NO_WRAP
    }

    private val start = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }
    private val end = (start.clone() as Calendar).apply { add(Calendar.YEAR, 30) }

    override val cert get() = provider.cert
    override val key get() = provider.key

    private val provider: CertKeyProvider

    inner class KeyStoreProvider :
        CertKeyProvider {
        private val ks by lazy { init() }
        override val cert by lazy { ks.getCertificate(ALIAS) as X509Certificate }
        override val key by lazy { ks.getKey(
            ALIAS,
            PASSWORD
        ) as PrivateKey }
    }


    init {
        val pm = context.packageManager
        val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        val sig = info.signatures[0]
        val digest = MessageDigest.getInstance("SHA1")
        val chksum = digest.digest(sig.toByteArray())

        val sb = StringBuilder()
        for (b in chksum) {
            sb.append("%02x".format(0xFF and b.toInt()))
        }
        provider = KeyStoreProvider()
    }

    private fun init(): KeyStore {
        val ks = KeyStore.getInstance("PKCS12")
        ks.load(null)

        // Keys already exist
        if (ks.containsAlias(ALIAS))
            return ks

        // Generate new private key and certificate
        val kp = KeyPairGenerator.getInstance("RSA").apply { initialize(4096) }.genKeyPair()
        val dname = X500Name(DNAME)
        val builder = JcaX509v3CertificateBuilder(dname, BigInteger(160, Random()),
            start.time, end.time, dname, kp.public)
        val signer = JcaContentSignerBuilder("SHA1WithRSA").build(kp.private)
        val cert = JcaX509CertificateConverter().getCertificate(builder.build(signer))

        // Store them into keystore
        ks.setKeyEntry(
            ALIAS, kp.private,
            PASSWORD, arrayOf(cert))
        val bytes = ByteArrayOutputStream()
        GZIPOutputStream(Base64OutputStream(bytes,
            BASE64_FLAG
        )).use {
            ks.store(it,
                PASSWORD
            )
        }
//        Config.keyStoreRaw = bytes.toString("UTF-8")

        return ks
    }
}
