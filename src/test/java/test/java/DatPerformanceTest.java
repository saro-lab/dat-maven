package test.java;

import me.saro.dat.key.DatUtils;
import me.saro.dat.key.crypto.CryptoAlgorithm;
import me.saro.dat.key.dat.DatKey;
import me.saro.dat.key.dat.kid.Kid;
import me.saro.dat.key.dat.kid.ToKid;
import me.saro.dat.key.signature.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

public class DatPerformanceTest {
    public DatKey generate(SignatureAlgorithm signatureAlgorithm, CryptoAlgorithm cryptoAlgorithm) {
        return DatKey.generate(
                Kid.BY_LONG.toKid("0"),
                signatureAlgorithm,
                cryptoAlgorithm,
                System.currentTimeMillis() - 10,
                System.currentTimeMillis() + 600,
                60
        );
    }

    @Test
    public void test() {
        ToKid toKid = Kid.BY_LONG;
        String plain = DatUtils.generateRandomBase62(100);
        String secure = DatUtils.generateRandomBase62(100);

        for (var signatureAlgorithm : SignatureAlgorithm.getEntries()) {
            for (var cryptoAlgorithm : CryptoAlgorithm.getEntries()) {
                DatKey key = generate(signatureAlgorithm, cryptoAlgorithm);
                String tag = signatureAlgorithm.name() + "/" + cryptoAlgorithm.name();
                String dat = "";

                long time = System.currentTimeMillis();
                for (var i = 0; i < 1000; i++) {
                    dat = key.toDat(plain, secure);
                }
                System.out.println(tag + " toDat * 1000 : " + (System.currentTimeMillis() - time) + " ms");

                time = System.currentTimeMillis();
                for (var i = 0; i < 1000; i++) {
                    key.toPayload(dat, toKid);
                }
                System.out.println(tag + " toPayload * 1000 : " + (System.currentTimeMillis() - time) + " ms");
            }
        }

    }
}
