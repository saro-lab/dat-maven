package test.kt

import me.saro.dat.key.DatUtils.Companion.generateRandomBase62
import me.saro.dat.key.crypto.CryptoAlgorithm
import me.saro.dat.key.dat.DatKey
import me.saro.dat.key.dat.DatKey.Companion.generate
import me.saro.dat.key.dat.DatKey.Companion.parse
import me.saro.dat.key.signature.SignatureAlgorithm
import me.saro.dat.key.signature.SignatureKeyOutOption
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class DatKeyTest {
    fun unit(failKey: DatKey, signatureAlgorithm: SignatureAlgorithm, cryptoAlgorithm: CryptoAlgorithm) {
        val tag = "dat." + signatureAlgorithm.name + "." + cryptoAlgorithm.name

        val plain = generateRandomBase62(30)
        val secure = generateRandomBase62(30)

        val newKey = generate(signatureAlgorithm, cryptoAlgorithm)
        val newKeyStr = newKey.exports(SignatureKeyOutOption.FULL)

        val readKey = parse(newKeyStr)

        val dat = newKey.toDat(plain, secure)
        println("$tag: $dat")

        val payload = readKey.toPayload(dat)
        println(tag + ": " + payload.plain + " / " + payload.secure)

        assert(plain == payload.plain)
        assert(secure == payload.secure)
        Assertions.assertThrows(Exception::class.java, Executable { failKey.toPayload(dat) })
    }


    fun generate(signatureAlgorithm: SignatureAlgorithm, cryptoAlgorithm: CryptoAlgorithm): DatKey {
        return generate(
            generateRandomBase62(10),
            signatureAlgorithm,
            cryptoAlgorithm,
            System.currentTimeMillis() - 10,
            System.currentTimeMillis() + 600,
            60
        )
    }

    @Test
    fun test() {
        val failKey = generate(SignatureAlgorithm.P256, CryptoAlgorithm.AES128GCMN)

        for (signatureAlgorithm in SignatureAlgorithm.entries) {
            for (cryptoAlgorithm in CryptoAlgorithm.entries) {
                repeat(20) {
                    unit(failKey, signatureAlgorithm, cryptoAlgorithm)
                }
            }
        }
    }
}
