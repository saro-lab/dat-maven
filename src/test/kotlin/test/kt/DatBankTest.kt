package test.kt

import me.saro.dat.key.DatUtils.Companion.generateRandomBase62
import me.saro.dat.key.bank.DatBank
import me.saro.dat.key.crypto.CryptoAlgorithm
import me.saro.dat.key.dat.DatKey
import me.saro.dat.key.dat.DatKey.Companion.generate
import me.saro.dat.key.signature.SignatureAlgorithm
import me.saro.dat.key.signature.SignatureKeyOutOption
import org.junit.jupiter.api.Test

class DatBankTest {
    fun generate(kid: String, signatureAlgorithm: SignatureAlgorithm, cryptoAlgorithm: CryptoAlgorithm): DatKey {
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
        val plain = generateRandomBase62(30)
        val secure = generateRandomBase62(30)
        val bank = DatBank()
        val dats: MutableList<String> = ArrayList<String>()
        var i: Long = 1

        for (signatureAlgorithm in SignatureAlgorithm.entries) {
            for (cryptoAlgorithm in CryptoAlgorithm.entries) {
                repeat(20) {
                    val key = generate((i++).toString(), signatureAlgorithm, cryptoAlgorithm)
                    dats.add(key.toDat(plain, secure))
                    bank.imports(listOf(key), false)
                }
            }
        }

        val readBank = DatBank()
        readBank.imports(bank.exports(SignatureKeyOutOption.FULL), true)

        for (dat in dats) {
            val payload = readBank.toPayload(dat)
            assert(plain == payload.plain)
            assert(secure == payload.secure)
            println("DatBank.PASS.$dat")
        }
    }

    //@Test
    fun tmp() {
        val bank = DatBank()


        val format =
            "2.208.P256.0l0Zg3M6awe-EazlOPu2toOeCNLG0fJSg0jyFMxS0GA.AES128GCMN.80vsGYE1I0FuIg6IsGTcmg.1777223714.1777227314.1800\n" +
                    "2.209.P256.0pPQ_IDM6cE_cnKoJCOpAvfwwPzdJCJkwFG2Kf43Oyg.AES128GCMN.leyMYpNByPzFRbNJ2-ThLw.1777224000.1777227600.1800\n" +
                    "2.210.P256.WtoJ0Iewmaj8TciUUXNN1FQZ-w81LWw4LviMm13XWoQ.AES128GCMN.njKFMdgqgeaXxk7VOHyCBA.1777224600.1777228200.1800\n" +
                    "2.211.P256.wIFMUNe2aeft8PZLbNGaY-tVDnjyQpcwc5c_P-t0ToQ.AES128GCMN.3x5V2iHBdRfcxFt2tkPL9w.1777225200.1777228800.1800\n" +
                    "2.212.P256.GN_JVc6H1Br7JxGZdPeiFLOwZXv9aMNL1my83VOsMt8.AES128GCMN.U0-HEiRp_P7Pb0w1jCmtpw.1777225800.1777229400.1800\n" +
                    "2.213.P256.neVA44aC5tondxG_kpRbqGPQfBvVvQbwDoe4H0mSGmQ.AES128GCMN.pXJsuId09_3MqtVZyr57NA.1777226400.1777230000.1800\n" +
                    "2.214.P256.24ckfCvPnmjwEPIGWnYcfBeIm3smsD9ZOh4_hc7HjME.AES128GCMN.Qh5mEtpSN_3SQdQuC_Lz4Q.1777227000.1777230600.1800\n" +
                    "2.215.P256.q4sNyIpb1HYguLwPFppKeIasODrYWpd8civBZYXATqo.AES128GCMN.IfUaX9a-WRR9Ex7IWWCFPQ.1777227600.1777231200.1800\n" +
                    "2.216.P256.gr0o_sVihBuuB8H1XelCYlOl6rQ2tEXcb_2Ry13FSiY.AES128GCMN.5YmLe8ciqWdWaHE9DNqEgg.1777228200.1777231800.1800\n" +
                    "2.217.P256.yLBPmlbBkAAz95vU8LRImpfDW4NiJUpTWH7iYkH6UmY.AES128GCMN.fLmUj4J3TA86ltBGKBPNAA.1777228800.1777232400.1800\n" +
                    "2.218.P256.wJa-a4lD4g7d-GxqGtf4vnSeRRiIKDm80vph99Sv1w8.AES128GCMN.fSQWo5PMDBrWis8mM2xrNg.1777229400.1777233000.1800\n" +
                    "2.219.P256.dSvJRLdmeRVHuzF5CkcoJ8gKZbZZLvk-okMrPUWnODw.AES128GCMN.0hvJxx8FKNr_OVEMt5PzRA.1777229615.1777233215.1800\n" +
                    "2.220.P256.YvrE6Sn-1_tNQxVwT1qr2a9MfKLD_02X8TKD5xvfgf8.AES128GCMN.7imRXI1R-Jf730TqkgOm5Q.1777229631.1777233231.180"


        bank.imports(format, true)

        val dat = bank.toDat("plain", "secure")

        val payload = bank.toPayload(dat)

        println(payload)
    }
}
