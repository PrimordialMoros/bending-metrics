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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

record SimpleHandler(byte[] responseBytes) implements HttpHandler {
  private static final String CONTENT_TYPE = "text/plain; charset=utf-8";

  SimpleHandler() {
    this("Metrics path is /metrics\n".getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE);
      exchange.getResponseHeaders().set("Content-Length", Integer.toString(responseBytes.length));
      exchange.sendResponseHeaders(200, responseBytes.length);
      exchange.getResponseBody().write(responseBytes);
    } finally {
      exchange.close();
    }
  }
}
