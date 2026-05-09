package me.saro.dat.crypto

import me.saro.dat.crypto.DatCryptoAlgorithm.AES128GCMN
import me.saro.dat.crypto.DatCryptoAlgorithm.AES256GCMN

interface DatCryptoKey: Cloneable {
    fun algorithm(): DatCryptoAlgorithm
    fun toBytes(): ByteArray
    fun encrypt(bytes: ByteArray): ByteArray
    fun decrypt(bytes: ByteArray): ByteArray
    public override fun clone(): DatCryptoKey

    companion object {
        @JvmStatic
        fun fromBytes(algorithm: DatCryptoAlgorithm, bytes: ByteArray): DatCryptoKey {
            when (algorithm) {
                AES128GCMN, AES256GCMN -> {
                    return DatCryptoKeyAesGcmNonce.fromBytes(algorithm, bytes)
                }
            }
            //throw DatException("""$algorithm does not support""")
        }

        @JvmStatic
        fun generate(algorithm: DatCryptoAlgorithm): DatCryptoKey {
            when (algorithm) {
                AES128GCMN, AES256GCMN -> {
                    return DatCryptoKeyAesGcmNonce.generate(algorithm)
                }
            }
            //throw DatException("""$algorithm does not support""")
        }
    }
}
