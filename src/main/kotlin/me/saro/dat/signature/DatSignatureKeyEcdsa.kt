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

        internal fun getPrivateKeySize(algorithm: DatSignatureAlgorithm): Int {
            return when (algorithm) {
                P256 -> 32
                P384 -> 48
                P521 -> 66
            }
        }

        internal fun getPublicKeySize(algorithm: DatSignatureAlgorithm): Int {
            return when (algorithm) {
                P256 -> 65
                P384 -> 97
                P521 -> 133
            }
        }

        @JvmStatic
        internal fun fromKey(algorithm: DatSignatureAlgorithm, key: ByteArray): DatSignatureKey {
            val spec = ECNamedCurveTable.getParameterSpec(getECGenParameterSpecName(algorithm))
            val kf: KeyFactory = getKeyFactory(algorithm)
            val privateKeySize = getPrivateKeySize(algorithm)
            val publicKeySize = getPublicKeySize(algorithm)

            val signingKey = if (key.size == privateKeySize + publicKeySize) {
                val pk = key.sliceArray(0 until privateKeySize);
                kf.generatePrivate(ECPrivateKeySpec(BigInteger(1, pk), spec))
            } else if (key.size == publicKeySize) {
                null
            } else {
                throw DatException("Invalid Dat Signature Key Size: $algorithm ${key.size}")
            }

            val verifyKeyByte = if (key.size == publicKeySize) {
                key
            } else {
                key.sliceArray(privateKeySize until key.size)
            }

            val point = spec.curve.decodePoint(verifyKeyByte)
            val pubSpec = ECPublicKeySpec(point, spec)
            val verifyingKey = kf.generatePublic(pubSpec)

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

    override fun exportKey(verifyOnly: Boolean): ByteArray {
        if (verifyOnly && signingKey == null) {
            throw DatException("verifyOnly: Is not Have Signing Key")
        }

        val vk = (verifyingKey).q.getEncoded(false)
        if (signingKey != null) {
            val key = (signingKey as BCECPrivateKey)
            val d = key.d
            val fieldSize = (getBitSize(algorithm) + 7) / 8
            val bytes = d.toByteArray()
            return if (bytes.size > fieldSize) {
                (bytes.copyOfRange(bytes.size - fieldSize, bytes.size)) + vk
            } else if (bytes.size < fieldSize) {
                (ByteArray(fieldSize - bytes.size) + bytes) +  vk
            } else {
                bytes + vk
            }
        } else {
            return vk
        }
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

    override fun signable(): Boolean {
        return this.signingKey != null
    }

    override fun clone(): DatSignatureKey {
        return fromKey(algorithm, exportKey())
    }
}