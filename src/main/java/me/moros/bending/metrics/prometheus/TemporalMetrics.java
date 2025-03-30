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

import java.util.List;
import java.util.Map;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import me.moros.bending.api.temporal.TempBlock;
import me.moros.bending.api.temporal.TempDisplayEntity;
import me.moros.bending.api.temporal.TempEntity;
import me.moros.bending.api.temporal.TempLight;
import me.moros.bending.api.temporal.TemporalManager;

final class TemporalMetrics implements MultiCollector {
  private static final String METRIC_NAME_TEMPORAL_INSTANCES = "bending_temporal_instances";

  private static final List<String> ALL_METRIC_NAMES = List.of(METRIC_NAME_TEMPORAL_INSTANCES);

  private static final Map<String, TemporalManager<?, ?>> TEMPORAL_MANAGERS = Map.of(
    "light", TempLight.MANAGER,
    "entity", TempEntity.MANAGER,
    "display", TempDisplayEntity.MANAGER,
    "block", TempBlock.MANAGER
  );

  private final Gauge temporalGauge;

  TemporalMetrics() {
    temporalGauge = Gauge.builder()
      .name(METRIC_NAME_TEMPORAL_INSTANCES)
      .help("Active temporal instances")
      .labelNames("type")
      .build();
  }

  @Override
  public MetricSnapshots collect() {
    TEMPORAL_MANAGERS.forEach((key, value) -> temporalGauge.labelValues(key).set(value.size()));
    return MetricSnapshots.builder().metricSnapshot(temporalGauge.collect()).build();
  }

  @Override
  public List<String> getPrometheusNames() {
    return ALL_METRIC_NAMES;
  }
}
