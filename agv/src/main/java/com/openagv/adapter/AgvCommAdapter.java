package com.openagv.adapter;

import com.google.inject.assistedinject.Assisted;
import com.openagv.config.AgvConfiguration;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.virtualvehicle.AdapterPanelComponentsFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

public class AgvCommAdapter extends BasicVehicleCommAdapter {

    /**大杀器*/
    private TCSObjectService tcsObjectService;
    private ExecutorService kernelExecutor;
    private AdapterComponentsFactory componentsFactory;
    private AgvConfiguration configuration;
    private Vehicle vehicle;

    @Inject
    public AgvCommAdapter(AdapterComponentsFactory componentsFactory,
                                        TCSObjectService tcsObjectService,
                                        AgvConfiguration configuration,
                                        @Assisted Vehicle vehicle,
                                        @KernelExecutor ExecutorService kernelExecutor) {
        super(new VehicleModel(vehicle),
                configuration.commandQueueCapacity(),
                configuration.sentQueueCapacity(),
                configuration.rechargeOperation());

        this.tcsObjectService = requireNonNull(tcsObjectService, "tcsObjectService");
        this.vehicle = requireNonNull(vehicle, "vehicle");
        this.configuration = requireNonNull(configuration, "configuration");
        this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
        this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    }

    @Override
    public void sendCommand(MovementCommand cmd) throws IllegalArgumentException {

    }

    @Override
    protected void connectVehicle() {

    }

    @Override
    protected void disconnectVehicle() {

    }

    @Override
    protected boolean isVehicleConnected() {
        return false;
    }

    @Nonnull
    @Override
    public ExplainedBoolean canProcess(@Nonnull List<String> operations) {
        return null;
    }

    @Override
    public void processMessage(@Nullable Object message) {

    }
}