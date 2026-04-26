package me.saro.dat.key.crypto

enum class CryptoAlgorithm {
    AES128GCMN,
    AES256GCMN,
    ;
    fun getKeySize(): Int {
        return when (this) {
            AES128GCMN -> 16
            AES256GCMN -> 32
        }
    }
}
