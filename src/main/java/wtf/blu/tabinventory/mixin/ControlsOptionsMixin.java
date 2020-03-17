package wtf.blu.tabinventory.mixin;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.options.ControlsListWidget;
import net.minecraft.client.gui.screen.options.ControlsOptionsScreen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;

@Mixin(ControlsOptionsScreen.class)
public abstract class ControlsOptionsMixin extends Screen {
    public ControlsOptionsMixin() {
        super(null);
    }

    @Shadow
    public KeyBinding focusedBinding;

    @Shadow
    public long time;

    @Shadow
    private ControlsListWidget keyBindingListWidget;

    @Shadow @Final private GameOptions options;

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (focusedBinding != null && keyCode == GLFW_KEY_TAB) {
            InputUtil.KeyCode newKeyCode = InputUtil.getKeyCode(keyCode, scanCode);

            if (
                    !focusedBinding.matchesKey(keyCode, scanCode) && // It was not changed
                    !focusedBinding.getDefaultKeyCode().equals(newKeyCode) // TAB is already the default key
            ) {
                // User really tries to set TAB to a binding
                // Catch the event
                callbackInfo.setReturnValue(true);
                callbackInfo.cancel();

                KeyBinding tmpBinding = focusedBinding;
                focusedBinding = null;

                // Ask him if he is really sure about this
                AskUserAndBind(tmpBinding, newKeyCode);
            }
        }
    }

    private void AskUserAndBind(KeyBinding binding, InputUtil.KeyCode newKeyCode) {
        Text unsafeKeyText = new TranslatableText(newKeyCode.getName()).formatted(Formatting.YELLOW);
        Text titleText = new TranslatableText("controls.unsafeBinding.title").formatted(Formatting.RED, Formatting.BOLD);

        String[] splitted = I18n.translate("controls.unsafeBinding.message", "%s").split("%s", 2);
        Text messageText = new LiteralText(splitted[0]);
        if (splitted.length > 1) {
            messageText.append(unsafeKeyText);
            messageText.append(splitted[1]);
        }

        double scrollAmount = keyBindingListWidget.getScrollAmount();
        minecraft.openScreen(new ConfirmScreen((confirmation) -> {
            if (confirmation) {
                // Set and save the binding
                options.setKeyCode(binding, newKeyCode);
                time = Util.getMeasuringTimeMs();
                KeyBinding.updateKeysByCode();
            }

            // Go back to the ControlsOptionsScreen
            minecraft.openScreen(this);
            keyBindingListWidget.setScrollAmount(scrollAmount);
        }, titleText, messageText));
    }
}