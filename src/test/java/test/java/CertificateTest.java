package test.java;

import me.saro.dat.DatUtils;
import me.saro.dat.crypto.DatCryptoAlgorithm;
import me.saro.dat.dat.DatCertificate;
import me.saro.dat.dat.DatManager;
import me.saro.dat.dat.Payload;
import me.saro.dat.signature.DatSignatureAlgorithm;
import me.saro.dat.signature.DatSignatureKeyExportOption;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CertificateTest {

    public void unit(DatCertificate failCert, DatSignatureAlgorithm signatureAlgorithm, DatCryptoAlgorithm cryptoAlgorithm) {
        String tag = "dat." + signatureAlgorithm.name() + "." + cryptoAlgorithm.name();

        String plain = DatUtils.generateRandomBase62(30);
        String secure = DatUtils.generateRandomBase62(30);

        long id = (long) (Math.random() * Long.MAX_VALUE);
        DatCertificate newCert = generate(id, signatureAlgorithm, cryptoAlgorithm);
        String newCertStr = newCert.exports(DatSignatureKeyExportOption.PAIR);

        DatCertificate readCert = DatCertificate.parse(newCertStr);

        String dat = DatManager.issue(newCert, plain, secure);
        String dat2 = DatManager.issue(newCert, plain.getBytes(StandardCharsets.UTF_8), secure.getBytes(StandardCharsets.UTF_8));
        System.out.println(tag + ": " + dat);

        Payload payload = DatManager.parse(readCert, dat);
        Payload payload2 = DatManager.parse(readCert, dat2);
        System.out.println(tag + ": " + payload.getPlain() + " / " + payload.getSecure());

        assert plain.equals(payload.getPlain());
        assert secure.equals(payload.getSecure());
        assert plain.equals(payload2.getPlain());
        assert secure.equals(payload2.getSecure());
        assert id == newCert.getCid();
        assert id == readCert.getCid();
        assertThrows(Exception.class, () -> DatManager.parse(failCert, dat));
    }


    public DatCertificate generate(long id, DatSignatureAlgorithm signatureAlgorithm, DatCryptoAlgorithm cryptoAlgorithm) {
        return DatCertificate.generate(
                id,
                signatureAlgorithm,
                cryptoAlgorithm,
                System.currentTimeMillis() - 10,
                System.currentTimeMillis() + 600,
                60
        );
    }

    @Test
    public void test() {
        var failCert = generate((long) (Math.random() * Long.MAX_VALUE),DatSignatureAlgorithm.P256, DatCryptoAlgorithm.AES128GCMN);

        for (var signatureAlgorithm : DatSignatureAlgorithm.getEntries()) {
            for (var cryptoAlgorithm : DatCryptoAlgorithm.getEntries()) {
                for (var i = 0; i < 20; i++) {
                    unit(failCert, signatureAlgorithm, cryptoAlgorithm);
                }
            }
        }

    }
}
