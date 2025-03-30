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

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import me.moros.bending.api.ability.AbilityDescription;
import me.moros.bending.api.event.AbilityActivationEvent;
import me.moros.bending.api.event.BendingDamageEvent;
import me.moros.bending.api.game.Game;

final class AbilityMetrics implements MultiCollector {
  private static final String METRIC_NAME_ABILITY_ACTIVATIONS = "bending_ability_activations";
  private static final String METRIC_NAME_ACTIVE_ABILITY_INSTANCES = "bending_ability_instances";
  private static final String METRIC_NAME_DAMAGE = "bending_ability_damage";

  private static final List<String> ALL_METRIC_NAMES = List.of(
    METRIC_NAME_ABILITY_ACTIVATIONS,
    METRIC_NAME_ACTIVE_ABILITY_INSTANCES,
    METRIC_NAME_DAMAGE
  );

  private final Game game;

  private final Counter abilityActivationCounter;
  private final Gauge abilityInstancesGauge;
  private final Counter damageCounter;

  AbilityMetrics(Game game) {
    this.game = game;
    registerEventHandlers();

    abilityActivationCounter = Counter.builder()
      .name(METRIC_NAME_ABILITY_ACTIVATIONS)
      .help("Ability activations totals")
      .labelNames("namespace", "ability")
      .build();
    abilityInstancesGauge = Gauge.builder()
      .name(METRIC_NAME_ACTIVE_ABILITY_INSTANCES)
      .help("Active ability instances")
      .labelNames("world")
      .build();
    damageCounter = Counter.builder()
      .name(METRIC_NAME_DAMAGE)
      .help("Bending damage totals")
      .labelNames("namespace", "ability")
      .build();
  }

  private void registerEventHandlers() {
    game.eventBus().subscribe(AbilityActivationEvent.class, this::onAbilityActivation);
    game.eventBus().subscribe(BendingDamageEvent.class, this::onBendingDamage, Integer.MAX_VALUE);
  }

  // Event handlers - Start
  private void onAbilityActivation(AbilityActivationEvent event) {
    AbilityDescription desc = event.ability();
    abilityActivationCounter.labelValues(desc.key().namespace(), desc.key().value()).inc();
  }

  private void onBendingDamage(BendingDamageEvent event) {
    AbilityDescription desc = event.ability();
    damageCounter.labelValues(desc.key().namespace(), desc.key().value()).inc(event.damage());
  }
  // Event handlers - End

  @Override
  public MetricSnapshots collect() {
    game.worldManager().forEach(mgr ->
      abilityInstancesGauge.labelValues(mgr.worldKey().asString()).set(mgr.size())
    );

    return MetricSnapshots.builder()
      .metricSnapshot(abilityActivationCounter.collect())
      .metricSnapshot(abilityInstancesGauge.collect())
      .metricSnapshot(damageCounter.collect())
      .build();
  }

  @Override
  public List<String> getPrometheusNames() {
    return ALL_METRIC_NAMES;
  }
}
