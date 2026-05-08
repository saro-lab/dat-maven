package test.kt

import me.saro.dat.key.DatUtils.Companion.generateRandomBase62
import me.saro.dat.key.signature.SignatureAlgorithm
import me.saro.dat.key.signature.SignatureKey.Companion.fromBytes
import me.saro.dat.key.signature.SignatureKey.Companion.generate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SignatureTest {
    fun unit(alg: SignatureAlgorithm) {
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
        for (alg in SignatureAlgorithm.entries) {
            println("sign test - " + alg.name)
            repeat(20) {
                unit(alg)
            }
        }
    }
}
