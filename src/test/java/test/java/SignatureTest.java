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
        byte[] signingKeyBytes = signatureKey.getSigningKeyBytes();
        byte[] verifyingKeyBytes = signatureKey.getVerifyingKeyBytes();
        DatSignatureKey signatureKeyFrom = DatSignatureKey.fromBytes(alg, signingKeyBytes, verifyingKeyBytes);
        DatSignatureKey verifyKeyFrom = DatSignatureKey.fromBytes(alg, null, verifyingKeyBytes);

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
