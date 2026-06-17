package test.kt;

import me.saro.dat.dat.DatCmsManager;
import me.saro.dat.dat.Payload;
import org.junit.jupiter.api.Test;

public class ExampleCmsManagerTest {

    @Test
    public void useDatCms() {

        /*
        # server example
        TOKEN_MASTER="123456789012" \
        TOKEN_CERT_FULL="12345678901a,12345678901b" \
        TOKEN_CERT_VERIFY="12345678901C,12345678901D" \
        .\dat-cms
        */

        // singleton
        DatCmsManager manager = DatCmsManager.builder()
                .host("localhost")
                .port(8088)
                .token("12345678901b")
                .build();

        String plain = "Unicode 유니코드 ユニコード 万国码 يونيكود यूनिकोड Юникод 🦄💻";
        String secure = "Ciphertext 암호문 暗号文 密文 Шифротекст Texte chiffré Geheimtext نص مشفر सिफरपाठ 🔐";

        System.out.println("plain : " + plain);
        System.out.println("secure : " + secure);

        // issue dat
        String dat = manager.issue(plain, secure);
        System.out.println("dat : " + dat);

        // parse dat
        Payload payload = manager.parse(dat);

        String payloadPlain = payload.getPlain();
        String payloadSecure = payload.getSecure();

        System.out.println("payload plain : " + payloadPlain);
        System.out.println("payload secure : " + payloadSecure);

        assert plain.equals(payloadPlain);
        assert secure.equals(payloadSecure);
    }
}
