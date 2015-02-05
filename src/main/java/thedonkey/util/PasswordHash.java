package thedonkey.util;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Computes the password hashes.
 */
public class PasswordHash {
  private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final byte[] SALT = "0>/Y(}R&p%C{Q)_D|tnoDczyv!.Mr]5<".getBytes();
  private static final int ITERATIONS = 10000;
  private static final int HASH_SIZE = 48;

  /**
   * Computes a password hash.
   * 
   * @param input The password.
   * @return The hash as hex string.
   */
  public static String computeHash(String input) {
    try {
      return toHex(pbkdf2(input.toCharArray(), SALT, ITERATIONS, HASH_SIZE));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException("Security exception: ", e);
    }
  }

  /**
   * Just to see what it does and how slow it is (notice that slow is good when hashing passwords).
   * 
   * @param args CLI arguments.
   */
  public static void main(String[] args) {
    System.out.println(computeHash("password"));

    long startTs = System.currentTimeMillis();
    for (int i = 0; i < 10; i++) {
      computeHash("password" + i);
    }

    System.out.println((System.currentTimeMillis() - startTs) / 10 + " ms per hash.");
  }

  /**
   * Computes the PBKDF2 hash of a password.
   *
   * @param password the password to hash.
   * @param salt the salt
   * @param iterations the iteration count (slowness factor)
   * @param bytes the length of the hash to compute in bytes
   * @return the PBDKF2 hash of the password
   */
  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
    SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
    return skf.generateSecret(spec).getEncoded();
  }

  /**
   * Converts a byte array into a hexadecimal string.
   *
   * @param array the byte array to convert
   * @return a length*2 character string encoding the byte array
   */
  private static String toHex(byte[] array) {
    BigInteger bi = new BigInteger(1, array);
    String hex = bi.toString(16);
    int paddingLength = (array.length * 2) - hex.length();
    if (paddingLength > 0) {
      return String.format("%0" + paddingLength + "d", 0) + hex;
    }
    return hex;
  }
}
