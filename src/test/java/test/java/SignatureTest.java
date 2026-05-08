package test.java;

import me.saro.dat.key.DatUtils;
import me.saro.dat.key.signature.SignatureAlgorithm;
import me.saro.dat.key.signature.SignatureKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureTest {

    public void unit(SignatureAlgorithm alg) {
        byte[] body = DatUtils.generateRandomBase62(100).getBytes();
        SignatureKey signatureKey = SignatureKey.generate(alg);
        SignatureKey signatureKeyFail = SignatureKey.generate(alg);
        byte[] signingKeyBytes = signatureKey.getSigningKeyBytes();
        byte[] verifyingKeyBytes = signatureKey.getVerifyingKeyBytes();
        SignatureKey signatureKeyFrom = SignatureKey.fromBytes(alg, signingKeyBytes, verifyingKeyBytes);
        SignatureKey verifyKeyFrom = SignatureKey.fromBytes(alg, null, verifyingKeyBytes);

        var sign = signatureKeyFrom.sign(body);

        assertTrue(signatureKey.verify(body, sign));
        assertTrue(signatureKeyFrom.verify(body, sign));
        assertTrue(verifyKeyFrom.verify(body, sign));
        assertFalse(signatureKeyFail.verify(body, sign));
    }

    @Test
    public void test() {
        for (var alg : SignatureAlgorithm.getEntries()) {
            System.out.println("sign test - " + alg.name());
            for (var i = 0; i < 20; i++) {
                unit(alg);
            }
        }
    }
}
