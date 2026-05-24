package me.saro.dat.crypto

import me.saro.dat.exception.DatException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class DatCryptoAesGcmNonce private constructor(
    private val algorithm: DatCryptoAlgorithm,
    private val key: SecretKeySpec,
): DatCrypto {
    companion object {
        private const val NONCE_LEN = 12

        private fun getKeySize(algorithm: DatCryptoAlgorithm): Int {
            return when (algorithm) {
                DatCryptoAlgorithm.IV_AES128_GCM -> 16
                DatCryptoAlgorithm.IV_AES256_GCM -> 32
            }
        }

        internal fun fromBytes(algorithm: DatCryptoAlgorithm, bytes: ByteArray): DatCrypto {
            if (bytes.size != getKeySize(algorithm)) {
                throw DatException("Invalid $algorithm Key Size: ${bytes.size}")
            }
            val key = SecretKeySpec(bytes, "AES")
            return DatCryptoAesGcmNonce(algorithm, key)
        }

        internal fun generate(algorithm: DatCryptoAlgorithm): DatCrypto {
            val rand = ByteArray(getKeySize(algorithm)).apply { SecureRandom().nextBytes(this) }
            val key = SecretKeySpec(rand, "AES")
            return DatCryptoAesGcmNonce(algorithm, key)
        }
    }

    private fun getCipher(mode: Int, nonce: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding");
        val spec = GCMParameterSpec(128, nonce)
        cipher.init(mode, key, spec)
        return cipher
    }

    override fun algorithm(): DatCryptoAlgorithm {
        return algorithm
    }

    override fun toBytes(): ByteArray {
        return key.encoded
    }

    override fun encrypt(bytes: ByteArray): ByteArray {
        val nonce: ByteArray = ByteArray(NONCE_LEN).apply { SecureRandom().nextBytes(this) }
        val cipher = getCipher(Cipher.ENCRYPT_MODE, nonce)
        return nonce + cipher.doFinal(bytes)
    }

    override fun decrypt(bytes: ByteArray): ByteArray {
        val nonce = bytes.sliceArray(0 until NONCE_LEN)
        val encrypted = bytes.sliceArray(NONCE_LEN until bytes.size)
        val cipher = getCipher(Cipher.DECRYPT_MODE, nonce)
        return cipher.doFinal(encrypted)
    }

    override fun clone(): DatCrypto {
        return fromBytes(algorithm, toBytes())
    }
}