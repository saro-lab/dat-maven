package me.saro.dat.key.dat.kid

@FunctionalInterface
interface ToKid {
    fun toKid(kid: String): Kid
}