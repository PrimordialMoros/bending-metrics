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

package me.moros.bending.metrics;

import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.BasicAuthenticator;
import me.moros.bending.api.util.KeyUtil;
import me.moros.bending.metrics.util.PasswordEncoder;

final class Authenticator extends BasicAuthenticator {
  private final String username;
  private final String token;

  Authenticator(String username, String token) {
    super(KeyUtil.BENDING_NAMESPACE, StandardCharsets.UTF_8);
    this.username = username;
    this.token = token;
  }

  @Override
  public boolean checkCredentials(String username, String password) {
    return this.username.equals(username) && PasswordEncoder.create().authenticate(password.toCharArray(), token);
  }
}
