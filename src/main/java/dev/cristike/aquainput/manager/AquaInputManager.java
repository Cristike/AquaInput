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
import dev.cristike.aquainput.input.enums.InputMessage;
import dev.cristike.aquainput.request.AquaInputRequest;
import dev.cristike.aquainput.response.AquaInputResponse;
import dev.cristike.aquainput.response.enums.InputStatus;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * The main class that provides the function to prompt for input.
 * */
public class AquaInputManager {
    private static JavaPlugin plugin;
    private static final Map<UUID, Queue<AquaInputRequest>> requestsQueue = new HashMap<>();

    /**
     * Prompts the player with the given unique id for input of a given form
     * defined by the AquaInput object.
     *
     * @param plugin the plugin
     * @param uuid the unique id
     * @param input the input type
     *
     * @return a CompletableFuture that returns the response to the prompt.
     * */
    public static CompletableFuture<AquaInputResponse> promptInput(@NotNull JavaPlugin plugin,
                                                                   @NotNull UUID uuid,
                                                                   @NotNull AquaInput input) {
        if (AquaInputManager.plugin == null) initialize(plugin);
        Queue<AquaInputRequest> queue = requestsQueue.computeIfAbsent(uuid, k -> new LinkedList<>());
        AquaInputRequest request = new AquaInputRequest(input);

        queue.add(request);
        if (queue.size() == 1)
            initializeInputRequest(uuid, request);

        return request.getFuture();
    }

    /* Initializes the tool for the hosting plugin. */
    private static void initialize(@NotNull JavaPlugin plugin) {
        AquaInputManager.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new EventsListener(), plugin);
    }

    /* Initializes the new request with the timeout task and sending the prompt message. */
    private static void initializeInputRequest(@NotNull UUID uuid, @NotNull AquaInputRequest request) {
        /* Sending the prompt message. */
        sendInputMessage(uuid, request.getInput(), InputMessage.PROMPT);

        /* Initializing the timeout task. */
        if (request.getInput().getTimeout() < 0) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!requestsQueue.containsKey(uuid)) return;
            Queue<AquaInputRequest> requests = requestsQueue.get(uuid);

            if (requests.element() != request) return;
            sendInputMessage(uuid, request.getInput(), InputMessage.TIMEOUT);
            completeCurrentRequest(uuid, new AquaInputResponse(InputStatus.TIMEOUT, ""));
        }, request.getInput().getTimeout() * 20L);
    }
    
    /* Sends the input message to the player with the given unique id if he's online. */
    private static void sendInputMessage(@NotNull UUID uuid, @NotNull AquaInput input, @NotNull InputMessage message) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) input.sendMessage(message, player);
    }

    /* Gets the current head of the input requests queue. */
    @Nullable
    protected static AquaInput getCurrentRequest(@NotNull UUID uuid) {
        if (!requestsQueue.containsKey(uuid)) return null;
        return requestsQueue.get(uuid).element().getInput();
    }

    /* Completes the CompletableFuture of the current head of the input requests queue. */
    protected static void completeCurrentRequest(@NotNull UUID uuid, @NotNull AquaInputResponse response) {
        Queue<AquaInputRequest> requests = requestsQueue.get(uuid);

        requests.element().getFuture().complete(response);
        requests.remove();

        if (requests.isEmpty()) requestsQueue.remove(uuid);
        else initializeInputRequest(uuid, requests.element());
    }

    /* Completes all the CompletableFuture from the input requests queue. */
    protected static void completeAllRequests(@NotNull UUID uuid, @NotNull AquaInputResponse response) {
        if (!requestsQueue.containsKey(uuid)) return;

        requestsQueue.get(uuid).forEach(request -> request.getFuture().complete(response));
        requestsQueue.remove(uuid);
    }

    /* Clears all the input requests without completing them. */
    protected static void clearAllRequests(@NotNull UUID uuid) {
        requestsQueue.remove(uuid);
    }
}
