package me.saro.dat.key.signature

import java.security.KeyFactory
import java.security.Signature

enum class SignatureAlgorithm {
    P256,
    P384,
    P521,
    ;

    fun getKeyFactory(): KeyFactory {
        return when (this) {
            P256, P384, P521 -> KeyFactory.getInstance("EC")
        }
    }

    fun getSignature(): Signature {
        return when (this) {
            P256 -> Signature.getInstance("SHA256withPLAIN-ECDSA")
            P384 -> Signature.getInstance("SHA384withPLAIN-ECDSA")
            P521 -> Signature.getInstance("SHA512withPLAIN-ECDSA")
        }
    }

    fun getECGenParameterSpecName(): String {
        return when (this) {
            P256 -> "secp256r1"
            P384 -> "secp384r1"
            P521 -> "secp521r1"
        }
    }
}
