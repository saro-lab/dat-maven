package test.kt

import me.saro.dat.key.DatUtils.Companion.generateRandomBase62
import me.saro.dat.key.crypto.CryptoAlgorithm
import me.saro.dat.key.dat.DatKey
import me.saro.dat.key.dat.DatKey.Companion.generate
import me.saro.dat.key.signature.SignatureAlgorithm
import org.junit.jupiter.api.Test

class DatPerformanceTest {
    fun generate(signatureAlgorithm: SignatureAlgorithm, cryptoAlgorithm: CryptoAlgorithm): DatKey {
        return generate(
            "0",
            signatureAlgorithm,
            cryptoAlgorithm,
            System.currentTimeMillis() - 10,
            System.currentTimeMillis() + 600,
            60
        )
    }

    @Test
    fun test() {
        val plain = generateRandomBase62(100)
        val secure = generateRandomBase62(100)

        for (signatureAlgorithm in SignatureAlgorithm.entries) {
            for (cryptoAlgorithm in CryptoAlgorithm.entries) {
                val key = generate(signatureAlgorithm, cryptoAlgorithm)
                val tag: String = signatureAlgorithm.name + "/" + cryptoAlgorithm.name
                var dat = ""

                var time = System.currentTimeMillis()
                repeat(1000) {
                    dat = key.toDat(plain, secure)
                }
                println(tag + " toDat * 1000 : " + (System.currentTimeMillis() - time) + " ms")

                time = System.currentTimeMillis()
                repeat(1000) {
                    key.toPayload(dat)
                }
                println(tag + " toPayload * 1000 : " + (System.currentTimeMillis() - time) + " ms")
            }
        }
    }
}
