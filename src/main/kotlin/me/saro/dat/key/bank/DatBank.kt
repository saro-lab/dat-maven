package me.saro.dat.key.bank

import me.saro.dat.key.dat.DatKey
import me.saro.dat.key.dat.Payload
import me.saro.dat.key.dat.kid.Kid
import me.saro.dat.key.dat.kid.ToKid
import me.saro.dat.key.exception.DatException
import me.saro.dat.key.signature.SignatureKeyOutOption
import java.util.stream.Collectors

class DatBank(
    val toKid: ToKid,
    private var paddingKeys: List<DatKey> = emptyList(),
    private var issueKey: DatKey? = null,
    private var verifyKeys: List<DatKey> = emptyList(),
) {
    constructor(toKid: ToKid) : this(toKid, emptyList(), null, emptyList())

    private val lock = Any()
    private val paddingKeysLock = Any()

    fun toDat(plain: String, secure: String): String {
        if (issueKey != null) {
            return issueKey!!.toDat(plain, secure)
        } else {
            throw DatException("IssueKeyIsNull")
        }
    }

    fun toPayload(dat: String): Payload {
        val split = DatKey.split(dat)
        val kid = toKid.toKid(split[1])
        return findDatKey(kid).toPayload(dat, split, kid)
    }

    fun toPayloadWithoutVerify(dat: String): Payload {
        val split = DatKey.split(dat)
        return findDatKey(toKid.toKid(split[1])).toPayloadWithoutVerifying(split)
    }

    internal fun findDatKey(kid: Kid): DatKey {
        val key = synchronized(lock) {
            verifyKeys.find { it.kid == kid }
        }
        if (key == null) {
            throw DatException("KidNotFound")
        }
        return key
    }

    fun exportkids(): List<String> {
        return synchronized(lock) {
            verifyKeys.map { it.kid.toString() }
        }
    }

    fun exportkeys(): List<DatKey> {
        return synchronized(lock) {
            verifyKeys.map { it.clone() }
        }
    }

    fun exportKeysFormat(signatureKeyOutOption: SignatureKeyOutOption): String {
        return synchronized(lock) {
            verifyKeys.map { it.format(signatureKeyOutOption) }
        }.joinToString("\n")
    }

    fun importKeysFormat(format: String, clear: Boolean) {
        val list = if (format.isNotBlank()) {
            format.split("\n")
                .stream()
                .filter { it.isNotBlank() }
                .map { DatKey.parse(it, toKid) }
                .toList()
        } else {
            listOf()
        }
        importKeys(list, clear)
    }

    fun importKeys(newKeys: List<DatKey>, clear: Boolean) {
        val list = synchronized(paddingKeysLock) {
            val list = if (clear) { mutableListOf<DatKey>() } else { paddingKeys.toMutableList() }
            for (key in newKeys) {
                if (!list.contains(key)) {
                    list.add(key)
                }
            }
            paddingKeys = list.stream()
                .filter { !it.expired() }
                .sorted(Comparator.comparingLong { it.issueBegin })
                .collect(Collectors.toList())

            paddingKeys.map { it.clone() }
        }

        val now = System.currentTimeMillis() / 1000L

        synchronized(lock) {
            issueKey = list.findLast { it.issueBegin <= now && it.issueEnd > now }
            verifyKeys = list
        }
    }
}
