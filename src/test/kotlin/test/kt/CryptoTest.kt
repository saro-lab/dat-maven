package test.kt

import me.saro.dat.DatUtils.Companion.generateRandomBase62
import me.saro.dat.crypto.DatCryptoAlgorithm
import me.saro.dat.crypto.DatCryptoKey.Companion.fromBytes
import me.saro.dat.crypto.DatCryptoKey.Companion.generate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CryptoTest {
    fun unit(alg: DatCryptoAlgorithm) {
        val body = generateRandomBase62(100).toByteArray()
        val cryptoKey = generate(alg)
        val cryptoKeyFail = generate(alg)
        val cryptoKeyBytes = cryptoKey.toBytes()
        val cryptoKeyFrom = fromBytes(alg, cryptoKeyBytes)

        val encrypted = cryptoKeyFrom.encrypt(body)

        assert(cryptoKey.decrypt(encrypted).contentEquals(body))
        assert(cryptoKeyFrom.decrypt(encrypted).contentEquals(body))
        Assertions.assertThrows(Exception::class.java, { cryptoKeyFail.decrypt(encrypted) })
    }

    @Test
    fun test() {
        for (algorithm in DatCryptoAlgorithm.entries) {
            println("crypto test - " + algorithm.name)
            for (i in 0..19) {
                unit(algorithm)
            }
        }
    }
}
