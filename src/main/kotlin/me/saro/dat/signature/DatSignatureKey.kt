package me.saro.dat.signature

interface DatSignatureKey: Cloneable {
    fun algorithm(): DatSignatureAlgorithm
    fun verify(body: ByteArray, signature: ByteArray): Boolean
    fun sign(body: ByteArray): ByteArray
    fun getSigningKeyBytes(): ByteArray?
    fun getVerifyingKeyBytes(): ByteArray
    fun hasSigningKey(): Boolean
    public override fun clone(): DatSignatureKey

    companion object {
        @JvmStatic
        fun fromBytes(algorithm: DatSignatureAlgorithm, privateKey: ByteArray?, publicKey: ByteArray): DatSignatureKey {
            when (algorithm) {
                DatSignatureAlgorithm.P256, DatSignatureAlgorithm.P384, DatSignatureAlgorithm.P521 -> {
                    return DatSignatureKeyEcdsa.fromBytes(algorithm, if ((privateKey?.size ?: 0) > 0) privateKey else null, publicKey)
                }
            }
        }

        @JvmStatic
        fun generate(algorithm: DatSignatureAlgorithm): DatSignatureKey {
            when (algorithm) {
                DatSignatureAlgorithm.P256, DatSignatureAlgorithm.P384, DatSignatureAlgorithm.P521 -> {
                    return DatSignatureKeyEcdsa.generate(algorithm)
                }
            }
        }
    }
}