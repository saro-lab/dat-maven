package test.java;

import me.saro.dat.DatUtils;
import me.saro.dat.crypto.DatCryptoAlgorithm;
import me.saro.dat.dat.DatCertificate;
import me.saro.dat.dat.DatManager;
import me.saro.dat.signature.DatSignatureAlgorithm;
import org.junit.jupiter.api.Test;

public class DatPerformanceTest {
    public DatCertificate generate(DatSignatureAlgorithm signatureAlgorithm, DatCryptoAlgorithm cryptoAlgorithm) {
        return DatCertificate.generate(
                0,
                signatureAlgorithm,
                cryptoAlgorithm,
                System.currentTimeMillis() - 10,
                System.currentTimeMillis() + 600,
                60
        );
    }

    @Test
    public void test() {
        String plain = DatUtils.generateRandomBase62(100);
        String secure = DatUtils.generateRandomBase62(100);

        System.out.println("plain : " + plain);
        System.out.println("secure : " + secure);

        for (var signatureAlgorithm : DatSignatureAlgorithm.getEntries()) {
            for (var cryptoAlgorithm : DatCryptoAlgorithm.getEntries()) {
                DatCertificate cert = generate(signatureAlgorithm, cryptoAlgorithm);
                String tag = signatureAlgorithm.name() + "/" + cryptoAlgorithm.name();
                String dat = "";

                long time = System.currentTimeMillis();
                for (var i = 0; i < 10000; i++) {
                    dat = DatManager.issue(cert, plain, secure);
                }
                System.out.println(tag + " issue * 10000 : " + (System.currentTimeMillis() - time) + " ms");

                time = System.currentTimeMillis();
                for (var i = 0; i < 10000; i++) {
                    DatManager.parse(cert, dat);
                }
                System.out.println(tag + " parse * 10000 : " + (System.currentTimeMillis() - time) + " ms");
            }
        }

    }
}
