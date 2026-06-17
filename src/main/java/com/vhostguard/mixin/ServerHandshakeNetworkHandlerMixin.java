package com.vhostguard.mixin;

import com.vhostguard.VhostGuard;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the server's handshake handler to reject connections that don't
 * come through an allowed hostname. This runs before the player actually logs in.
 */
@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {

    @Shadow
    @Final
    private ClientConnection connection;

    @Inject(method = "login", at = @At("HEAD"), cancellable = true)
    private void onLogin(HandshakeC2SPacket packet, boolean transfer, CallbackInfo ci) {
        // Status pings are fine; only check actual join attempts (and transfers, which are joins too).
        ConnectionIntent intent = packet.intendedState();
        if (intent != ConnectionIntent.LOGIN && intent != ConnectionIntent.TRANSFER) {
            return;
        }

        String host = packet.address();
        if (!VhostGuard.getConfig().isAllowed(host)) {
            String message = VhostGuard.getConfig()
                    .getKickMessage()
                    .replace("%host%", host == null ? "unknown" : host);

            // Disconnect here and stop the rest of the login handler from running.
            this.connection.disconnect(Text.literal(message));
            ci.cancel();
        }
    }
}
