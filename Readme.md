# Keystore Demo

This repository contains code to demonstrate saving/loading of a secret key from a keystore with inconsistent/incompatible 
versions of Java.


## Summary
When you use the accompanying code to:
* generate a new secret key and `save` it inside a keystore and print it on screen
* `load` the same key from the keystore and print it on screen

Then the following holds true:
* `save` followed by `load` on Oracle JDK 1.8.0_301-b09 works 
* `save` followed by `load` on OpenJDK 1.8.0_302-b08 works 
* `save` on OpenJDK 1.8.0_302-b08 followed by `load` on Oracle JDK 1.8.0_301-b09 works
* `save` on Oracle JDK 1.8.0_301-b09 followed by `load` on OpenJDK 1.8.0_302-b08 fails

The following steps reproduce the above scenario.

## Steps to reproduce

### Clone and build code
```shell
git clone git@github.com:tushar-hv/keystore-demo.git
cd keystore-demo
./gradlew clean build shadowJar
```

### Prepare Oracle JDK
Download Oracle's JDK from https://www.oracle.com/java/technologies/downloads/#java8.
Download the file `jdk-8u301-linux-x64.tar.gz`. Downloading this will require login. As of this writing, this is the version `1.8.0_301-b09`. 

Verify checksum:
```shell
md5sum jdk-8u301-linux-x64.tar.gz
# Output must be:
# e77f9ea4c9ad849960ca295684ff9143  jdk-8u301-linux-x64.tar.gz 
```

Extract:
```shell
tar -xzf jdk-8u301-linux-x64.tar.gz -C /tmp/
```
---

All the following commands are executed from the `keystore-demo` directory. Open multiple simultaneous terminals for the two containers.

```shell
cd keystore-demo
```

Make sure the keystore is initially empty:

```shell
keytool -v -list  -keystore keystore -storepass waterlinedata
```
Output:
```shell
Keystore type: PKCS12
Keystore provider: SUN

Your keystore contains 0 entries
```

If not, delete the existing entry:
```shell
keytool  -keystore keystore -delete -alias ldc-encryption-key -storepass waterlinedata
```


### Test 1:`save` followed by `load` on Oracle JDK 1.8.0_301-b09

Launch `debian:buster-slim` container with Oracle JDK mount:
```shell
docker run --rm -it -v /tmp/jdk1.8.0_301:/jdk1.8.0_301 -v $PWD:/keystore-demo  -w /keystore-demo debian:buster-slim bash
```
Run `save` command inside the container:
```shell
# Execute this inside the container:
/jdk1.8.0_301/bin/java -jar build/libs/keystore-demo-0.1.0-all.jar save
```


Output:
```shell
Runtime Name: Java(TM) SE Runtime Environment
Runtime Version: 1.8.0_301-b09
VM Vendor: Oracle Corporation
VM Version: 25.301-b09

Generating a new secret key...
Saving secret key to keystore ...
encodedKey = uXDNnsCNzC5SINIfhjI1pNtM8KulPHfkeyxaJkZOJwE=

```
Run `load` command inside the container:

```shell
# Execute this inside the container:
/jdk1.8.0_301/bin/java -jar build/libs/keystore-demo-0.1.0-all.jar load
```

Output:
```shell

Runtime Name: Java(TM) SE Runtime Environment
Runtime Version: 1.8.0_301-b09
VM Vendor: Oracle Corporation
VM Version: 25.301-b09

Loading secret key from keystore ...
encodedKey = uXDNnsCNzC5SINIfhjI1pNtM8KulPHfkeyxaJkZOJwE=
```
**Result**: Works!

Keep this container running for later.

---

### Test 2: `save` followed by `load` on OpenJDK 1.8.0_302-b08
On a new terminal on your **host**, launch `openjdk:8-slim-buster` container:
```shell
docker run --rm -it -v $PWD:/keystore-demo  -w /keystore-demo openjdk:8-slim-buster bash
```

Run `save` command inside the container:
```shell
# Execute this inside the container:
java -jar build/libs/keystore-demo-0.1.0-all.jar save
```

Output:
```shell

Runtime Name: OpenJDK Runtime Environment
Runtime Version: 1.8.0_302-b08
VM Vendor: Oracle Corporation
VM Version: 25.302-b08

Generating a new secret key...
Saving secret key to keystore ...
encodedKey = cNQ1N2SlZbHUu54ZNFF6pmhdzIXbvclei4iPGXOee2A=
```

Run `load` command inside the container:
```shell
# Execute this inside the container:
java -jar build/libs/keystore-demo-0.1.0-all.jar load
```

Output:
```shell

Runtime Name: OpenJDK Runtime Environment
Runtime Version: 1.8.0_302-b08
VM Vendor: Oracle Corporation
VM Version: 25.302-b08

Loading secret key from keystore ...
encodedKey = cNQ1N2SlZbHUu54ZNFF6pmhdzIXbvclei4iPGXOee2A=
```
**Result**: Works!

---
### Test 3: `save` on OpenJDK 1.8.0_302-b08 followed by `load` on Oracle JDK 1.8.0_301-b09

On OpenJDK container, run `save` command:
```shell
# Execute this inside the OpenJDK container:
java -jar build/libs/keystore-demo-0.1.0-all.jar save
```

Output:
```shell

Runtime Name: OpenJDK Runtime Environment
Runtime Version: 1.8.0_302-b08
VM Vendor: Oracle Corporation
VM Version: 25.302-b08

Generating a new secret key...
Saving secret key to keystore ...
encodedKey = GIIwcteAdxwHALIz+nGFFGGOrq8TZisxFa25Noh4GHo=
```

On Oracle JDK container, run the `load` command:
```shell
# Execute this inside the Oracle JDK container:
/jdk1.8.0_301/bin/java -jar build/libs/keystore-demo-0.1.0-all.jar load
```
Output:
```shell

Runtime Name: Java(TM) SE Runtime Environment
Runtime Version: 1.8.0_301-b09
VM Vendor: Oracle Corporation
VM Version: 25.301-b09

Loading secret key from keystore ...
encodedKey = GIIwcteAdxwHALIz+nGFFGGOrq8TZisxFa25Noh4GHo=
```

**Result**: Works!

---

### Test 4: `save` on Oracle JDK 1.8.0_301-b09 followed by `load` on OpenJDK 1.8.0_302-b08

On the Oracle JDK container, run the `save` command:
```shell
# Execute this inside the Oracle JDK container:
/jdk1.8.0_301/bin/java -jar build/libs/keystore-demo-0.1.0-all.jar save
```
Output:
```shell

Runtime Name: Java(TM) SE Runtime Environment
Runtime Version: 1.8.0_301-b09
VM Vendor: Oracle Corporation
VM Version: 25.301-b09

Generating a new secret key...
Saving secret key to keystore ...
encodedKey = HypJtmNXt5Po+rVeaYk9vGmk4hJopzxtjl8sI9pJXeA=
```

On the OpenJDK container, run the `load` command:

```shell
# Execute this inside the OpenJDK container:
java -jar build/libs/keystore-demo-0.1.0-all.jar load
```

This is expected to fail with the following output:
```shell
root@58a280c3d9bc:/keystore-demo# java -jar build/libs/keystore-demo-0.1.0-all.jar load

Runtime Name: OpenJDK Runtime Environment
Runtime Version: 1.8.0_302-b08
VM Vendor: Oracle Corporation
VM Version: 25.302-b08

Loading secret key from keystore ...
Exception in thread "main" java.security.UnrecoverableKeyException: Private key not stored as PKCS#8 EncryptedPrivateKeyInfo: java.io.IOException: ObjectIdentifier() -- data isn't an object ID (tag = 48)
	at sun.security.pkcs12.PKCS12KeyStore.engineGetKey(PKCS12KeyStore.java:360)
	at sun.security.pkcs12.PKCS12KeyStore.engineGetEntry(PKCS12KeyStore.java:1319)
	at java.security.KeyStore.getEntry(KeyStore.java:1521)
	at com.hitachivantara.keystore.Demo.loadKeyFromKeystore(Demo.java:85)
	at com.hitachivantara.keystore.Demo.main(Demo.java:45)
Caused by: java.io.IOException: ObjectIdentifier() -- data isn't an object ID (tag = 48)
	at sun.security.util.ObjectIdentifier.<init>(ObjectIdentifier.java:285)
	at sun.security.util.DerInputStream.getOID(DerInputStream.java:320)
	at com.sun.crypto.provider.PBES2Parameters.engineInit(PBES2Parameters.java:267)
	at java.security.AlgorithmParameters.init(AlgorithmParameters.java:293)
	at sun.security.x509.AlgorithmId.decodeParams(AlgorithmId.java:151)
	at sun.security.x509.AlgorithmId.<init>(AlgorithmId.java:133)
	at sun.security.x509.AlgorithmId.parse(AlgorithmId.java:413)
	at sun.security.pkcs.EncryptedPrivateKeyInfo.<init>(EncryptedPrivateKeyInfo.java:80)
	at sun.security.pkcs12.PKCS12KeyStore.engineGetKey(PKCS12KeyStore.java:349)
	... 4 more
```


