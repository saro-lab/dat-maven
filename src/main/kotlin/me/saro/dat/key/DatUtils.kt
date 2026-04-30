package me.saro.dat.key

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.util.*

class DatUtils {
    companion object {
        private val DE_BASE64_URL_WP: Base64.Decoder = Base64.getUrlDecoder()
        private val EN_BASE64_URL_WP: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()
        private val MOLD_BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
        private val ESCAPE_DAT_PATTERN = Regex("[.\r\n]");
        internal val BOUNCY_CASTLE_PROVIDER = BouncyCastleProvider().apply { Security.addProvider(this) }


        @JvmStatic
        fun encodeBase64UrlWp(bytes: ByteArray): String {
            if (bytes.isEmpty()) {
                return ""
            }
            return EN_BASE64_URL_WP.encodeToString(bytes)
        }

        @JvmStatic
        fun encodeBase64UrlWpBytes(bytes: ByteArray): ByteArray {
            if (bytes.isEmpty()) {
                return ByteArray(0)
            }
            return EN_BASE64_URL_WP.encode(bytes)
        }

        @JvmStatic
        fun decodeBase64UrlWp(str: String): ByteArray {
            if (str.isEmpty()) {
                return ByteArray(0)
            }
            return DE_BASE64_URL_WP.decode(str)
        }

        @JvmStatic
        fun generateRandomBase62(size: Int): String {
            return generateRandomString(size, MOLD_BASE62)
        }

        @JvmStatic
        fun generateRandomString(size: Int, mold: CharArray): String {
            if (size <= 0 || mold.isEmpty()) {
                throw IllegalArgumentException("invalid size or mold")
            }
            val moldLen = mold.size
            val rv = CharArray(size)
            for (i in rv.indices) {
                rv[i] = mold[(Math.random() * moldLen).toInt()]
            }
            return String(rv)
        }

        @JvmStatic
        fun hasEscapeDatChars(str: String): Boolean {
            return ESCAPE_DAT_PATTERN.containsMatchIn(str)
        }
    }
}