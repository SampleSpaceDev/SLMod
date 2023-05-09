package dev.samplespace.slmod.client;

import dev.samplespace.slmod.gui.MediaDisplay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public class SLModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MediaDisplay mediaDisplay = new MediaDisplay(MinecraftClient.getInstance());

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            mediaDisplay.registerCommand(dispatcher);
        });
    }
}
