package me.saro.dat.key.crypto

import me.saro.dat.key.crypto.CryptoAlgorithm.AES128GCMN
import me.saro.dat.key.crypto.CryptoAlgorithm.AES256GCMN
import me.saro.dat.key.exception.DatException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoKeyAesGcmNonce(
    val algorithm: CryptoAlgorithm,
    val key: SecretKeySpec,
): CryptoKey {
    companion object {
        private const val NONCE_LEN = 12

        @JvmStatic
        fun fromBytes(algorithm: CryptoAlgorithm, bytes: ByteArray): CryptoKey {
            when (algorithm) {
                AES128GCMN, AES256GCMN -> {
                    if (bytes.size != algorithm.getKeySize()) {
                        throw DatException("""invalid key size""")
                    }
                    val key = SecretKeySpec(bytes, "AES")
                    return CryptoKeyAesGcmNonce(algorithm, key)
                }
            }
        }

        @JvmStatic
        fun generate(algorithm: CryptoAlgorithm): CryptoKey {
            when (algorithm) {
                AES128GCMN, AES256GCMN -> {
                    val rand = ByteArray(algorithm.getKeySize()).apply { SecureRandom().nextBytes(this) }
                    val key = SecretKeySpec(rand, "AES")
                    return CryptoKeyAesGcmNonce(algorithm, key)
                }
            }
        }
    }

    fun getCipher(mode: Int, nonce: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding");
        val spec = GCMParameterSpec(128, nonce)
        cipher.init(mode, key, spec)
        return cipher
    }

    fun newNonce(): ByteArray {
        return ByteArray(NONCE_LEN).apply { SecureRandom().nextBytes(this) }
    }

    override fun toAlgorithm(): CryptoAlgorithm {
        return algorithm
    }

    override fun toBytes(): ByteArray {
        return key.encoded
    }

    override fun encrypt(bytes: ByteArray): ByteArray {
        val nonce: ByteArray = newNonce()
        val cipher = getCipher(Cipher.ENCRYPT_MODE, nonce)
        return nonce + cipher.doFinal(bytes)
    }

    override fun decrypt(bytes: ByteArray): ByteArray {
        val nonce = bytes.sliceArray(0 until NONCE_LEN)
        val encrypted = bytes.sliceArray(NONCE_LEN until bytes.size)
        val cipher = getCipher(Cipher.DECRYPT_MODE, nonce)
        return cipher.doFinal(encrypted)
    }

    override fun clone(): CryptoKey {
        return fromBytes(algorithm, toBytes())
    }
}