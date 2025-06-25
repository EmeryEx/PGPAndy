package engineer.warfare.pgpandy

import android.content.Context
import org.bouncycastle.openpgp.PGPObjectFactory
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator

/** Service for importing only public keys. Throws if a private key is found. */
class PublicKeyImportService(private val context: Context) {
    /**
     * Parses ASCII armored data and inserts any public keys found. If a private
     * key is encountered an exception is thrown.
     * @return number of public keys imported
     */
    fun importPublicKeys(armored: String): Int {
        val decoder = PGPUtil.getDecoderStream(armored.byteInputStream())
        val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
        var count = 0
        var found = false
        while (true) {
            val obj = factory.nextObject() ?: break
            when (obj) {
                is PGPSecretKeyRing -> throw IllegalArgumentException("private")
                is PGPPublicKeyRing -> {
                    found = true
                    val out = java.io.ByteArrayOutputStream()
                    org.bouncycastle.bcpg.ArmoredOutputStream(out).use {
                        obj.encode(it)
                    }
                    val ringArmored = out.toString(java.nio.charset.StandardCharsets.UTF_8.name())

                    val pubKey = obj.publicKey
                    val fingerprint = pubKey.fingerprint.joinToString("") { "%02X".format(it) }
                    val keyId = java.lang.Long.toHexString(pubKey.keyID).uppercase()
                    val userId = pubKey.userIDs.asSequence().firstOrNull()
                    val algorithm = algorithmName(pubKey.algorithm)
                    val bitLength = pubKey.bitStrength
                    val createdAt = pubKey.creationTime.time / 1000
                    val expiresAt = if (pubKey.validSeconds > 0) createdAt + pubKey.validSeconds else null

                    val info = PgpKeyInfo(
                        userId = userId,
                        fingerprint = fingerprint,
                        keyId = keyId,
                        isPrivate = false,
                        armoredKey = ringArmored,
                        algorithm = algorithm,
                        bitLength = bitLength,
                        comment = null,
                        createdAt = createdAt,
                        expiresAt = expiresAt
                    )
                    DatabaseHelper(context).insertKey(info)
                    count++
                }
            }
        }
        if (!found) throw IllegalArgumentException("none")
        return count
    }

    private fun algorithmName(tag: Int): String {
        return when (tag) {
            1, 2, 3 -> "RSA"
            17 -> "DSA"
            19 -> "ECDSA"
            22 -> "EdDSA"
            else -> "Unknown"
        }
    }
}

