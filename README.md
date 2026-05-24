# DAT - Distributed Access Token

## Document

### [DAT Run Online](https://dat.saro.me)

### [What is DAT](https://dat.saro.me/--/intro)

### [Java / Kotlin Example](https://dat.saro.me/--/libs/maven-me.saro-dat)


## Support
### Signature algorithm
| name            | note                  |
|-----------------|-----------------------|
| ECDSA-P256      | = secp256r1           |
| ECDSA-P384      | = secp384r1           |
| ECDSA-P521      | = secp521r1           |
| HMAC-SHA256-MFS | = 256Bit Fixed Secret |
| HMAC-SHA384-MFS | = 384Bit Fixed Secret |
| HMAC-SHA512-MFS | = 512Bit Fixed Secret |
- MFS : Maximum(Same Bit) Fixed Secret

### Crypto algorithm
| name       | note                          |
|------------|-------------------------------|
| IV-AES128-GCM | (IV=NONCE:96BIT) + AES128 GCM |
| IV-AES256-GCM | (IV=NONCE:96BIT) + AES256 GCM |

# Performance
- random plain and secure test
- mac mini m4 2024 basic (10 core)
- [BenchTest.java](src/test/java/test/java/BenchTest.java)
- [BenchTest.kt](src/test/kotlin/test/kt/BenchTest.kt)
```
Plain : C6MLbvob9PxOzpx6h6Ev0ln8Nk37ZevOIOKEkQgMylfCgTDbwywp0QyBoYi8doPt0zv9L5wQjAkzhRdvVYS6lU2TRpY57SANEDSL
Secure : zYXLJ4pdPqj7uEy0kbwih5326wVjJd98KJKMFr9GoCfeR7Eb22fflWPng0oQUR9nyZ1UvXLdCjtx926WlHh1aAmwIkuZVumxWZK5

Multi-Thread 
HMAC-SHA256-MFS IV-AES128-GCM Issue * 10000 : 131ms
HMAC-SHA256-MFS IV-AES128-GCM Parse * 10000 : 66ms
HMAC-SHA256-MFS IV-AES256-GCM Issue * 10000 : 45ms
HMAC-SHA256-MFS IV-AES256-GCM Parse * 10000 : 45ms
HMAC-SHA384-MFS IV-AES128-GCM Issue * 10000 : 75ms
HMAC-SHA384-MFS IV-AES128-GCM Parse * 10000 : 58ms
HMAC-SHA384-MFS IV-AES256-GCM Issue * 10000 : 64ms
HMAC-SHA384-MFS IV-AES256-GCM Parse * 10000 : 60ms
HMAC-SHA512-MFS IV-AES128-GCM Issue * 10000 : 80ms
HMAC-SHA512-MFS IV-AES128-GCM Parse * 10000 : 62ms
HMAC-SHA512-MFS IV-AES256-GCM Issue * 10000 : 60ms
HMAC-SHA512-MFS IV-AES256-GCM Parse * 10000 : 63ms
ECDSA-P256 IV-AES128-GCM Issue * 10000 : 287ms
ECDSA-P256 IV-AES128-GCM Parse * 10000 : 188ms
ECDSA-P256 IV-AES256-GCM Issue * 10000 : 143ms
ECDSA-P256 IV-AES256-GCM Parse * 10000 : 125ms
ECDSA-P384 IV-AES128-GCM Issue * 10000 : 646ms
ECDSA-P384 IV-AES128-GCM Parse * 10000 : 461ms
ECDSA-P384 IV-AES256-GCM Issue * 10000 : 334ms
ECDSA-P384 IV-AES256-GCM Parse * 10000 : 421ms
ECDSA-P521 IV-AES128-GCM Issue * 10000 : 1106ms
ECDSA-P521 IV-AES128-GCM Parse * 10000 : 780ms
ECDSA-P521 IV-AES256-GCM Issue * 10000 : 505ms
ECDSA-P521 IV-AES256-GCM Parse * 10000 : 582ms

Single-Thread 
HMAC-SHA256-MFS IV-AES128-GCM Issue * 10000 : 51ms
HMAC-SHA256-MFS IV-AES128-GCM Parse * 10000 : 52ms
HMAC-SHA256-MFS IV-AES256-GCM Issue * 10000 : 57ms
HMAC-SHA256-MFS IV-AES256-GCM Parse * 10000 : 53ms
HMAC-SHA384-MFS IV-AES128-GCM Issue * 10000 : 51ms
HMAC-SHA384-MFS IV-AES128-GCM Parse * 10000 : 48ms
HMAC-SHA384-MFS IV-AES256-GCM Issue * 10000 : 53ms
HMAC-SHA384-MFS IV-AES256-GCM Parse * 10000 : 51ms
HMAC-SHA512-MFS IV-AES128-GCM Issue * 10000 : 58ms
HMAC-SHA512-MFS IV-AES128-GCM Parse * 10000 : 51ms
HMAC-SHA512-MFS IV-AES256-GCM Issue * 10000 : 52ms
HMAC-SHA512-MFS IV-AES256-GCM Parse * 10000 : 51ms
ECDSA-P256 IV-AES128-GCM Issue * 10000 : 513ms
ECDSA-P256 IV-AES128-GCM Parse * 10000 : 511ms
ECDSA-P256 IV-AES256-GCM Issue * 10000 : 509ms
ECDSA-P256 IV-AES256-GCM Parse * 10000 : 513ms
ECDSA-P384 IV-AES128-GCM Issue * 10000 : 1373ms
ECDSA-P384 IV-AES128-GCM Parse * 10000 : 1505ms
ECDSA-P384 IV-AES256-GCM Issue * 10000 : 1353ms
ECDSA-P384 IV-AES256-GCM Parse * 10000 : 1520ms
ECDSA-P521 IV-AES128-GCM Issue * 10000 : 2688ms
ECDSA-P521 IV-AES128-GCM Parse * 10000 : 3361ms
ECDSA-P521 IV-AES256-GCM Issue * 10000 : 2693ms
ECDSA-P521 IV-AES256-GCM Parse * 10000 : 3374ms
```
