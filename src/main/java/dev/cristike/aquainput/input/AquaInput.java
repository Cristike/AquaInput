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

package dev.cristike.aquainput.input;

import dev.cristike.aquainput.input.enums.InputFlag;
import dev.cristike.aquainput.input.enums.InputMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An abstract class that stands as base for all type of inputs.
 * */
public abstract class AquaInput {
    private final Map<InputMessage, String> messages = new HashMap<>();
    private final EnumSet<InputFlag> flags = EnumSet.noneOf(InputFlag.class);
    private List<String> allowedCommands = new ArrayList<>();
    private int attempts = -1;
    private int timeout = -1;

    /**
     * Checks if the given input is valid.
     *
     * @param input the input
     * */
    public abstract boolean isValidInput(@NotNull String input);

    /**
     * Sends, if present, the message of the given type to the player.
     * The following placeholders can be used: {player}, {attempts}
     *
     * @param type the message type
     * @param player the player
     * */
    public final void sendMessage(@NotNull InputMessage type, @NotNull Player player) {
        if (!messages.containsKey(type)) return;
        player.sendMessage(messages.get(type)
                .replace("{player}", player.getName())
                .replace("{attempts}", String.valueOf(attempts)));
    }

    /**
     * Sends, if present, the message of the given type to the player.
     * The following placeholders can be used: {player}, {attempts}, {input}
     *
     * @param type the message type
     * @param player the player
     * @param input the input
     * */
    public final void sendMessage(@NotNull InputMessage type, @NotNull Player player, @NotNull String input) {
        if (!messages.containsKey(type)) return;
        player.sendMessage(messages.get(type)
                .replace("{player}", player.getName())
                .replace("{attempts}", String.valueOf(attempts))
                .replace("{input}", input));
    }

    /**
     * Sets the message for the given message type.
     *
     * @param type the message type
     * @param message the message
     * */
    @NotNull
    public final AquaInput setMessage(@NotNull InputMessage type, @NotNull String message) {
        messages.put(type, message);
        return this;
    }

    /**
     * Checks if the given flag is enabled.
     *
     * @param flag the flag
     * @return whether the flag is or not enabled
     * */
    public final boolean hasFlag(@NotNull InputFlag flag) {
        return flags.contains(flag);
    }

    /**
     * Sets the given flags as enabled.
     *
     * @param flags the flags
     * */
    @NotNull
    public final AquaInput setFlags(@NotNull InputFlag ... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    /**
     * Checks if the given command is allowed to be used.
     *
     * @param command the command
     * @return whether the command is allowed or not
     * */
    public final boolean isCommandAllowed(@NotNull String command) {
        return allowedCommands.contains(command);
    }

    /**
     * Sets the allowed commands to be used.
     *
     * @param allowedCommands the commands
     * */
    @NotNull
    public final AquaInput setAllowedCommands(@NotNull List<String> allowedCommands) {
        this.allowedCommands = allowedCommands;
        return this;
    }

    /**
     * Sets the allowed commands to be used.
     *
     * @param allowedCommands the commands
     * */
    @NotNull
    public final AquaInput setAllowedCommands(@NotNull String ... allowedCommands) {
        this.allowedCommands = Arrays.asList(allowedCommands);
        return this;
    }

    /**
     * Gets the remaining number of attempts the input
     * can be given.
     *
     * @return the remaining number of attempts
     * */
    public final int getAttempts() {
        return attempts;
    }

    /**
     * Sets the remaining number of attempts the input
     * can be given.
     * Any value smaller than 0 will be interpreted
     * as infinite.
     *
     * @param attempts the number of attempts
     * */
    @NotNull
    public final AquaInput setAttempts(int attempts) {
        this.attempts = attempts;
        return this;
    }

    /**
     * Gets the duration after which the input prompt
     * will be cancelled.
     * This duration is in seconds. Any value smaller than
     * 0 will disable the timeout.
     *
     * @return the duration
     * */
    public final int getTimeout() {
        return timeout;
    }

    /**
     * Gets the duration after which the input prompt
     * will be cancelled.
     * This duration is in seconds. Any value smaller than
     * 0 will disable the timeout.
     *
     * @param timeout  the duration
     * */
    @NotNull
    public final AquaInput setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Copies the properties of this object in the given
     * object.
     *
     * @param input the object
     * */
    public final void copyTo(@NotNull AquaInput input) {
        messages.forEach(input::setMessage);

        input.setFlags(flags.toArray(InputFlag[]::new));
        input.setAllowedCommands(allowedCommands.toArray(String[]::new));
        input.setAttempts(attempts);
        input.setTimeout(timeout);
    }
}
