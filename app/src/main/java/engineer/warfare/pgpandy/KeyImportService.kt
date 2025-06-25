package engineer.warfare.pgpandy

import android.content.Context
import org.bouncycastle.openpgp.PGPObjectFactory
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator

/** Service class responsible for importing PGP keys from ASCII armored files. */
class KeyImportService(private val context: Context) {

    /**
     * Parses the provided ASCII armored key data and inserts it into the local
     * database. The method extracts some basic metadata such as fingerprint and
     * user id using BouncyCastle.
     */
    fun importArmoredKey(armored: String): Int {
        val decoder = PGPUtil.getDecoderStream(armored.byteInputStream())
        val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
        var count = 0
        while (true) {
            val obj = factory.nextObject() ?: break
            if (obj !is PGPSecretKeyRing && obj !is PGPPublicKeyRing) continue

            val out = java.io.ByteArrayOutputStream()
            org.bouncycastle.bcpg.ArmoredOutputStream(out).use { (obj as org.bouncycastle.openpgp.PGPKeyRing).encode(it) }
            val ringArmored = out.toString(java.nio.charset.StandardCharsets.UTF_8.name())

            val isPrivate = obj is PGPSecretKeyRing
            val pubKey = when (obj) {
                is PGPSecretKeyRing -> obj.publicKey
                is PGPPublicKeyRing -> obj.publicKey
                else -> continue
            }

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
                isPrivate = isPrivate,
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

/** Simple data holder used when inserting a key into the database. */
data class PgpKeyInfo(
    val userId: String?,
    val fingerprint: String,
    val keyId: String,
    val isPrivate: Boolean,
    val armoredKey: String,
    val algorithm: String?,
    val bitLength: Int?,
    val comment: String?,
    val createdAt: Long?,
    val expiresAt: Long?
)
