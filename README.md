# DAT - Distributed Access Token

## Document

### [DAT Run Online](https://dat.saro.me)

### [What is DAT](https://dat.saro.me/--/intro)

### [Java / Kotlin Example](https://dat.saro.me/--/libs/maven-me.saro-dat)

## support signature algorithm
| name   | algorithm  |
|--------|------------|
| P256   | secp256r1  |
| P384   | secp384r1  |
| P521   | secp521r1  |

## support crypto algorithm
| name       | algorithm                   |
|------------|-----------------------------|
| AES128GCMN | aes-128-gcm n(nonce + body) |
| AES256GCMN | aes-256-cbc n(nonce + body) |


# Performance
- random plain and secure test
- mac mini m4 2024 basic (10 core)
- [DatPerformanceTest.java](src/test/java/test/java/DatPerformanceTest.java)
- [DatPerformanceTest.kt](src/test/kotlin/test/kt/DatPerformanceTest.kt)
```
plain : RvHFRtU7IuPtBHlhaaqoazTiuNYi3jkwOjiEu3CSJvSrG4AD1lsUwGPL3Rj3fYRSuVSY2yZsou6hStBpFaazeKuSVnJwAreiBXuV
secure : tKjPROuGcFYWYxItDXVB2UTbt34dLQ72W8Lxn3ifBzMWbOuJGg9Wj842p96tBwTt3FFYzrAU48cnoRLcG9a87gmu6C1786nl39Tl
P256/AES128GCMN toDat * 10000 : 649 ms
P256/AES128GCMN toPayload * 10000 : 544 ms
P256/AES256GCMN toDat * 10000 : 535 ms
P256/AES256GCMN toPayload * 10000 : 529 ms
P384/AES128GCMN toDat * 10000 : 1395 ms
P384/AES128GCMN toPayload * 10000 : 1481 ms
P384/AES256GCMN toDat * 10000 : 1367 ms
P384/AES256GCMN toPayload * 10000 : 1485 ms
P521/AES128GCMN toDat * 10000 : 2683 ms
P521/AES128GCMN toPayload * 10000 : 3362 ms
P521/AES256GCMN toDat * 10000 : 2691 ms
P521/AES256GCMN toPayload * 10000 : 3349 ms
```
