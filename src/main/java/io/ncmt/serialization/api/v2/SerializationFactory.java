package io.ncmt.serialization.api.v2;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class SerializationFactory<T extends SerializationNode> {

    private final Supplier<T> supplier;
    private final TranslationLayer translator;

    public SerializationFactory(TranslationLayer translator, Supplier<T> supplier) {
        this.supplier = supplier;
        this.translator = translator;
    }

    public T getConfiguration(ResourceHandler handler) {
        byte[] data;

        try {
            data = handler.readFromSource();
        } catch (IOException ex) {
            throw new SerializationException("Couldn't read source! Source doesn't exist!");
        }

        return translator.read(data, supplier);
    }

    public Optional<T> optConfiguration(ResourceHandler handler) {
        try {
            return Optional.of(getConfiguration(handler));
        } catch (SerializationException ex) {
            //do nothing
            return Optional.empty();
        }
    }

    public void write(T root, ResourceHandler handler) {
        try {
            handler.writeToSource(translator.write(root));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeDefault(ResourceHandler handler) {
        try {
            handler.writeToSource(translator.writeDefault());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
