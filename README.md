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
Plain : k2wzENx0zqCVeMMRL9J4pPiFT8zSglUrygnYsQLo6sdjaupY6U9O3WuABukGwW38dvO68xERsdJk2D3cQDJbCPtNp5LHnNfIKFSl
Secure : UemGfRODZqFyDGEcMGtWbtDbMgcvHiKHfhZCkvIqanz7SDKMMU63boTxWjAGBefIWmliFXV6E6eB5Cj4ct3Jk2jczR6rFiOr8NcB

Multi-Thread 
HMAC-SHA256-MFS IV-AES128-GCM Issue * 10000 : 144ms
HMAC-SHA256-MFS IV-AES128-GCM Parse * 10000 : 51ms
HMAC-SHA256-MFS IV-AES256-GCM Issue * 10000 : 47ms
HMAC-SHA256-MFS IV-AES256-GCM Parse * 10000 : 40ms
HMAC-SHA384-MFS IV-AES128-GCM Issue * 10000 : 104ms
HMAC-SHA384-MFS IV-AES128-GCM Parse * 10000 : 52ms
HMAC-SHA384-MFS IV-AES256-GCM Issue * 10000 : 50ms
HMAC-SHA384-MFS IV-AES256-GCM Parse * 10000 : 48ms
HMAC-SHA512-MFS IV-AES128-GCM Issue * 10000 : 80ms
HMAC-SHA512-MFS IV-AES128-GCM Parse * 10000 : 57ms
HMAC-SHA512-MFS IV-AES256-GCM Issue * 10000 : 31ms
HMAC-SHA512-MFS IV-AES256-GCM Parse * 10000 : 19ms
ECDSA-P256 IV-AES128-GCM Issue * 10000 : 296ms
ECDSA-P256 IV-AES128-GCM Parse * 10000 : 185ms
ECDSA-P256 IV-AES256-GCM Issue * 10000 : 159ms
ECDSA-P256 IV-AES256-GCM Parse * 10000 : 126ms
ECDSA-P384 IV-AES128-GCM Issue * 10000 : 481ms
ECDSA-P384 IV-AES128-GCM Parse * 10000 : 467ms
ECDSA-P384 IV-AES256-GCM Issue * 10000 : 285ms
ECDSA-P384 IV-AES256-GCM Parse * 10000 : 271ms
ECDSA-P521 IV-AES128-GCM Issue * 10000 : 829ms
ECDSA-P521 IV-AES128-GCM Parse * 10000 : 751ms
ECDSA-P521 IV-AES256-GCM Issue * 10000 : 505ms
ECDSA-P521 IV-AES256-GCM Parse * 10000 : 591ms

Single-Thread 
HMAC-SHA256-MFS IV-AES128-GCM Issue * 10000 : 53ms
HMAC-SHA256-MFS IV-AES128-GCM Parse * 10000 : 46ms
HMAC-SHA256-MFS IV-AES256-GCM Issue * 10000 : 51ms
HMAC-SHA256-MFS IV-AES256-GCM Parse * 10000 : 51ms
HMAC-SHA384-MFS IV-AES128-GCM Issue * 10000 : 51ms
HMAC-SHA384-MFS IV-AES128-GCM Parse * 10000 : 48ms
HMAC-SHA384-MFS IV-AES256-GCM Issue * 10000 : 54ms
HMAC-SHA384-MFS IV-AES256-GCM Parse * 10000 : 50ms
HMAC-SHA512-MFS IV-AES128-GCM Issue * 10000 : 50ms
HMAC-SHA512-MFS IV-AES128-GCM Parse * 10000 : 49ms
HMAC-SHA512-MFS IV-AES256-GCM Issue * 10000 : 53ms
HMAC-SHA512-MFS IV-AES256-GCM Parse * 10000 : 50ms
ECDSA-P256 IV-AES128-GCM Issue * 10000 : 552ms
ECDSA-P256 IV-AES128-GCM Parse * 10000 : 506ms
ECDSA-P256 IV-AES256-GCM Issue * 10000 : 546ms
ECDSA-P256 IV-AES256-GCM Parse * 10000 : 522ms
ECDSA-P384 IV-AES128-GCM Issue * 10000 : 1370ms
ECDSA-P384 IV-AES128-GCM Parse * 10000 : 1514ms
ECDSA-P384 IV-AES256-GCM Issue * 10000 : 1350ms
ECDSA-P384 IV-AES256-GCM Parse * 10000 : 1496ms
ECDSA-P521 IV-AES128-GCM Issue * 10000 : 2731ms
ECDSA-P521 IV-AES128-GCM Parse * 10000 : 3364ms
ECDSA-P521 IV-AES256-GCM Issue * 10000 : 2732ms
ECDSA-P521 IV-AES256-GCM Parse * 10000 : 3370ms
```
