package login.tagbox.task.loginbox.utils;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class PasswordEncryption {

    public static final String KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore";
    public static final String TYPE_RSA = "RSA";
    private static KeyStore keyStore;
    public static void createKeys(Context context, String alias) throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException {

        keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        try {
            keyStore.load(null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        // initialising calendar for setting keystore key validity
        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 100);
        KeyPairGenerator kpGenerator = KeyPairGenerator
                .getInstance(TYPE_RSA,
                        KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        AlgorithmParameterSpec spec = null;

        // creating keys for apis between kitkat and marshmallow
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // Below Android M, use the KeyPairGeneratorSpec.Builder.


            spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(new X500Principal("CN=" + alias))
                    .setSerialNumber(BigInteger.valueOf(1337))
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
        }
        // On Android M or above,
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            spec = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                    .setCertificateSubject(new X500Principal("CN=" + alias))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setCertificateSerialNumber(BigInteger.valueOf(1337))
                    .setCertificateNotBefore(start.getTime())
                    .setCertificateNotAfter(end.getTime())
                    .build();
        }
        kpGenerator.initialize(spec);
        kpGenerator.generateKeyPair();
    }

    public String encryptString(String alias,String password) {
        byte [] valuebytes = null;
        try {
            byte[] key = null;

            try {
                keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
                keyStore.load(null);
                KeyStore.Entry entry = keyStore.getEntry(alias, null);
                key = entry.toString().getBytes("UTF-8");
            } catch (CertificateException
                    | KeyStoreException
                    | UnrecoverableEntryException
                    | NoSuchAlgorithmException
                    | IOException e
                    ) {
            }

            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            SecretKeySpec sks = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, cipher);
            cipherOutputStream.write(password.getBytes("UTF-8"));
            cipherOutputStream.close();

             valuebytes = outputStream.toByteArray();

        } catch (Exception e) {

        }
        return Base64.encodeToString(valuebytes, Base64.DEFAULT);
    }

    public String decryptString(String alias,String passwordHash) {
        String finalString = null;
        try {

            byte[] key = null;

            try {
                keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
                keyStore.load(null);
                KeyStore.Entry entry = keyStore.getEntry(alias, null);
                key = entry.toString().getBytes("UTF-8");
            } catch (CertificateException
                    | KeyStoreException
                    | UnrecoverableEntryException
                    | NoSuchAlgorithmException
                    | IOException e
                    ) {
            }

            // getting cipher input stream
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            SecretKeySpec sks = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sks);


            String cipherText = passwordHash;
            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), cipher);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;

            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }
            String finalText = new String(bytes, 0, bytes.length, "UTF-8");
            finalString = finalText;
        } catch (Exception e) {
        }
        return finalString;
    }

}