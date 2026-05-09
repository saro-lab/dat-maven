package test.java;

import me.saro.dat.DatUtils;
import me.saro.dat.crypto.DatCryptoAlgorithm;
import me.saro.dat.crypto.DatCryptoKey;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CryptoTest {

    public void unit(DatCryptoAlgorithm alg) {
        byte[] body = DatUtils.generateRandomBase62(100).getBytes();
        DatCryptoKey cryptoKey = DatCryptoKey.generate(alg);
        DatCryptoKey cryptoKeyFail = DatCryptoKey.generate(alg);
        byte[] cryptoKeyBytes = cryptoKey.toBytes();
        DatCryptoKey cryptoKeyFrom = DatCryptoKey.fromBytes(alg, cryptoKeyBytes);

        var encrypted = cryptoKeyFrom.encrypt(body);

        assert Arrays.equals(cryptoKey.decrypt(encrypted), body);
        assert Arrays.equals(cryptoKeyFrom.decrypt(encrypted), body);
        assertThrows(Exception.class, () -> cryptoKeyFail.decrypt(encrypted));
    }

    @Test
    public void test() {
        for (var algorithm : DatCryptoAlgorithm.getEntries()) {
            System.out.println("crypto test - " + algorithm.name());
            for (var i = 0; i < 20; i++) {
                unit(algorithm);
            }
        }
    }
}
