package test.kt

import me.saro.dat.DatUtils.Companion.generateRandomBase62
import me.saro.dat.signature.DatSignatureAlgorithm
import me.saro.dat.signature.DatSignatureKey.Companion.fromBytes
import me.saro.dat.signature.DatSignatureKey.Companion.generate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SignatureTest {
    fun unit(alg: DatSignatureAlgorithm) {
        val body = generateRandomBase62(100).toByteArray()
        val signatureKey = generate(alg)
        val signatureKeyFail = generate(alg)
        val signingKeyBytes = signatureKey.getSigningKeyBytes()
        val verifyingKeyBytes = signatureKey.getVerifyingKeyBytes()
        val signatureKeyFrom = fromBytes(alg, signingKeyBytes, verifyingKeyBytes)
        val verifyKeyFrom = fromBytes(alg, null, verifyingKeyBytes)

        val sign = signatureKeyFrom.sign(body)

        Assertions.assertTrue(signatureKey.verify(body, sign))
        Assertions.assertTrue(signatureKeyFrom.verify(body, sign))
        Assertions.assertTrue(verifyKeyFrom.verify(body, sign))
        Assertions.assertFalse(signatureKeyFail.verify(body, sign))
    }

    @Test
    fun test() {
        for (algorithm in DatSignatureAlgorithm.entries) {
            println("sign test - " + algorithm.name)
            for (i in 0..19) {
                unit(algorithm)
            }
        }
    }
}
