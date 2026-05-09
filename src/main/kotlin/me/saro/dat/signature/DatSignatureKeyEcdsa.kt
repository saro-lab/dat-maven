package me.saro.dat.signature

import me.saro.dat.exception.DatException
import me.saro.dat.signature.DatSignatureAlgorithm.*
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec


class DatSignatureKeyEcdsa private constructor(
    private val algorithm: DatSignatureAlgorithm,
    private val signingKey: BCECPrivateKey?,
    private val verifyingKey: BCECPublicKey,
) : DatSignatureKey {

    companion object {
        private val BOUNCY_CASTLE_PROVIDER = BouncyCastleProvider().apply { Security.addProvider(this) }

        @JvmStatic
        internal fun fromBytes(algorithm: DatSignatureAlgorithm, signingKey: ByteArray?, verifyingKey: ByteArray): DatSignatureKey {
            val spec = ECNamedCurveTable.getParameterSpec(getECGenParameterSpecName(algorithm))
            val kf: KeyFactory = getKeyFactory(algorithm)

            val signingKey = signingKey?.let {
                val d = BigInteger(1, it) // 1은 양수를 의미
                kf.generatePrivate(ECPrivateKeySpec(d, spec))
            }

            val verifyingKey = if (signingKey != null && verifyingKey.isEmpty()) {
                val d = (signingKey as BCECPrivateKey).d
                val q = spec.g.multiply(d) // 기준점 G에 개인키 d를 곱함
                kf.generatePublic(ECPublicKeySpec(q, spec))
            } else {
                val point = spec.curve.decodePoint(verifyingKey)
                val pubSpec = ECPublicKeySpec(point, spec)
                kf.generatePublic(pubSpec)
            }

            return DatSignatureKeyEcdsa(algorithm, signingKey as BCECPrivateKey?, verifyingKey as BCECPublicKey)
        }

        @JvmStatic
        internal fun generate(algorithm: DatSignatureAlgorithm): DatSignatureKey {
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("EC", BOUNCY_CASTLE_PROVIDER)
                .apply { initialize(ECGenParameterSpec(getECGenParameterSpecName(algorithm))) }
            val keyPair: KeyPair = kpg.generateKeyPair()
            return DatSignatureKeyEcdsa(algorithm, keyPair.private as BCECPrivateKey, keyPair.public as BCECPublicKey)
        }

        private fun getKeyFactory(algorithm: DatSignatureAlgorithm): KeyFactory {
            return when (algorithm) {
                P256, P384, P521 -> KeyFactory.getInstance("EC", BOUNCY_CASTLE_PROVIDER)
            }
        }

        private fun getSignature(algorithm: DatSignatureAlgorithm): Signature {
            return when (algorithm) {
                P256 -> Signature.getInstance("SHA256withPLAIN-ECDSA")
                P384 -> Signature.getInstance("SHA384withPLAIN-ECDSA")
                P521 -> Signature.getInstance("SHA512withPLAIN-ECDSA")
            }
        }

        private fun getECGenParameterSpecName(algorithm: DatSignatureAlgorithm): String {
            return when (algorithm) {
                P256 -> "secp256r1"
                P384 -> "secp384r1"
                P521 -> "secp521r1"
            }
        }

        private fun getBitSize(algorithm: DatSignatureAlgorithm): Int {
            return when (algorithm) {
                P256 -> 256
                P384 -> 384
                P521 -> 521
            }
        }
    }

    override fun algorithm(): DatSignatureAlgorithm {
        return algorithm
    }

    override fun getSigningKeyBytes(): ByteArray? {
        if (signingKey != null) {
            val key = (signingKey as BCECPrivateKey)
            val d = key.d
            val fieldSize = (getBitSize(algorithm) + 7) / 8
            val bytes = d.toByteArray()
            return if (bytes.size > fieldSize) {
                bytes.copyOfRange(bytes.size - fieldSize, bytes.size)
            } else if (bytes.size < fieldSize) {
                ByteArray(fieldSize - bytes.size) + bytes
            } else {
                bytes
            }
        }
        return null
    }

    override fun getVerifyingKeyBytes(): ByteArray {
        return (verifyingKey as BCECPublicKey).q.getEncoded(false)
    }

    override fun verify(body: ByteArray, signature: ByteArray): Boolean {
        try {
            val instant: Signature = getSignature(algorithm)
            instant.initVerify(verifyingKey)
            instant.update(body)
            return instant.verify(signature)
        } catch (e: Exception) {
            return false
        }
    }

    override fun sign(body: ByteArray): ByteArray {
        if (signingKey != null) {
            val signature: Signature = getSignature(algorithm)
            signature.initSign(signingKey)
            signature.update(body)
            return signature.sign()
        }
        throw DatException("VerifyingKey Only Key: Is not Have Signing Key")
    }

    override fun hasSigningKey(): Boolean {
        return this.signingKey != null
    }

    override fun clone(): DatSignatureKey {
        return fromBytes(algorithm, getSigningKeyBytes(), getVerifyingKeyBytes())
    }
}