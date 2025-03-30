/*
 * Copyright 2020-2025 Moros
 *
 * This file is part of Bending.
 *
 * Bending is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bending is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bending. If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.bending.metrics.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PasswordEncoder {
  // owasp recommendation
  public static final int ITERATIONS = 600_000;

  public static final String PREFIX = "ENC(";
  public static final String SUFFIX = ")";
  public static final Pattern ENCRYPTED_PATTERN = Pattern.compile("^ENC\\((.{86})\\)$");

  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int SIZE = 256;
  private final int iterations;
  private final SecureRandom random;

  private PasswordEncoder(int iterations, SecureRandom random) {
    this.iterations = iterations;
    this.random = random;
  }

  private PasswordEncoder() {
    this(ITERATIONS, new SecureRandom());
  }

  public String hash(char[] password) {
    byte[] salt = new byte[SIZE / 8];
    random.nextBytes(salt);
    byte[] dk = pbkdf2(password, salt, iterations);
    byte[] hash = new byte[salt.length + dk.length];
    System.arraycopy(salt, 0, hash, 0, salt.length);
    System.arraycopy(dk, 0, hash, salt.length, dk.length);
    return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(hash) + SUFFIX;
  }

  public boolean authenticate(char[] password, String token) {
    Matcher m = ENCRYPTED_PATTERN.matcher(token);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid token format");
    }
    byte[] hash = Base64.getUrlDecoder().decode(m.group(1));
    byte[] salt = Arrays.copyOfRange(hash, 0, SIZE / 8);
    byte[] check = pbkdf2(password, salt, iterations);
    int zero = 0;
    for (int idx = 0; idx < check.length; ++idx) {
      zero |= hash[salt.length + idx] ^ check[idx];
    }
    return zero == 0;
  }

  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
    KeySpec spec = new PBEKeySpec(password, salt, iterations, SIZE);
    try {
      SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
      return f.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Missing algorithm: " + ALGORITHM, ex);
    } catch (InvalidKeySpecException ex) {
      throw new IllegalStateException("Invalid SecretKeyFactory", ex);
    }
  }

  public static PasswordEncoder create() {
    return new PasswordEncoder();
  }
}
