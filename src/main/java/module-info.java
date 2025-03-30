module me.moros.bending.metrics {
  exports me.moros.bending.metrics;
  exports me.moros.bending.metrics.prometheus;
  exports me.moros.bending.metrics.util;

  requires static me.moros.bending;
  requires static io.prometheus.metrics.core;
  requires static io.prometheus.metrics.exporter.httpserver;
  requires static io.prometheus.metrics.model;
  requires static jdk.httpserver;
  requires static org.spongepowered.configurate;

  provides me.moros.bending.api.addon.Addon with me.moros.bending.metrics.MetricsAddon;
}
