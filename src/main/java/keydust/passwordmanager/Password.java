package keydust.passwordmanager;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Password {

    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private static final String ALG = "PBKDF2WithHmacSHA256";

    private final char[] password;
    private final byte[] salt;
    private byte[] hash;

    public Password(String password) {
        this.password = password.toCharArray();
        this.salt = new byte[16];
        new SecureRandom().nextBytes(this.salt);
        this.hash = derive(this.password, this.salt);
    }

    public Password(String password, String saltBase64) {
        this.password = password.toCharArray();
        this.salt = Base64.getDecoder().decode(saltBase64);
        this.hash = derive(this.password, this.salt);
    }

    public boolean checkHash(String storedHashBase64) {
        byte[] stored = Base64.getDecoder().decode(storedHashBase64);
        return MessageDigest.isEqual(stored, this.hash);
    }

    public String getHash() {
        return Base64.getEncoder().encodeToString(hash);
    }

    public String getSalt() {
        return Base64.getEncoder().encodeToString(salt);
    }

    private static byte[] derive(char[] password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(ALG);
                return factory.generateSecret(spec).getEncoded();
            } finally {
                if (spec instanceof PBEKeySpec pbe) pbe.clearPassword();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cryption failed", e);
        }
    }


}