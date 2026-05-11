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
- [BenchTest.java](src/test/java/test/java/BenchTest.java)
- [BenchTest.kt](src/test/kotlin/test/kt/BenchTest.kt)
```
Plain : OzZK9TylEu8KY0JBNXevy3jWoDCAVMY8I7RzX657A0XyPeXU6vifhZhzh6K0wbDKGjkbU85e9M7J1MIwp72Fk4uKOvOyMj6fxPEk
Secure : FTnnTPJ4cz2JjoPgTODtib9yVoaKT9HOf3302z0HRdvJNstHh57GKh84CF6QddNVCyPo1UVo4gpHkBvqNFElt09Y9RlPamzrE367

Multi-Thread 
P256 AES128GCMN Issue * 10000 : 373ms
P256 AES128GCMN Parse * 10000 : 187ms
P256 AES256GCMN Issue * 10000 : 149ms
P256 AES256GCMN Parse * 10000 : 132ms
P384 AES128GCMN Issue * 10000 : 450ms
P384 AES128GCMN Parse * 10000 : 408ms
P384 AES256GCMN Issue * 10000 : 274ms
P384 AES256GCMN Parse * 10000 : 293ms
P521 AES128GCMN Issue * 10000 : 720ms
P521 AES128GCMN Parse * 10000 : 724ms
P521 AES256GCMN Issue * 10000 : 464ms
P521 AES256GCMN Parse * 10000 : 570ms

Single-Thread 
P256 AES128GCMN Issue * 10000 : 511ms
P256 AES128GCMN Parse * 10000 : 510ms
P256 AES256GCMN Issue * 10000 : 519ms
P256 AES256GCMN Parse * 10000 : 520ms
P384 AES128GCMN Issue * 10000 : 1396ms
P384 AES128GCMN Parse * 10000 : 1496ms
P384 AES256GCMN Issue * 10000 : 1344ms
P384 AES256GCMN Parse * 10000 : 1594ms
P521 AES128GCMN Issue * 10000 : 2855ms
P521 AES128GCMN Parse * 10000 : 3497ms
P521 AES256GCMN Issue * 10000 : 2834ms
P521 AES256GCMN Parse * 10000 : 3433ms
```
