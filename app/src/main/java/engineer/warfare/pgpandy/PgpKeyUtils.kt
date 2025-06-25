package engineer.warfare.pgpandy

import org.bouncycastle.bcpg.ArmoredOutputStream
import org.bouncycastle.openpgp.*
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/** Utility helpers for working with armored PGP keys. */
object PgpKeyUtils {
    /**
     * Extracts the ASCII armored public key portion from the provided armored key.
     * The input can be either a public or private key block. Returns null if
     * parsing fails.
     */
    fun extractPublicKey(armored: String): String? {
        val decoder = PGPUtil.getDecoderStream(armored.byteInputStream())
        val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
        var keyRing: Any? = null
        while (true) {
            val obj = factory.nextObject() ?: break
            if (obj is PGPSecretKeyRing || obj is PGPPublicKeyRing) {
                keyRing = obj
                break
            }
        }
        if (keyRing == null) return null
        val publicRing = when (keyRing) {
            is PGPSecretKeyRing -> {
                val keys = mutableListOf<PGPPublicKey>()
                val it = keyRing.publicKeys
                while (it.hasNext()) {
                    keys.add(it.next() as PGPPublicKey)
                }
                PGPPublicKeyRing(keys)
            }
            is PGPPublicKeyRing -> keyRing
            else -> return null
        }
        val out = ByteArrayOutputStream()
        ArmoredOutputStream(out).use { publicRing.encode(it) }
        return out.toString(StandardCharsets.UTF_8.name())
    }
}
