package me.saro.dat.key.crypto

import me.saro.dat.key.crypto.CryptoAlgorithm.AES128GCMN
import me.saro.dat.key.crypto.CryptoAlgorithm.AES256GCMN

interface CryptoKey: Cloneable {
    fun toAlgorithm(): CryptoAlgorithm
    fun toBytes(): ByteArray
    fun encrypt(bytes: ByteArray): ByteArray
    fun decrypt(bytes: ByteArray): ByteArray
    public override fun clone(): CryptoKey

    companion object {
        @JvmStatic
        fun fromBytes(algorithm: CryptoAlgorithm, bytes: ByteArray): CryptoKey {
            when (algorithm) {
                AES128GCMN, AES256GCMN -> {
                    return CryptoKeyAesGcmNonce.fromBytes(algorithm, bytes)
                }
            }
            //throw DatException("""$algorithm does not support""")
        }

        @JvmStatic
        fun generate(algorithm: CryptoAlgorithm): CryptoKey {
            when (algorithm) {
                AES128GCMN, AES256GCMN -> {
                    return CryptoKeyAesGcmNonce.generate(algorithm)
                }
            }
            //throw DatException("""$algorithm does not support""")
        }
    }
}
