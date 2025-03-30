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

package me.moros.test;

import java.util.ResourceBundle;

import me.moros.bending.api.ability.AbilityDescription;
import me.moros.bending.api.ability.Activation;
import me.moros.bending.api.ability.element.Element;
import me.moros.bending.api.addon.Addon;
import me.moros.bending.api.addon.BendingContext;
import me.moros.bending.api.locale.Translation;
import me.moros.bending.api.registry.Registries;
import me.moros.test.ability.EarthDome;
import net.kyori.adventure.util.UTF8ResourceBundleControl;

public class Bootstrap implements Addon {
  @Override
  public void load(BendingContext context) {
    // First let's register our AbilityDescription
    AbilityDescription earthDome = AbilityDescription.builder("bending-addon", "EarthDome", EarthDome::new)
      .element(Element.EARTH).activation(Activation.SNEAK).build();
    Registries.ABILITIES.register(earthDome);

    // Now we construct a translation for the default locale
    // Note: You can also manually construct a translation using Translation#create but bundles are easier to manage for multiple keys
    ResourceBundle bundle = ResourceBundle.getBundle("earthdome", Translation.DEFAULT_LOCALE, UTF8ResourceBundleControl.get());
    // If you want to register multiple locales then make sure to provide unique keys for each locale.
    Translation translation = Translation.fromBundle(earthDome.key(), bundle); // Since we are only registering one locale, we'll use the ability key

    Registries.TRANSLATIONS.register(translation); // Register the translation containing description and instructions for EarthDome
  }
}
