/*
 * Copyright 2022-2024 Moros
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

package me.moros.test.ability;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import me.moros.bending.api.ability.AbilityDescription;
import me.moros.bending.api.ability.AbilityInstance;
import me.moros.bending.api.ability.Activation;
import me.moros.bending.api.ability.MultiUpdatable;
import me.moros.bending.api.ability.common.Pillar;
import me.moros.bending.api.config.Configurable;
import me.moros.bending.api.config.attribute.Attribute;
import me.moros.bending.api.config.attribute.Modifiable;
import me.moros.bending.api.platform.Direction;
import me.moros.bending.api.platform.block.Block;
import me.moros.bending.api.platform.world.WorldUtil;
import me.moros.bending.api.temporal.TempBlock;
import me.moros.bending.api.user.User;
import me.moros.bending.api.util.functional.Policies;
import me.moros.bending.api.util.functional.RemovalPolicy;
import me.moros.bending.api.util.material.EarthMaterials;
import me.moros.math.FastMath;

public class EarthDome extends AbilityInstance {
  private Config userConfig;
  private RemovalPolicy removalPolicy;

  private Predicate<Block> predicate;
  private final MultiUpdatable<Pillar> pillars = MultiUpdatable.empty();

  public EarthDome(AbilityDescription desc) {
    super(desc);
  }

  @Override
  public boolean activate(User user, Activation method) {
    this.user = user;
    loadConfig();
    if (!user.isOnGround()) {
      return false;
    }
    predicate = b -> EarthMaterials.isEarthNotLava(user, b);
    if (calculatePillars()) {
      removalPolicy = Policies.builder().add(Policies.NOT_SNEAKING).build();
      user.addCooldown(description(), userConfig.cooldown);
      return true;
    }
    return false;
  }

  @Override
  public void loadConfig() {
    userConfig = user.game().configProcessor().calculate(this, Config.class);
  }

  @Override
  public UpdateResult update() {
    if (removalPolicy.test(user, description())) {
      return UpdateResult.REMOVE;
    }
    return pillars.update();
  }

  private boolean calculatePillars() {
    int offset = FastMath.ceil(userConfig.radius + 1);
    int size = offset * 2 + 1;
    boolean[][] checked = new boolean[size][size];
    Block center = user.block().offset(Direction.DOWN);
    for (int i = 0; i < 2; i++) {
      double radius = userConfig.radius + i;
      int height = userConfig.height - i;
      for (Block block : WorldUtil.createBlockRing(center, radius)) {
        int dx = offset + center.blockX() - block.blockX();
        int dz = offset + center.blockZ() - block.blockZ();
        if (checked[dx][dz]) {
          continue;
        }
        Optional<Pillar> pillar = block.world().findTop(block, height, predicate)
          .flatMap(b -> createPillar(b, height));
        if (pillar.isPresent()) {
          checked[dx][dz] = true;
          pillars.add(pillar.get());
        }
      }
    }
    return !pillars.isEmpty();
  }

  private Optional<EarthPillar> createPillar(Block block, int height) {
    if (!predicate.test(block) || !TempBlock.isBendable(block)) {
      return Optional.empty();
    }
    return Pillar.builder(user, block, EarthPillar::new).interval(75).predicate(predicate)
      .build(height + 2, height);
  }

  @Override
  public User user() {
    return user;
  }

  private static final class EarthPillar extends Pillar {
    private EarthPillar(Builder<EarthPillar> builder) {
      super(builder);
    }

    @Override
    public void playSound(Block block) {
      if (ThreadLocalRandom.current().nextInt(8) == 0) {
        super.playSound(block);
      }
    }
  }

  private static final class Config implements Configurable {
    @Modifiable(Attribute.COOLDOWN)
    private long cooldown = 9000;
    @Modifiable(Attribute.RADIUS)
    private double radius = 3;
    @Modifiable(Attribute.HEIGHT)
    private int height = 3;

    @Override
    public List<String> path() {
      return List.of("abilities", "earth", "earthdome");
    }
  }
}
