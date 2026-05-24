package me.saro.dat.signature

interface DatSignatureKey: Cloneable {
    fun algorithm(): DatSignatureAlgorithm
    fun verify(body: ByteArray, signature: ByteArray): Boolean
    fun sign(body: ByteArray): ByteArray
    fun exportKey(verifyOnly: Boolean = false): ByteArray
    fun signable(): Boolean
    public override fun clone(): DatSignatureKey

    companion object {
        @JvmStatic
        fun fromKey(algorithm: DatSignatureAlgorithm, key: ByteArray): DatSignatureKey {
            when (algorithm) {
                DatSignatureAlgorithm.P256, DatSignatureAlgorithm.P384, DatSignatureAlgorithm.P521 -> {
                    return DatSignatureKeyEcdsa.fromKey(algorithm, key)
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