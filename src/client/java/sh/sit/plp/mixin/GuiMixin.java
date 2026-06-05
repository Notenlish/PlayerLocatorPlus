package sh.sit.plp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.sit.plp.PlayerLocatorPlusClient;

import java.util.Objects;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private SpectatorGui spectatorGui;

    @Inject(
        method = "renderPlayerHealth",
        at = @At(value = "HEAD")
    )
    private void beforeRenderStatusBars(GuiGraphics context, CallbackInfo ci) {
        float offset = PlayerLocatorPlusClient.INSTANCE.getCurrentHudOffset();
        if (offset > 0) {
            context.pose().pushMatrix();
            context.pose().translate(0.0f, -offset);
        }
    }

    @Inject(
        method = "renderPlayerHealth",
        at = @At(value = "RETURN")
    )
    private void afterRenderStatusBars(GuiGraphics context, CallbackInfo ci) {
        if (PlayerLocatorPlusClient.INSTANCE.getCurrentHudOffset() > 0) {
            context.pose().popMatrix();
        }
    }

    @Inject(
        method = "renderHotbarAndDecorations",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z")
    )
    private void beforeRenderExperienceLevel(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        PlayerLocatorPlusClient.INSTANCE.render(context, tickCounter);

        float offset = PlayerLocatorPlusClient.INSTANCE.getCurrentHudOffset();
        if (offset > 0) {
            context.pose().pushMatrix();
            context.pose().translate(0.0f, -offset);
        }
    }

    @Inject(
        method = "renderHotbarAndDecorations",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    )
    private void afterRenderExperienceLevel(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (PlayerLocatorPlusClient.INSTANCE.getCurrentHudOffset() > 0) {
            context.pose().popMatrix();
        }
    }

    @Inject(
        method = "renderChat",
        at = @At(value = "HEAD")
    )
    private void beforeRenderChat(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        float offset = PlayerLocatorPlusClient.INSTANCE.getCurrentHudOffset();
        if (offset > 0) {
            context.pose().pushMatrix();
            context.pose().translate(0.0f, -offset);
        }
    }

    @Inject(
        method = "renderChat",
        at = @At(value = "RETURN")
    )
    private void afterRenderChat(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (PlayerLocatorPlusClient.INSTANCE.getCurrentHudOffset() > 0) {
            context.pose().popMatrix();
        }
    }

    @Inject(
        method = "nextContextualInfoState",
        at = @At(value = "RETURN"),
        cancellable = true
    )
    private void getCurrentBarType(CallbackInfoReturnable<Gui.ContextualInfo> cir) {
        // we hide the vanilla locator bar (so we can draw our own) when our bar should be visible
        // OR when the spectator menu is not open.
        // the vanilla locator bar is visible in spectator without the menu, while our users don't
        // want that (and I agree): https://github.com/timas130/PlayerLocatorPlus/issues/10
        boolean hideVanillaBarInSpectator =
            Objects.requireNonNull(this.minecraft.gameMode).getPlayerMode() == GameType.SPECTATOR
            && !this.spectatorGui.isMenuActive();
        if (
            cir.getReturnValue() == Gui.ContextualInfo.LOCATOR
            && (
                PlayerLocatorPlusClient.INSTANCE.isBarVisible()
                || hideVanillaBarInSpectator
            )
        ) {
            // we don't need to account for the jump bar here, because the locator bar never
            // replaces it in vanilla code
            if (this.minecraft.gameMode.hasExperience()) {
                cir.setReturnValue(Gui.ContextualInfo.EXPERIENCE);
            } else {
                cir.setReturnValue(Gui.ContextualInfo.EMPTY);
            }
        }
    }
}
