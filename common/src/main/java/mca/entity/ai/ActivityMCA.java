package mca.entity.ai;

import java.util.function.Supplier;

import mca.MCA;
import mca.cobalt.registration.Registration;
import mca.entity.ai.brain.sensor.ExplodingCreeperSensor;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.util.Identifier;

public interface ActivityMCA {
    Activity CHORE = activity("chore");
    Activity GRIEVE = activity("grieve");

    SensorType<ExplodingCreeperSensor> EXPLODING_CREEPER = sensor("exploding_creeper", ExplodingCreeperSensor::new);

    static void bootstrap() { }

    static Activity activity(String name) {
        return Registration.ObjectBuilders.Activities.create(new Identifier(MCA.MOD_ID, name));
    }

    static <T extends Sensor<?>> SensorType<T> sensor(String name, Supplier<T> factory) {
        return Registration.ObjectBuilders.Sensors.create(new Identifier(MCA.MOD_ID, name), factory);
    }
}
