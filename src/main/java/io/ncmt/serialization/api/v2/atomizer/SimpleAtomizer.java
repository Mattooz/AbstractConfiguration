package io.ncmt.serialization.api.v2.atomizer;

import io.ncmt.serialization.api.v2.SerializationNode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleAtomizer<SN extends SerializationNode> implements Atomizer<SN> {

    public final int priority = 0;
    public Collection<Class<?>> supportedTypes = Arrays.asList(new Class<?>[] {Any.class});
    private final Supplier<SN> supplier;

    public SimpleAtomizer(Supplier<SN> supplier) {
        this.supplier = supplier;
    }

    @Override
    public SN atomize(Object object) {
        SN node;

        if(object instanceof Iterable) {
            node = atomizeIterable((Iterable<?>) object);
        } else if (object instanceof Map) {
            node = atomizeDict((Map<?, ?>) object);
        } else
            node = atomizeObject(object);

        return node;
    }

    public SN atomizeObject(Object o) {
        Class<?> clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();

        if (fields.length == 0) return null;

        SN node = supplier.get();

        for (var f : fields) {

            f.setAccessible(true);
            String name = f.getName();
            Object fValue;

            try {
                fValue = f.get(o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }

            if (ATOMIC_TYPES.contains(f.getType())) {
                node.put(name, fValue);
            } else {
                node.put(name, atomize(o));
            }
        }

        return node;
    }

    private SN atomizeIterable(Iterable<?> iterable) {
        SN node = supplier.get();

        iterable.forEach(a -> node.add(atomize(a)));

        return node;
    }

    private SN atomizeDict(Map<?, ?> map) {
        var node = supplier.get();

        map.forEach((key, val) -> {
           var node1 = supplier.get();

           if(key instanceof String) {
               node1.put((String) key, atomize(val));
           } else {
               node1.add(atomize(key));
               node1.add(atomize(val));
           }
           node.add(node1);
        });

        return node;
    }

}
