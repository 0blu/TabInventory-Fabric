package wtf.blu.tabinventory.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends Screen {
    protected ContainerScreenMixin() {
        super(null);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (minecraft.options.keyInventory.matchesKey(keyCode, scanCode) && keyCode == GLFW_KEY_TAB) {
            minecraft.player.closeContainer();
            callbackInfo.setReturnValue(true);
            callbackInfo.cancel();
        }
    }
}
