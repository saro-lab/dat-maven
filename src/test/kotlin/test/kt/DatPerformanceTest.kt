package test.kt

import me.saro.dat.DatUtils.Companion.generateRandomBase62
import me.saro.dat.crypto.DatCryptoAlgorithm
import me.saro.dat.dat.DatCertificate
import me.saro.dat.dat.DatCertificate.Companion.generate
import me.saro.dat.dat.DatManager.Companion.issue
import me.saro.dat.dat.DatManager.Companion.parse
import me.saro.dat.signature.DatSignatureAlgorithm
import org.junit.jupiter.api.Test

class DatPerformanceTest {
    fun generate(signatureAlgorithm: DatSignatureAlgorithm, cryptoAlgorithm: DatCryptoAlgorithm): DatCertificate {
        return generate(
            0,
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

        println("plain : " + plain)
        println("secure : " + secure)

        for (signatureAlgorithm in DatSignatureAlgorithm.entries) {
            for (cryptoAlgorithm in DatCryptoAlgorithm.entries) {
                val cert = generate(signatureAlgorithm, cryptoAlgorithm)
                val tag: String = signatureAlgorithm.name + "/" + cryptoAlgorithm.name
                var dat = ""

                var time = System.currentTimeMillis()
                for (i in 0..9999) {
                    dat = issue(cert, plain, secure)
                }
                println(tag + " issue * 10000 : " + (System.currentTimeMillis() - time) + " ms")

                time = System.currentTimeMillis()
                for (i in 0..9999) {
                    parse(cert, dat)
                }
                println(tag + " parse * 10000 : " + (System.currentTimeMillis() - time) + " ms")
            }
        }
    }
}
