package dev.cristike.aquainput.input;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A child class of AquaInput used for the input of a string present
 * in a collection.
 * */
public class StringCollectionInput extends AquaInput {
    private final Collection<String> collection;

    public StringCollectionInput(Collection<String> collection) {
        this.collection = collection;
    }

    @Override
    public boolean isValidInput(@NotNull String input) {
        return collection.contains(input);
    }
}
