module me.moros.test {
  exports me.moros.test;
  exports me.moros.test.ability;

  requires static me.moros.bending;

  provides me.moros.bending.api.addon.Addon with me.moros.test.Bootstrap;
}
