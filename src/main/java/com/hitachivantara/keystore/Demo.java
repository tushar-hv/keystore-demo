package com.hitachivantara.keystore;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Demo {

    private static final String KEY_GENERATION_ALGORITHM = "AES";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEY_ALIAS = "ldc-encryption-key";

    public static void main(String[] args) throws Exception {

        System.out.println(
                "\n" +
                        "Runtime Name: " + System.getProperty("java.runtime.name") + "\n" +
                        "Runtime Version: " + System.getProperty("java.runtime.version") + "\n" +
                        "VM Vendor: " + System.getProperty("java.vm.vendor") + "\n" +
                        "VM Version: " + System.getProperty("java.vm.version") + "\n"
        );


        if (args.length != 1) {
            printUsageAndExit();
        }
        String action = args[0];

        String keystorePath = "keystore";
        String keystorePassword = "waterlinedata";

        if ("save".equalsIgnoreCase(action)) {
            SecretKey secretKey = generateKey();
            saveKeyToKeystore(secretKey, keystorePath, keystorePassword);
            String encodedKey = encodeKey(secretKey);
            System.out.println("encodedKey = " + encodedKey);
        } else if ("load".equalsIgnoreCase(action)) {
            SecretKey secretKey = loadKeyFromKeystore(keystorePath, keystorePassword);
            String encodedKey = encodeKey(secretKey);
            System.out.println("encodedKey = " + encodedKey);
        } else {
            printUsageAndExit();
        }
    }

    private static void printUsageAndExit() {
        System.err.println("Error: Argument <save | load> expected");
        System.exit(1);
    }

    private static void saveKeyToKeystore(SecretKey secretKey, String keystorePath, String keystorePassword) throws Exception {
        System.out.println("Saving secret key to keystore ...");
        File file = new File(keystorePath);
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(new FileInputStream(file), keystorePassword.toCharArray());
        keyStore.setEntry(KEY_ALIAS, new KeyStore.SecretKeyEntry(secretKey), new KeyStore.PasswordProtection(keystorePassword.toCharArray()));
        keyStore.store(new FileOutputStream(file), keystorePassword.toCharArray());
    }

    private static String encodeKey(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_GENERATION_ALGORITHM);
        System.out.println("Generating a new secret key...");
        kg.init(256);
        return kg.generateKey();
    }

    private static SecretKey loadKeyFromKeystore(String keyStoreFile, String keystorePassword) throws Exception {
        System.out.println("Loading secret key from keystore ...");
        KeyStore.PasswordProtection keyStorePP = null;
        try (InputStream keyStoreData = new FileInputStream(keyStoreFile)) {
            keyStorePP = new KeyStore.PasswordProtection(keystorePassword.toCharArray());
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(keyStoreData, keystorePassword.toCharArray());
            KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, keyStorePP);
            if (entry instanceof KeyStore.SecretKeyEntry) {
                KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) entry;
                return secretKeyEntry.getSecretKey();
            }
            throw new RuntimeException("SecretKeyEntry '" + KEY_ALIAS + "' not found in keystore");
        } finally {
            try {
                if (keyStorePP != null && !keyStorePP.isDestroyed()) {
                    keyStorePP.destroy();
                }
            } catch (DestroyFailedException e) {
                System.err.println("Error destroying PasswordProtection");
                e.printStackTrace(System.err);
            }
        }
    }
}
