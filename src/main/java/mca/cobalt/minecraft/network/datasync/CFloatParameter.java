package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public class CFloatParameter extends CDataParameter<Float> {
    private final DataTracker data;
    private final float defaultValue;

    public CFloatParameter(String id, Class<? extends Entity> e, DataTracker d, float dv) {
        super(id, e, TrackedDataHandlerRegistry.FLOAT);
        data = d;
        defaultValue = dv;
    }

    public float get() {
        return data.get(param);
    }

    public void set(float v) {
        data.set(param, v);
    }

    @Override
    public void register() {
        data.startTracking(param, defaultValue);
    }

    @Override
    public void load(CNBT nbt) {
        set(nbt.getFloat(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setFloat(id, get());
    }
}