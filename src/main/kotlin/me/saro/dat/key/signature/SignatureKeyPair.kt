package me.saro.dat.key.signature

import me.saro.dat.key.exception.DatException
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec


class SignatureKeyPair(
    val algorithm: SignatureAlgorithm,
    val signingKey: PrivateKey?,
    val verifyingKey: PublicKey,
) : SignatureKey {

    companion object {
        @JvmStatic
        fun fromBytes(algorithm: SignatureAlgorithm, signingKey: ByteArray?, verifyingKey: ByteArray): SignatureKey {
            when (algorithm) {
                SignatureAlgorithm.P256, SignatureAlgorithm.P384, SignatureAlgorithm.P521 -> {
                    val spec = ECNamedCurveTable.getParameterSpec(algorithm.getECGenParameterSpecName())
                    val kf: KeyFactory = algorithm.getKeyFactory()

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

                    return SignatureKeyPair(algorithm, signingKey, verifyingKey)
                }
            }
        }

        @JvmStatic
        fun generate(algorithm: SignatureAlgorithm): SignatureKey {
            when (algorithm) {
                SignatureAlgorithm.P256, SignatureAlgorithm.P384, SignatureAlgorithm.P521 -> {
                    val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("EC", "BC")
                        .apply { initialize(ECGenParameterSpec(algorithm.getECGenParameterSpecName())) }
                    val keyPair: KeyPair = kpg.generateKeyPair()
                    return SignatureKeyPair(algorithm, keyPair.private, keyPair.public)
                }
            }
        }
    }

    override fun toAlgorithm(): SignatureAlgorithm {
        return algorithm
    }

    override fun getSigningKeyBytes(): ByteArray? {
        if (signingKey != null) {
            when (algorithm) {
                SignatureAlgorithm.P256, SignatureAlgorithm.P384, SignatureAlgorithm.P521 -> {
                    val key = (signingKey as BCECPrivateKey)
                    val d = key.d
                    val fieldSize = (algorithm.getECGenParameterSpecName().substring(4, 7).toInt() + 7) / 8
                    val bytes = d.toByteArray()
                    return if (bytes.size > fieldSize) {
                        bytes.copyOfRange(bytes.size - fieldSize, bytes.size)
                    } else if (bytes.size < fieldSize) {
                        ByteArray(fieldSize - bytes.size) + bytes
                    } else {
                        bytes
                    }
                }
            }
        }
        return null
    }

    override fun getVerifyingKeyBytes(): ByteArray {
        when (algorithm) {
            SignatureAlgorithm.P256, SignatureAlgorithm.P384, SignatureAlgorithm.P521 -> {
                return (verifyingKey as BCECPublicKey).q.getEncoded(false)
            }
        }
    }

    override fun verify(body: ByteArray, signature: ByteArray): Boolean {
        try {
            val instant: Signature = algorithm.getSignature()
            instant.initVerify(verifyingKey)
            instant.update(body)
            return instant.verify(signature)
        } catch (e: Exception) {
            return false
        }
    }

    override fun sign(body: ByteArray): ByteArray {
        if (signingKey != null) {
            val signature: Signature = algorithm.getSignature()
            signature.initSign(signingKey)
            signature.update(body)
            return signature.sign()
        }
        throw DatException("this key is verify only")
    }

    override fun hasSigningKey(): Boolean {
        return this.signingKey != null
    }

    override fun clone(): SignatureKey {
        return fromBytes(algorithm, getSigningKeyBytes(), getVerifyingKeyBytes())
    }
}