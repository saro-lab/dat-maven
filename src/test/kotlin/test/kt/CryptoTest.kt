package test.kt

import me.saro.dat.key.DatUtils.Companion.generateRandomBase62
import me.saro.dat.key.crypto.CryptoAlgorithm
import me.saro.dat.key.crypto.CryptoKey.Companion.fromBytes
import me.saro.dat.key.crypto.CryptoKey.Companion.generate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CryptoTest {
    fun unit(alg: CryptoAlgorithm) {
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
        for (alg in CryptoAlgorithm.entries) {
            println("crypto test - " + alg.name)
            repeat(20) {
                unit(alg)
            }
        }
    }
}
