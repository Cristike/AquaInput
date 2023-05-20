package dev.cristike.aquainput.input;

import org.jetbrains.annotations.NotNull;

/**
 * A child class of AquaInput used for the input of non-empty strings.
 * */
public class StringInput extends AquaInput {

    @Override
    public boolean isValidInput(@NotNull String input) {
        return !input.isEmpty();
    }
}
