package me.saro.dat.key

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.util.*

class DatUtils {
    companion object {
        private val DE_BASE64_URL: Base64.Decoder = Base64.getUrlDecoder()
        private val EN_BASE64_URL: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()
        private val MOLD_BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
        private val KID_PATTERN = Regex("^[A-Z0-9\\-_]*$", RegexOption.IGNORE_CASE)
        private val BOUNCY_CASTLE_PROVIDER: Int = Security.addProvider(BouncyCastleProvider())


        @JvmStatic
        fun encodeBase64Url(bytes: ByteArray): String {
            if (bytes.isEmpty()) {
                return ""
            }
            return EN_BASE64_URL.encodeToString(bytes)
        }

        @JvmStatic
        fun encodeBase64UrlBytes(bytes: ByteArray): ByteArray {
            if (bytes.isEmpty()) {
                return ByteArray(0)
            }
            return EN_BASE64_URL.encode(bytes)
        }

        @JvmStatic
        fun decodeBase64Url(str: String): ByteArray {
            if (str.isEmpty()) {
                return ByteArray(0)
            }
            return DE_BASE64_URL.decode(str)
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
        fun isKidFormat(str: String): Boolean {
            return KID_PATTERN.containsMatchIn(str)
        }
    }
}