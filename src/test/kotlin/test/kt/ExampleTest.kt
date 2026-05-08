package test.kt

import me.saro.dat.key.Unixtime.Companion.now
import me.saro.dat.key.bank.DatBank
import me.saro.dat.key.crypto.CryptoAlgorithm
import me.saro.dat.key.dat.DatKey
import me.saro.dat.key.dat.DatKey.Companion.generate
import me.saro.dat.key.signature.SignatureAlgorithm
import org.junit.jupiter.api.Test

class ExampleTest {
    @Test
    fun selfTest() {
        // Singleton Bank
        val bank = DatBank()

        val unixTime = now()

        val key = generate(
            "key-id",
            SignatureAlgorithm.P256,
            CryptoAlgorithm.AES128GCMN,
            unixTime - 1,  // issue begin time (unix time)
            unixTime + 1800,  // issue end time (unix time)
            1800 // DAT lifetime (seconds)
        )

        bank.imports(listOf<DatKey>(key), false)


        // example data
        val plainData = "plain data 유니코드 !!! ABCD"
        val secureData = ">! secure data 암호화 데이터 @@@@"

        println("plain : $plainData")
        println("secure : $secureData")

        // to dat
        val dat = bank.toDat(plainData, secureData)
        println("dat : $dat")

        // dat to payload
        val payload = bank.toPayload(dat)

        val payloadPlain = payload.plain
        val payloadSecure = payload.secure

        println("payload plain : $payloadPlain")
        println("payload secure : $payloadSecure")

        assert(plainData == payloadPlain)
        assert(secureData == payloadSecure)
    }


    @Test
    fun useDatBankServer() {
        // https://github.com/saro-lab/dat-bank
        //
        // docker run -d --name dat -p 8088:80 \
        //  -e SINGLE_SERVER=CRON \
        //  sarolab/dat
        //

        // Singleton Bank

        val bank = DatBank()

        // format = curl http://localhost:8088/keys
        val format =
            "2.0.P256.I5P_FNPSCiQrw12CXj8qYkBH_v3wFYmXBtTpmED59bs.AES128GCMN.SVz-zzee5hz9OzEHxQgEaA.1.17781627851.1800\n" +
                    "2.1.P256.mIA7RLJERhLD95pOpq9zxNLd98haUIbDzRR8IeWZA8c.AES128GCMN.6SnRhvQB3yh-PotQ8e_6nw.1.17781627851.1800\n" +
                    "2.2.P256.wRS5kklcIMdUJpCixUA4_pZNpaI1X34DK2txUGPqjd0.AES128GCMN.mCEWOK2jOWzES7LnQJtczw.1.17781627851.1800\n" +
                    "2.3.P256.YeX4JWaYQQh8HKctWsh6NuYtElCFRlRH1OyBOsGzdTM.AES128GCMN.42CSIN_0Zu-tpZqRvnMY7A.1.17781627851.1800\n" +
                    "2.4.P256.9o5N5hd6m3xHwUQhESm2-ghz1zQM9F_KI3LGqIlvvoA.AES128GCMN.ePrjtPJr25dgopD6-TgJxg.1.17781627851.1800\n" +
                    "2.5.P256.C9s4V_BWxxVtmoeIXsKcV4YKVmDdHcAWwVzvsEUzA0E.AES128GCMN.Kk0cQ9HLHWvfbJMAsKPXAg.1.17781627851.1800\n" +
                    "2.6.P256.iEqgUQRHxmFJOT1rVGK9fGleGKSsbCDo6hK7EZxVExU.AES128GCMN.u6YbhAZ5L7d6r1W3oStBjA.1.17781627851.1800\n" +
                    "2.7.P256.1hKGy6NQgrtvOBmb4YRZIJwoU1EPYTQPIDOkhBTx8PQ.AES128GCMN.MP0pBCEVCyeeyge6r1-VSw.1.17781627851.1800\n" +
                    "2.8.P256.iOEL5ERwtTmmmp7A4sVhDkTedhi4e6F53wG2xDRDEoE.AES128GCMN.VgojardW72K2jkbqNafegw.1.17781627851.1800\n" +
                    "2.9.P256.VPGAAvJhJ1KXkdPvz1AiWEHiCVR9u8KME0AOUso3-vI.AES128GCMN.Mfx6GnQZUzC0N4q0eB6PXQ.1.17781627851.1800"

        // import
        bank.imports(format, false)


        // example data
        val plainData = "plain data 유니코드 !!!"
        val secureData = ">! secure data 암호화 데이터"

        println("plain : $plainData")
        println("secure : $secureData")

        // to dat
        val dat = bank.toDat(plainData, secureData)
        println("dat : $dat")

        // dat to payload
        val payload = bank.toPayload(dat)

        val payloadPlain = payload.plain
        val payloadSecure = payload.secure

        println("payload plain : $payloadPlain")
        println("payload secure : $payloadSecure")

        assert(plainData == payloadPlain)
        assert(secureData == payloadSecure)
    }
}
