package keydust.contollers;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoController {
    private static final String PREFIX = "v2:";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;
    private static final int KEY_BITS = 256;
    private static final int PBKDF2_ITERATIONS = 210_000;

    private static byte[] AAD = "KeyDust:v2".getBytes(StandardCharsets.UTF_8);

    private final SecureRandom random = new SecureRandom();

    public boolean isV2(String value) {
        return  value != null && value.startsWith(PREFIX);
    }

    public String encrypt(String masterPassword, byte[] encSalt, String plaintext) {
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(masterPassword, encSalt), new GCMParameterSpec(TAG_BITS, iv));

            cipher.updateAAD(AAD);

            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer bb = ByteBuffer.allocate(iv.length + ct.length);
            bb.put(iv);
            bb.put(ct);

            return PREFIX + Base64.getEncoder().encodeToString(bb.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String masterPassword, byte[] encSalt, String ciphertext) {
        if (!isV2(ciphertext)) {
            throw new IllegalStateException("Unsupported ciphertext format");
        }
        try {
            byte[] blob = Base64.getDecoder().decode(ciphertext.substring(PREFIX.length()));
            if (blob.length <= IV_LEN) {
                throw new IllegalStateException("Ciphertext too short");
            }

            byte[] iv = new byte[IV_LEN];
            byte[] ct = new byte[blob.length - IV_LEN];
            System.arraycopy(blob, 0, iv, 0 , IV_LEN);
            System.arraycopy(blob, IV_LEN, ct, 0, ct.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(masterPassword, encSalt), new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(AAD);

            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalStateException e) {
            throw new IllegalStateException("Decryption failed, may wrong password or corrupted data.",e );
        }
    }

    private static SecretKeySpec deriveKey(String masterPassword, byte[] encSalt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), encSalt, PBKDF2_ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] key = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        } finally {
            spec.clearPassword();
        }
    }

}
