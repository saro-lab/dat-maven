package me.saro.dat.key.signature

interface SignatureKey: Cloneable {
    fun toAlgorithm(): SignatureAlgorithm
    fun getSigningKeyBytes(): ByteArray?
    fun getVerifyingKeyBytes(): ByteArray
    fun verify(body: ByteArray, signature: ByteArray): Boolean
    fun sign(body: ByteArray): ByteArray
    public override fun clone(): SignatureKey

    companion object {
        @JvmStatic
        fun fromBytes(algorithm: SignatureAlgorithm, privateKey: ByteArray?, publicKey: ByteArray): SignatureKey {
            when (algorithm) {
                SignatureAlgorithm.P256, SignatureAlgorithm.P384, SignatureAlgorithm.P521 -> {
                    return SignatureKeyPair.fromBytes(algorithm, if ((privateKey?.size ?: 0) > 0) privateKey else null, publicKey)
                }
            }
        }

        @JvmStatic
        fun generate(algorithm: SignatureAlgorithm): SignatureKey {
            when (algorithm) {
                SignatureAlgorithm.P256, SignatureAlgorithm.P384, SignatureAlgorithm.P521 -> {
                    return SignatureKeyPair.generate(algorithm)
                }
            }
        }
    }
}