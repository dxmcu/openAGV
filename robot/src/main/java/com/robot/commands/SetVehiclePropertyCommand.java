/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package com.robot.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * 设置车辆属性
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetVehiclePropertyCommand
        implements AdapterCommand {

    /**
     * The property key to set.
     */
    private final String key;
    /**
     * The property value to set.
     */
    private final String value;

    /**
     * Creates a new instance.
     *
     * @param key   The property key to set.
     * @param value The property value to set.
     */
    public SetVehiclePropertyCommand(@Nonnull String key, @Nullable String value) {
        this.key = requireNonNull(key, "key");
        this.value = value;
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        adapter.getProcessModel().setVehicleProperty(key, value);
    }
}
