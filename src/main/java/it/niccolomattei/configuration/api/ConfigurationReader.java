package it.niccolomattei.configuration.api;

import java.util.function.Supplier;

public interface ConfigurationReader<T, I> {

    void setPrinter(Printer<T, I> printer);

    <CO extends ConfigurationObject, CL extends ConfigurationList> CO read(byte[] input, Supplier<CO> objectProvider, Supplier<CL> listProvider);
    byte[] write(ConfigurationObject object);
    byte[] defaultObject();

}
