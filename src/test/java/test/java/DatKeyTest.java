package test.java;

import me.saro.dat.key.DatUtils;
import me.saro.dat.key.crypto.CryptoAlgorithm;
import me.saro.dat.key.dat.DatKey;
import me.saro.dat.key.dat.Payload;
import me.saro.dat.key.signature.SignatureAlgorithm;
import me.saro.dat.key.signature.SignatureKeyOutOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DatKeyTest {

    public void unit(DatKey failKey, SignatureAlgorithm signatureAlgorithm, CryptoAlgorithm cryptoAlgorithm) {
        String tag = "dat." + signatureAlgorithm.name() + "." + cryptoAlgorithm.name();

        String plain = DatUtils.generateRandomBase62(30);
        String secure = DatUtils.generateRandomBase62(30);

        DatKey newKey = generate(signatureAlgorithm, cryptoAlgorithm);
        String newKeyStr = newKey.exports(SignatureKeyOutOption.FULL);

        DatKey readKey = DatKey.parse(newKeyStr);

        String dat = newKey.toDat(plain, secure);
        System.out.println(tag + ": " + dat);

        Payload payload = readKey.toPayload(dat);
        System.out.println(tag + ": " + payload.getPlain() + " / " + payload.getSecure());

        assert plain.equals(payload.getPlain());
        assert secure.equals(payload.getSecure());
        assertThrows(Exception.class, () -> failKey.toPayload(dat));
    }


    public DatKey generate(SignatureAlgorithm signatureAlgorithm, CryptoAlgorithm cryptoAlgorithm) {
        return DatKey.generate(
                DatUtils.generateRandomBase62(10),
                signatureAlgorithm,
                cryptoAlgorithm,
                System.currentTimeMillis() - 10,
                System.currentTimeMillis() + 600,
                60
        );
    }

    @Test
    public void test() {
        var failKey = generate(SignatureAlgorithm.P256, CryptoAlgorithm.AES128GCMN);

        for (var signatureAlgorithm : SignatureAlgorithm.getEntries()) {
            for (var cryptoAlgorithm : CryptoAlgorithm.getEntries()) {
                for (var i = 0; i < 20; i++) {
                    unit(failKey, signatureAlgorithm, cryptoAlgorithm);
                }
            }
        }

    }
}
