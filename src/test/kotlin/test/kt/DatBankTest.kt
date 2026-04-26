package test.kt

import me.saro.dat.key.DatUtils.Companion.generateRandomBase62
import me.saro.dat.key.bank.DatBank
import me.saro.dat.key.crypto.CryptoAlgorithm
import me.saro.dat.key.dat.DatKey
import me.saro.dat.key.dat.DatKey.Companion.generate
import me.saro.dat.key.dat.kid.Kid
import me.saro.dat.key.dat.kid.KidLong
import me.saro.dat.key.signature.SignatureAlgorithm
import me.saro.dat.key.signature.SignatureKeyOutOption
import org.junit.jupiter.api.Test

class DatBankTest {
    fun generate(kid: Kid, signatureAlgorithm: SignatureAlgorithm, cryptoAlgorithm: CryptoAlgorithm): DatKey {
        return generate(
            kid,
            signatureAlgorithm,
            cryptoAlgorithm,
            System.currentTimeMillis() - 10,
            System.currentTimeMillis() + 600,
            60
        )
    }

    @Test
    fun test() {
        val toKid = Kid.BY_LONG
        val plain = generateRandomBase62(30)
        val secure = generateRandomBase62(30)
        val bank = DatBank(toKid)
        val dats: MutableList<String> = ArrayList<String>()
        var i: Long = 1

        for (signatureAlgorithm in SignatureAlgorithm.entries) {
            for (cryptoAlgorithm in CryptoAlgorithm.entries) {
                repeat(20) {
                    val key = generate(KidLong(i++), signatureAlgorithm, cryptoAlgorithm)
                    dats.add(key.toDat(plain, secure))
                    bank.importKeys(listOf(key), false)
                }
            }
        }

        val readBank = DatBank(toKid)
        readBank.importKeysFormat(bank.exportKeysFormat(SignatureKeyOutOption.FULL), true)

        for (dat in dats) {
            val payload = readBank.toPayload(dat)
            assert(plain == payload.plain)
            assert(secure == payload.secure)
            println("DatBank.PASS.$dat")
        }
    }
}
