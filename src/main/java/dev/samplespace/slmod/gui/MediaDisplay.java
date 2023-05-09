package dev.samplespace.slmod.gui;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.samplespace.slmod.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MediaDisplay {
    private final MinecraftClient client;
    private final Runtime runtime;
    private String artist = "Unknown";
    private String title = "Unknown";
    private boolean playing = true;
    private boolean enabled = true;

    public MediaDisplay(MinecraftClient client) {
        this.client = client;
        this.runtime = Runtime.getRuntime();

        Scheduler.get().schedule(this::updateValues, 0, 30, Integer.MAX_VALUE);

        HudRenderCallback.EVENT.register(this::render);
    }

    public void updateValues() {
        if (this.client.currentScreen != null && !(this.client.currentScreen instanceof ChatScreen) || this.client.isPaused()) {
            return;
        }

        this.artist = this.executeCommand("playerctl metadata artist");
        this.title = this.executeCommand("playerctl metadata title");
        this.playing = this.executeCommand("playerctl status").equals("Playing");
    }

    private void render(MatrixStack matrixStack, float delta) {
        if (!this.shouldRender()) {
            return;
        }
        int scaledWidth = this.client.getWindow().getScaledWidth() - 5;

        TextRenderer textRenderer = this.client.textRenderer;
        int indicatorWidth = textRenderer.getWidth("▶") + 3;
        int artistWidth = textRenderer.getWidth(this.artist);
        int separatorWidth = textRenderer.getWidth(" - ");
        int titleWidth = textRenderer.getWidth(this.title);

        textRenderer.draw(matrixStack, this.playing ? "▶" : "⏸", scaledWidth - indicatorWidth - artistWidth - separatorWidth - titleWidth, 6, 0xc7f0e2);
        textRenderer.drawWithShadow(matrixStack, this.artist, scaledWidth - artistWidth - separatorWidth - titleWidth, 5, 0xa9dbca);
        textRenderer.drawWithShadow(matrixStack, " - ", scaledWidth - separatorWidth - titleWidth, 5, 0xAAAAAA);
        textRenderer.drawWithShadow(matrixStack, this.title, scaledWidth - titleWidth, 5, 0x8bd6bc);
    }

    private boolean shouldRender() {
        return this.enabled && !this.client.options.debugEnabled;
    }

    public boolean toggle() {
        this.enabled = !this.enabled;
        return this.enabled;
    }

    private String executeCommand(String command) {
        try {
            Process process = this.runtime.exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            reader.close();

            return line == null ? "Unknown" : line;
        } catch (IOException e) {
            return "Unknown";
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("mediadisplay")
                .then(ClientCommandManager.literal("toggle").executes(context -> {
                    boolean toggled = this.toggle();
                    this.client.player.sendMessage(Text.literal("Media display %s.".formatted(toggled ? "enabled" : "disabled"))
                            .formatted(toggled ? Formatting.GREEN : Formatting.RED));

                    return Command.SINGLE_SUCCESS;
                }).build())
                .then(ClientCommandManager.literal("refresh").executes(context -> {
                    this.updateValues();
                    this.client.player.sendMessage(Text.literal("Media display refreshed.").formatted(Formatting.GREEN));

                    return Command.SINGLE_SUCCESS;
                }).build())
        );
    }
}
