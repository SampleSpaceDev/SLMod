package dev.samplespace.slmod.mixin;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin() {
        super(Text.empty());
    }

    private final ServerInfo HYPIXEL = new ServerInfo("Hypixel", "mc.hypixel.net", false);
    private final Identifier HYPIXEL_TEXTURE = new Identifier("slmod", "textures/hypixel_logo.png");

    @Inject(at = @At("TAIL"), method = "init()V")
    private void init(CallbackInfo ci) {
        if (this.client == null) {
            return;
        }

        int l = this.height / 4 + 48;
        this.addDrawableChild(new TexturedButtonWidget(this.width / 2 + 107, l + 72 + 12 - 29,
                13, 24, 0, 0, 0, this.HYPIXEL_TEXTURE, 13, 24, (button) ->
                ConnectScreen.connect(new MultiplayerScreen(this), this.client, ServerAddress.parse(this.HYPIXEL.address), this.HYPIXEL),
                Text.literal("Hypixel")
        ));
    }
}
