# DAT - Distributed Access Token

## Document

### [DAT Run Online](https://dat.saro.me)

### [What is DAT](https://dat.saro.me/intro)

### [Java / Kotlin Example](https://dat.saro.me/libs/maven-me.saro-dat)


## Support algorithm
### Signature
| name            | note                  |
|-----------------|-----------------------|
| ECDSA-P256      | = secp256r1           |
| ECDSA-P384      | = secp384r1           |
| ECDSA-P521      | = secp521r1           |
| HMAC-SHA256-MFS | = 256Bit Fixed Secret |
| HMAC-SHA384-MFS | = 384Bit Fixed Secret |
| HMAC-SHA512-MFS | = 512Bit Fixed Secret |
- MFS : Maximum(Same Bit) Fixed Secret

### Crypto
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
Plain : nM5XpjUWD4IIeGIXN1f5oQZ9ar8wJ3n1z6KeRtzdakjvcIZoQgztGou1slXyjfz7xbJNEatLnYuTNDvfzejxUxdZ7xxX9O2KuxFX
Secure : JMo0tvdg9CKeAMtoX1br2rabS0hNNx9GBnEG59Fkoll6vipHSeKGeIivGlbwLByX1x9Y24qa3iu6MLNIgQkydL5NJVxkgOuBAuvl

Multi-Thread 
HMAC-SHA256-MFS IV-AES128-GCM Issue * 10000 : 145ms
HMAC-SHA256-MFS IV-AES128-GCM Parse * 10000 : 52ms
HMAC-SHA256-MFS IV-AES256-GCM Issue * 10000 : 50ms
HMAC-SHA256-MFS IV-AES256-GCM Parse * 10000 : 44ms
HMAC-SHA384-MFS IV-AES128-GCM Issue * 10000 : 114ms
HMAC-SHA384-MFS IV-AES128-GCM Parse * 10000 : 47ms
HMAC-SHA384-MFS IV-AES256-GCM Issue * 10000 : 45ms
HMAC-SHA384-MFS IV-AES256-GCM Parse * 10000 : 35ms
HMAC-SHA512-MFS IV-AES128-GCM Issue * 10000 : 73ms
HMAC-SHA512-MFS IV-AES128-GCM Parse * 10000 : 30ms
HMAC-SHA512-MFS IV-AES256-GCM Issue * 10000 : 37ms
HMAC-SHA512-MFS IV-AES256-GCM Parse * 10000 : 26ms
ECDSA-P256 IV-AES128-GCM Issue * 10000 : 298ms
ECDSA-P256 IV-AES128-GCM Parse * 10000 : 194ms
ECDSA-P256 IV-AES256-GCM Issue * 10000 : 181ms
ECDSA-P256 IV-AES256-GCM Parse * 10000 : 123ms
ECDSA-P384 IV-AES128-GCM Issue * 10000 : 546ms
ECDSA-P384 IV-AES128-GCM Parse * 10000 : 423ms
ECDSA-P384 IV-AES256-GCM Issue * 10000 : 268ms
ECDSA-P384 IV-AES256-GCM Parse * 10000 : 268ms
ECDSA-P521 IV-AES128-GCM Issue * 10000 : 716ms
ECDSA-P521 IV-AES128-GCM Parse * 10000 : 680ms
ECDSA-P521 IV-AES256-GCM Issue * 10000 : 456ms
ECDSA-P521 IV-AES256-GCM Parse * 10000 : 571ms

Single-Thread 
HMAC-SHA256-MFS IV-AES128-GCM Issue * 10000 : 50ms
HMAC-SHA256-MFS IV-AES128-GCM Parse * 10000 : 48ms
HMAC-SHA256-MFS IV-AES256-GCM Issue * 10000 : 53ms
HMAC-SHA256-MFS IV-AES256-GCM Parse * 10000 : 51ms
HMAC-SHA384-MFS IV-AES128-GCM Issue * 10000 : 49ms
HMAC-SHA384-MFS IV-AES128-GCM Parse * 10000 : 48ms
HMAC-SHA384-MFS IV-AES256-GCM Issue * 10000 : 51ms
HMAC-SHA384-MFS IV-AES256-GCM Parse * 10000 : 49ms
HMAC-SHA512-MFS IV-AES128-GCM Issue * 10000 : 50ms
HMAC-SHA512-MFS IV-AES128-GCM Parse * 10000 : 48ms
HMAC-SHA512-MFS IV-AES256-GCM Issue * 10000 : 51ms
HMAC-SHA512-MFS IV-AES256-GCM Parse * 10000 : 51ms
ECDSA-P256 IV-AES128-GCM Issue * 10000 : 517ms
ECDSA-P256 IV-AES128-GCM Parse * 10000 : 510ms
ECDSA-P256 IV-AES256-GCM Issue * 10000 : 503ms
ECDSA-P256 IV-AES256-GCM Parse * 10000 : 512ms
ECDSA-P384 IV-AES128-GCM Issue * 10000 : 1324ms
ECDSA-P384 IV-AES128-GCM Parse * 10000 : 1487ms
ECDSA-P384 IV-AES256-GCM Issue * 10000 : 1331ms
ECDSA-P384 IV-AES256-GCM Parse * 10000 : 1488ms
ECDSA-P521 IV-AES128-GCM Issue * 10000 : 2709ms
ECDSA-P521 IV-AES128-GCM Parse * 10000 : 3351ms
ECDSA-P521 IV-AES256-GCM Issue * 10000 : 2723ms
ECDSA-P521 IV-AES256-GCM Parse * 10000 : 3339ms
```
