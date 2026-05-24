package test.java;

import me.saro.dat.DatUtils;
import me.saro.dat.signature.DatSignatureAlgorithm;
import me.saro.dat.signature.DatSignatureKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureTest {

    public void unit(DatSignatureAlgorithm alg) {
        byte[] body = DatUtils.generateRandomBase62(100).getBytes();
        DatSignatureKey signatureKey = DatSignatureKey.generate(alg);
        DatSignatureKey signatureKeyFail = DatSignatureKey.generate(alg);
        byte[] keyBytes = signatureKey.exportKey(false);
        byte[] verifyingKeyBytes = signatureKey.exportKey(true);
        DatSignatureKey signatureKeyFrom = DatSignatureKey.fromKey(alg, keyBytes);
        DatSignatureKey verifyKeyFrom = DatSignatureKey.fromKey(alg, verifyingKeyBytes);

        var sign = signatureKeyFrom.sign(body);

        assertTrue(signatureKey.verify(body, sign));
        assertTrue(signatureKeyFrom.verify(body, sign));
        assertTrue(verifyKeyFrom.verify(body, sign));
        assertFalse(signatureKeyFail.verify(body, sign));
    }

    @Test
    public void test() {
        for (var algorithm : DatSignatureAlgorithm.getEntries()) {
            System.out.println("sign test - " + algorithm.name());
            for (var i = 0; i < 20; i++) {
                unit(algorithm);
            }
        }
    }
}
