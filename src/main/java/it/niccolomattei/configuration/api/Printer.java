package it.niccolomattei.configuration.api;

public interface Printer<I, O> {

    O print(I input);

}
