# DAT - Data Access Token

# gradle
```
implementation("me.saro:dat:1.0.1")
```
# maven
```
<dependency>
    <groupId>me.saro</groupId>
    <artifactId>dat</artifactId>
    <version>1.0.1</version>
</dependency>
```

# DAT
```
# Example
signature_algorithm: P256
crypto_algorithm: AES128GCMN
plain: 123
secure: asdf

# DAT result Example
1776530737.11.MTIz.8yKUvzs7mg3tDwdeA9I2gNOliewpTgm9OVbEY3Qh6io.qfnqmXKuNE3MfRr576rxNMCchxbY1iqC07-woJcbCudt2O0BAyK_86ypaSfLJjkGq9FZxpGrsgBDkk-xQhGvmA
```

> ```expire```.```kid```.```plain```.```secure```.```sign```

- ```expire```: number
    - Unix-Timestamp (sec)
- ```kid```: stringifiable
    - key id
- ```plain```: base64 url no pad
    - Text Data
- ```secure```: base64 url no pad
    - Encrypted Text Data
- ```sign```: base64 url no pad
    - dat-bank\[kid\].sign(expire.kid.plain.secure)

# DAT KEY
```
# Example
1.2.P256.DErFl-U5h4fdbnAXTTs2GikkJgZwYXV25v2EdFeXIXs.AES128GCMN.5VEziIzCu2LRsK1XS6OYxA.1776541326.1776544626.1800
```
> ```version```.```kid```.```signature-algorithm```.```signature-key```.```crypto-algorithm```.```crypto-key```.```issue-begin```.```issue-end```.```token-ttl```

- ```version```: number
    - dat-key format version
- ```kid```: stringifiable
    - key id
- ```signature-algorithm```: text
    - sign algorithm
- ```signature-key```: base64 url no pad
  > The signature-key is categorized into three types: FULL (signing-key~verifying-key), SIGNING (signing-key), and VERIFYING (~verifying-key).
  >
  > Whether a key is for sign or verify can be distinguished by the presence of a leading tilde (~). Generally, if you output the sign key alone, the public key can be derived from it (using the private key). However, depending on the platform, this derivation feature may not be available; in such cases, you should output the full key and parse it for use.
    - FULL: \<signing key base64\>~\<verifying key base64\>
    - SIGNING: \<signing key base64\>
    - VERIFYING: ~\<verifying key base64\>
- ```crypto-algorithm```: text
    - crypto algorithm,
- ```crypto-key```: base64 url no pad
    - crypto key
- ```issue-begin```: number
    - issue begin time
- ```issue-end```: number
    - issue end time
- ```token-ttl```: number
    - token(dat) TTL


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

## support dat key format version
| support key format version | maven version |
|----------------------------|---------------|
| 2                          | 1.0.0 +       |
| 1                          | BETA          |


### See Also
- Libraries
    - [Rust](https://github.com/saro-lab/dat-rust)
    - [Java, Kotlin](https://github.com/saro-lab/dat-maven)
- DatKey Generate Service
    - [DAT Bank: Binary Docker, Kubernetes](https://github.com/saro-lab/dat-bank)

### Use Cases
- Anissia (https://anissia.net)
    - BackEnd: https://github.com/anissia-net/core
    - FrontEnd: https://github.com/anissia-net/web
