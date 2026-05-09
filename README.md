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
plain : nvB1ZhTRyxT2mxVpz5YHdJWwyNc9PnBq0zjgRsLxC8xaXeZ7IEvGRjSn73Z2kZaHAv6FlV8Dn577vSUf5vUEhbFiEbX5RIa56KTN
secure : NhmeDEFWjJXUIjHsdHCrHdZaLy2WKdF27SP1BijHOg820ejsSdheVq0GTcuT1sYIqZCombWi33oDZyGgXDKLfN6fEvOiLZnEpwjz
P256/AES128GCMN issue * 10000 : 586 ms
P256/AES128GCMN parse * 10000 : 530 ms
P256/AES256GCMN issue * 10000 : 518 ms
P256/AES256GCMN parse * 10000 : 552 ms
P384/AES128GCMN issue * 10000 : 1353 ms
P384/AES128GCMN parse * 10000 : 1502 ms
P384/AES256GCMN issue * 10000 : 1327 ms
P384/AES256GCMN parse * 10000 : 1509 ms
P521/AES128GCMN issue * 10000 : 2746 ms
P521/AES128GCMN parse * 10000 : 3523 ms
P521/AES256GCMN issue * 10000 : 2757 ms
P521/AES256GCMN parse * 10000 : 3494 ms
```
