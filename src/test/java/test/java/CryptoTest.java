package test.java;

import me.saro.dat.key.DatUtils;
import me.saro.dat.key.crypto.CryptoAlgorithm;
import me.saro.dat.key.crypto.CryptoKey;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CryptoTest {

    public void unit(CryptoAlgorithm alg) {
        byte[] body = DatUtils.generateRandomBase62(100).getBytes();
        CryptoKey cryptoKey = CryptoKey.generate(alg);
        CryptoKey cryptoKeyFail = CryptoKey.generate(alg);
        byte[] cryptoKeyBytes = cryptoKey.toBytes();
        CryptoKey cryptoKeyFrom = CryptoKey.fromBytes(alg, cryptoKeyBytes);

        var encrypted = cryptoKeyFrom.encrypt(body);

        assert Arrays.equals(cryptoKey.decrypt(encrypted), body);
        assert Arrays.equals(cryptoKeyFrom.decrypt(encrypted), body);
        assertThrows(Exception.class, () -> cryptoKeyFail.decrypt(encrypted));
    }

    @Test
    public void test() {
        for (var alg : CryptoAlgorithm.getEntries()) {
            System.out.println("crypto test - " + alg.name());
            for (var i = 0; i < 20; i++) {
                unit(alg);
            }
        }
    }
}
