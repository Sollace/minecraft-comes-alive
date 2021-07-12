package mca.network;

import mca.client.gui.GuiInteract;
import mca.cobalt.network.Message;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;

public class GetInteractDataResponse implements Message {
    private static final long serialVersionUID = -4168503424192658779L;

    private final Map<String, Boolean> constraints;
    String father;
    String mother;

    public GetInteractDataResponse(Map<String, Boolean> constraints, String father, String mother) {
        this.constraints = constraints;
        this.father = father;
        this.mother = mother;
    }

    @Override
    public void receive(PlayerEntity player) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiInteract) {
            GuiInteract gui = (GuiInteract) screen;
            gui.setConstraints(constraints);
            gui.setParents(father, mother);
        }
    }
}