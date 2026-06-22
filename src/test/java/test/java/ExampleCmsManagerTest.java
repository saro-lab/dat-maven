package test.java;

import me.saro.dat.dat.DatCmsManager;
import me.saro.dat.dat.Payload;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExampleCmsManagerTest {

    public void useDatCms() throws IOException, InterruptedException {

        // singleton
        DatCmsManager manager = DatCmsManager.builder()
                .uri("http://localhost:8088")
                .intervalSeconds(1)
                .token("12345678901b")
                .build();

        String plain = "Unicode 유니코드 ユニコード 万国码 يونيكود यूनिकोड Юникод 🦄💻";
        String secure = "Ciphertext 암호문 暗号文 密文 Шифротекст Texte chiffré Geheimtext نص مشفر सिफरपाठ 🔐";

        System.out.println("plain : " + plain);
        System.out.println("secure : " + secure);

        // issue dat
        String dat = manager.issue(plain, secure).getOrThrow();
        System.out.println("dat : " + dat);

        // parse dat
        Payload payload = manager.parse(dat).getOrThrow();

        String payloadPlain = payload.getPlain();
        String payloadSecure = payload.getSecure();

        System.out.println("payload plain : " + payloadPlain);
        System.out.println("payload secure : " + payloadSecure);

        assert plain.equals(payloadPlain);
        assert secure.equals(payloadSecure);

        // wait
        Thread.sleep(5000);
    }

    @Test
    public void test() throws IOException, InterruptedException {
        try {
            useDatCms();
        } catch (Exception e) {
            System.out.println("Ignore: is soft test: real connection test");
        }
    }
}
