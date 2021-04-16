package it.niccolomattei.configuration.impl.reader;

import it.niccolomattei.configuration.api.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Supplier;

public class JSONConfigurationReader implements ConfigurationReader<JSONObject, String> {

    private Printer<JSONObject, String> printer;

    @Override
    public void setPrinter(Printer<JSONObject, String> printer) {
        this.printer = printer;
    }

    @Override
    public <CO extends ConfigurationObject, CL extends ConfigurationList> CO read(byte[] input, Supplier<CO> objectProvider, Supplier<CL> listProvider) {
        String rawJson = new String(input);
        JSONObject object;

        try {
            object = new JSONObject(rawJson);
        } catch (JSONException ex) {
            return null;
        }

        return process(null, objectProvider, listProvider, object, null);
    }

    private <CO extends ConfigurationObject, CL extends ConfigurationList> CO process(ConfigurationElement parent, Supplier<CO> provider,
                                                                                      Supplier<CL> listProvider, JSONObject object, String parentKey) {
        JSONObject section = parentKey != null ? object.getJSONObject(parentKey) : object;
        CO base = provider.get();

        if (parent != null) base.setParent(parent, parentKey);

        for (String key : section.keySet()) {
            Object o = section.get(key);

            if (!(o instanceof JSONObject)) {
                if (o instanceof JSONArray) {
                    base.put(key, processJSONArray(base, provider, listProvider, (JSONArray) o, parentKey));
                } else
                    base.put(key, o);
            } else {
                base.put(key, process(base, provider, listProvider, section, key));
            }
        }

        return base;
    }

    private <CO extends ConfigurationObject, CL extends ConfigurationList> CL processJSONArray(ConfigurationElement parent, Supplier<CO> provider,
                                                                                               Supplier<CL> listProvider, JSONArray object, String parentKey) {
        CL base = listProvider.get();

        if (parent != null) base.setParent(parent, parentKey);

        for(Object o : object) {
            if (!(o instanceof JSONObject)) {
                if (o instanceof JSONArray) {
                    base.put(processJSONArray(base, provider, listProvider, (JSONArray) o, parentKey));
                } else
                    base.put(o);
            } else {
                base.put(process(base, provider, listProvider, (JSONObject) o, null));
            }
        }

        return base;
    }

    @Override
    public byte[] write(ConfigurationObject object) {
        return printer == null ? processToOriginal(object).toString().getBytes() : printer.print(processToOriginal(object)).getBytes();
    }

    @Override
    public byte[] defaultObject() {
        return new JSONObject().toString().getBytes();
    }

    private JSONObject processToOriginal(ConfigurationObject object) {
        JSONObject toOriginal = new JSONObject();

        for (String key : object.keySet()) {
            Object o = object.get(key);
            if (o instanceof String || o instanceof Number) {
                toOriginal.put(key, o);
            } else if (o instanceof ConfigurationObject) {
                toOriginal.put(key, processToOriginal((ConfigurationObject) o));
            } else if (o instanceof ConfigurationList) {
                toOriginal.put(key, processToOriginalArray((ConfigurationList) o));
            } else {
                toOriginal.put(key, String.valueOf(o));
            }
        }

        return toOriginal;
    }

    private JSONArray processToOriginalArray(ConfigurationList list) {
        JSONArray jsonArray = new JSONArray();

        for (Object o : list) {
            if (o instanceof String || o instanceof Number) {
                jsonArray.put(o);
            } else if (o instanceof ConfigurationObject) {
                jsonArray.put(processToOriginal((ConfigurationObject) o));
            } else if (o instanceof ConfigurationList) {
                jsonArray.put(processToOriginalArray((ConfigurationList) o));
            } else {
                jsonArray.put(String.valueOf(o));
            }
        }

        return jsonArray;
    }

}
