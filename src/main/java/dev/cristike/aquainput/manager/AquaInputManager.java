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
import dev.cristike.aquainput.request.AquaInputRequest;
import dev.cristike.aquainput.response.AquaInputResponse;
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
        queue.add(new AquaInputRequest(input));

        return queue.element().getFuture();
    }

    @Nullable
    protected static AquaInput getCurrentRequest(@NotNull UUID uuid) {
        if (!requestsQueue.containsKey(uuid)) return null;
        return requestsQueue.get(uuid).element().getInput();
    }

    protected static void completeCurrentRequest(@NotNull UUID uuid, @NotNull AquaInputResponse response) {
        Queue<AquaInputRequest> requests = requestsQueue.get(uuid);

        requests.element().getFuture().complete(response);
        requests.remove();

        if (requests.isEmpty()) requestsQueue.remove(uuid);
    }

    protected static void completeAllRequests(@NotNull UUID uuid, @NotNull AquaInputResponse response) {
        requestsQueue.get(uuid).forEach(request -> completeCurrentRequest(uuid, response));
    }

    private static void initialize(@NotNull JavaPlugin plugin) {
        AquaInputManager.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new EventsListener(), plugin);
    }
}
