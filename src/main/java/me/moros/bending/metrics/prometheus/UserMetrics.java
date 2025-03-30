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

package me.moros.bending.metrics.prometheus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import me.moros.bending.api.registry.Registries;
import me.moros.bending.api.user.User;
import net.kyori.adventure.key.Key;

final class UserMetrics implements MultiCollector {
  private static final String METRIC_NAME_USERS = "bending_online_users";
  private static final String METRIC_NAME_NPC_USERS = "bending_online_npc_users";
  private static final String METRIC_NAME_ABILITY_BINDS = "bending_ability_binds";

  private static final List<String> ALL_METRIC_NAMES = List.of(
    METRIC_NAME_USERS,
    METRIC_NAME_NPC_USERS,
    METRIC_NAME_ABILITY_BINDS
  );

  private final Gauge onlineUserGauge;
  private final Gauge onlineNpcUserGauge;
  private final Gauge abilityBindGauge;

  UserMetrics() {
    onlineUserGauge = Gauge.builder()
      .name(METRIC_NAME_USERS)
      .help("Number of online bending users")
      .build();
    onlineNpcUserGauge = Gauge.builder()
      .name(METRIC_NAME_NPC_USERS)
      .help("Number of online bending npc users")
      .build();
    abilityBindGauge = Gauge.builder()
      .name(METRIC_NAME_ABILITY_BINDS)
      .help("User ability binds")
      .labelNames("namespace", "ability")
      .build();
  }

  @Override
  public MetricSnapshots collect() {
    onlineUserGauge.set(Registries.BENDERS.playersCount());
    onlineNpcUserGauge.set(Registries.BENDERS.nonPlayerCount());

    Map<Key, Integer> abilityBindCounts = new HashMap<>();
    for (User user : Registries.BENDERS) {
      user.slots().abilities().stream().filter(Objects::nonNull).distinct().forEach(desc ->
        abilityBindCounts.merge(desc.key(), 1, Integer::sum)
      );
    }
    abilityBindCounts.forEach((key, count) -> abilityBindGauge.labelValues(key.namespace(), key.value()).set(count));

    return MetricSnapshots.builder()
      .metricSnapshot(onlineUserGauge.collect())
      .metricSnapshot(onlineNpcUserGauge.collect())
      .metricSnapshot(abilityBindGauge.collect())
      .build();
  }

  @Override
  public List<String> getPrometheusNames() {
    return ALL_METRIC_NAMES;
  }
}
