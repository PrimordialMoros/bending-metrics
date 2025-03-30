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
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.Executors;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import me.moros.bending.api.addon.Addon;
import me.moros.bending.api.addon.BendingContext;
import me.moros.bending.api.config.Configurable;
import me.moros.bending.api.game.Game;
import me.moros.bending.metrics.prometheus.BendingMetrics;
import me.moros.bending.metrics.util.PasswordEncoder;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;

public final class MetricsAddon implements Addon {
  private final PrometheusRegistry registry = new PrometheusRegistry();
  private Config config;
  private HTTPServer server;

  @Override
  public void load(BendingContext context) {
    config = context.configLoader().load(Config::new);
  }

  @Override
  public void enable(Game game) {
    if (config.enabled) {
      registry.register(BendingMetrics.ability(game));
      registry.register(BendingMetrics.temporal());
      registry.register(BendingMetrics.user());
      initializeWebServer(config);
    }
  }

  @Override
  public void unload() {
    if (server != null) {
      server.close();
      server = null;
    }
    registry.clear();
  }

  private void initializeWebServer(Config config) {
    if (server != null) {
      return;
    }
    var builder = HTTPServer.builder()
      .port(config.port)
      .executorService(Executors.newVirtualThreadPerTaskExecutor())
      .registry(registry)
      .defaultHandler(new SimpleHandler());

    if (config.auth) {
      builder.authenticator(new Authenticator(config.username, config.password));
    }
    try {
      server = builder.buildAndStart();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static final class Config implements Configurable {
    private final boolean enabled = true;
    private final int port = 9666;
    private final boolean auth = false;
    private final String username = "bending";
    private String password = "bending";

    @Override
    public List<String> path() {
      return List.of("metrics");
    }

    @PostProcess
    private void validate() {
      if (!PasswordEncoder.ENCRYPTED_PATTERN.asPredicate().test(password)) {
        password = PasswordEncoder.create().hash(password.toCharArray());
      }
    }
  }
}
