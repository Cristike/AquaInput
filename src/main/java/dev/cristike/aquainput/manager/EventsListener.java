/*
 *   Copyright (c) 2023 Cristike
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */

package dev.cristike.aquainput.manager;

import dev.cristike.aquainput.input.AquaInput;
import dev.cristike.aquainput.input.enums.InputFlag;
import dev.cristike.aquainput.input.enums.InputMessage;
import dev.cristike.aquainput.response.AquaInputResponse;
import dev.cristike.aquainput.response.enums.InputStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

/**
 * An event listener class that is responsible for getting the input values,
 * restricting the player and complete input requests when needed.
 * */
public class EventsListener implements Listener {

    /* Gets the input from chat, filters it and possibly completes the input request. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        AquaInput input = AquaInputManager.getCurrentRequest(player.getUniqueId());

        if (input == null) return;
        event.setCancelled(true);

        if (input.isValidInput(event.getMessage())) {
            input.sendMessage(InputMessage.SUCCESS, player, event.getMessage());
            AquaInputManager.completeCurrentRequest(
                    player.getUniqueId(),
                    new AquaInputResponse(InputStatus.SUCCESS, event.getMessage()));

            return;
        }

        if (input.getAttempts() > 0)
            input.setAttempts(input.getAttempts() - 1);

        if (input.getAttempts() == 0) {
            AquaInputManager.completeCurrentRequest(
                    player.getUniqueId(),
                    new AquaInputResponse(InputStatus.FAILED_ATTEMPTS, ""));
            return;
        }

        input.sendMessage(InputMessage.INVALID_INPUT, player);
    }

    /* Handles the case when commands are not allowed. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        AquaInput input = AquaInputManager.getCurrentRequest(player.getUniqueId());

        if (input == null) return;
        if (!input.hasFlag(InputFlag.DISABLE_COMMANDS)) return;
        String command = event.getMessage().substring(1).split(" ")[0];

        if (input.isCommandAllowed(command)) return;
        input.sendMessage(InputMessage.DISABLED_COMMANDS, player, command);
        event.setCancelled(true);
    }

    /* Handles the case when movement is not allowed. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        AquaInput input = AquaInputManager.getCurrentRequest(player.getUniqueId());

        if (input == null) return;
        if (!input.hasFlag(InputFlag.DISABLE_MOVEMENT)) return;
        if (event.getFrom().distanceSquared(event.getTo()) == 0) return;
        input.sendMessage(InputMessage.DISABLED_MOVEMENT, player);
        event.setCancelled(true);
    }

    /* Handles the case when interactions are not allowed. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        AquaInput input = AquaInputManager.getCurrentRequest(player.getUniqueId());

        if (input == null) return;
        if (!input.hasFlag(InputFlag.DISABLE_INTERACTION)) return;
        input.sendMessage(InputMessage.DISABLED_INTERACTION, player);
        event.setCancelled(true);
    }

    /* Handles the case when interactions are not allowed. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteracted(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        AquaInput input = AquaInputManager.getCurrentRequest(player.getUniqueId());

        if (input == null) return;
        if (!input.hasFlag(InputFlag.DISABLE_INTERACTION)) return;
        input.sendMessage(InputMessage.DISABLED_INTERACTION, player);
        event.setCancelled(true);
    }

    /* Handles the case when there are somehow input requests for the player when he joins. */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        AquaInputManager.clearAllRequests(event.getPlayer().getUniqueId());
    }

    /* Clears the input requests when the player leaves the server. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        AquaInputManager.completeAllRequests(
                event.getPlayer().getUniqueId(),
                new AquaInputResponse(InputStatus.PLAYER_QUIT, ""));
    }

    /* Clears the input requests when the player dies. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        AquaInputManager.completeAllRequests(
                event.getEntity().getUniqueId(),
                new AquaInputResponse(InputStatus.PLAYER_QUIT, ""));
    }
}
