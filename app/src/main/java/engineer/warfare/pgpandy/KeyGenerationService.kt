package engineer.warfare.pgpandy

import android.content.Context
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.util.Date
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.*
import org.bouncycastle.openpgp.operator.jcajce.*
import org.bouncycastle.bcpg.ArmoredOutputStream

class KeyGenerationService(private val context: Context) {
    fun generateAndStore(data: KeyFormData) {
        val identity = buildIdentity(data.name, data.email)
        val pass = if (data.password.isNotEmpty()) data.password.toCharArray() else CharArray(0)
        val armored = generateRsaSecretKey(data.bitLength, identity, pass)
        val info = parseInfo(armored)
        if (info != null) {
            val withComment = info.copy(comment = data.label)
            DatabaseHelper(context).insertKey(withComment)
        }
    }

    private fun buildIdentity(name: String, email: String): String {
        val parts = mutableListOf<String>()
        if (name.isNotBlank()) parts.add(name.trim())
        if (email.isNotBlank()) parts.add("<${email.trim()}>")
        return parts.joinToString(" ")
    }

    private fun generateRsaSecretKey(bitLength: Int, identity: String, passphrase: CharArray): String {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(bitLength, SecureRandom())
        val kp = kpg.generateKeyPair()
        val pgpKp = JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kp, Date())
        val sha1Calc = JcaPGPDigestCalculatorProviderBuilder().build().get(PGPUtil.SHA1)
        val contentSigner = JcaPGPContentSignerBuilder(pgpKp.publicKey.algorithm, PGPUtil.SHA256).setProvider("AndroidOpenSSL")
        val encryptor = JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha1Calc)
            .build(passphrase)
        val keyRingGen = PGPKeyRingGenerator(
            PGPSignature.POSITIVE_CERTIFICATION,
            pgpKp,
            identity,
            sha1Calc,
            null,
            null,
            contentSigner,
            encryptor
        )
        val secretRing = keyRingGen.generateSecretKeyRing()
        val out = ByteArrayOutputStream()
        ArmoredOutputStream(out).use { secretRing.encode(it) }
        return out.toString(StandardCharsets.UTF_8.name())
    }

    private fun parseInfo(armored: String): PgpKeyInfo? {
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
        val isPrivate = keyRing is PGPSecretKeyRing
        val pubKey = when (keyRing) {
            is PGPSecretKeyRing -> keyRing.publicKey
            is PGPPublicKeyRing -> keyRing.publicKey
            else -> return null
        }
        val fingerprint = pubKey.fingerprint.joinToString("") { "%02X".format(it) }
        val keyId = java.lang.Long.toHexString(pubKey.keyID).uppercase()
        val userId = pubKey.userIDs.asSequence().firstOrNull()
        val algorithm = algorithmName(pubKey.algorithm)
        val bitLength = pubKey.bitStrength
        val createdAt = pubKey.creationTime.time / 1000
        val expiresAt = if (pubKey.validSeconds > 0) createdAt + pubKey.validSeconds else null
        return PgpKeyInfo(
            userId = userId,
            fingerprint = fingerprint,
            keyId = keyId,
            isPrivate = isPrivate,
            armoredKey = armored,
            algorithm = algorithm,
            bitLength = bitLength,
            comment = null,
            createdAt = createdAt,
            expiresAt = expiresAt
        )
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
