package io.ncmt.serialization.api.v2.impl;

import io.ncmt.serialization.api.v2.Configuration;
import io.ncmt.serialization.api.v2.SerializationException;
import io.ncmt.serialization.api.v2.SerializationNode;


import java.util.*;


@SuppressWarnings("unchecked")
public class BasicSerializationNode implements SerializationNode {

    enum Types {
        RECORD,
        ATOMIC,
        ARRAY,
        NULL
    }

    private Object atomic = null;
    private List<BasicSerializationNode> nodes = new LinkedList<>();
    private String key = null;
    private Types content;
    private BasicSerializationNode parent;

    private BasicSerializationNode() {
        this.content = Types.NULL;
    }

    @Override
    public <E> E get(Class<E> eClass) {
        if (isAtomic()) {
            E res;
            try {
                res = eClass.cast(this.atomic);
            } catch (ClassCastException exception) {
                throw new SerializationException("Couldn't cast to desired type");
            }
            return res;
        } else {
            throw new SerializationException("This node is not atomic");
        }
    }

    @Override
    public <E> E get(int index, Class<E> eClass) throws SerializationException {
        if (isArray()) {
            var at = this.nodes.get(index);
            E res;
            try {
                if (at.isAtomic()) {
                    res = at.get(eClass);
                } else
                    res = eClass.cast(at);
            } catch (ClassCastException e) {
                throw new SerializationException("Couldn't cast to desired type");
            }
            return res;
        } else {
            throw new SerializationException("this node doesn't contain an array");
        }
    }

    @Override
    public <E> E get(String key, Class<E> eClass) throws SerializationException {
        if (!isRecord()) throw new SerializationException("This is not a record");

        Objects.requireNonNull(key, "Key can't be null");
        if (key.isBlank()) throw new SerializationException("Key is blank");

        var split = Arrays.asList(key.split(Configuration.getPathDelim()));

        if (split.size() == 0) throw new SerializationException("Split didn't work");

        var it = split.iterator();

        var tmp = this;
        while (it.hasNext()) {
            var tmpKey = it.next();

            if (it.hasNext() && !tmp.isRecord())
                throw new SerializationException("Could not retrieve value at: " + key);

            for (var child : tmp.nodes) {
                if (child.key.equals(tmpKey)) {
                    tmp = child;
                    break;
                }
            }
        }

        E expected;

        try {
            if (tmp.isAtomic()) expected = tmp.get(eClass);
            else expected = eClass.cast(tmp);
        } catch (ClassCastException e) {
            throw new SerializationException("Couldn't cast");
        }

        return expected;
    }

    @Override
    public <E> Optional<E> opt(Class<E> eClass) {
        try {
            return Optional.of(get(eClass));
        } catch (ClassCastException exception) {
            return Optional.empty();
        }
    }

    @Override
    public <E> Optional<E> opt(int index, Class<E> eClass) {
        try {
            return Optional.of(get(index, eClass));
        } catch (ClassCastException exception) {
            return Optional.empty();
        }
    }

    @Override
    public <E> Optional<E> opt(String key, Class<E> eClass) {
        try {
            return Optional.of(get(key, eClass));
        } catch (ClassCastException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object val) {
        if (!isRecord()) throw new SerializationException("This is not a record");
        this.content = Types.RECORD;

        Objects.requireNonNull(key, "Key can't be null");
        if (key.isBlank()) throw new SerializationException("Key is blank");

        var split = Arrays.asList(key.split(Configuration.getPathDelim()));

        if (split.size() == 0) throw new SerializationException("Split didn't work");

        var currentNode = this;
        for (var i = 0; i < split.size() - 1; i++) {
            var tmpKey = split.get(i);
            var tmpNode = currentNode.opt(tmpKey, BasicSerializationNode.class).orElse(new BasicSerializationNode());

            if(tmpNode.isNull())
                currentNode.put(tmpKey, tmpNode);
            currentNode = tmpNode;
        }


        var newNode = of(val);
        newNode.key = key;
        newNode.parent = parent;
        currentNode.nodes.add(newNode);
    }

    @Override
    public void add(Object val) {
        if (isNull() || isArray()) {
            var newNode = of(val);
            newNode.parent = this;
            this.nodes.add(newNode);
            this.content = Types.ARRAY;
        } else {
            throw new SerializationException("there is already an atomic value in this object or you tried to put a wrong object type");
        }
    }

    @Override
    public void set(Object atomic) {
        if (isNull() || isAtomic()) {
            this.atomic = atomic;
            this.content = Types.ATOMIC;
        } else {
            throw new SerializationException("this object already contains other data");
        }
    }

    @Override
    @Deprecated
    public void remove(Object val) {
        for (var node : this.nodes) {
        }
    }

    @Override
    public void remove(int index) {
        this.nodes.remove(index);
    }

    @Override
    public boolean checkKey(String key) {
        return (Objects.equals(this.key, key));
    }

    @Override
    public boolean isRecord() {
        return this.content == Types.RECORD;
    }

    @Override
    public boolean isArray() {
        return this.content == Types.ARRAY;
    }

    @Override
    public boolean isAtomic() {
        return this.content == Types.ATOMIC;
    }

    @Override
    public boolean isNull() {
        return this.content == Types.NULL;
    }

    @Override
    public BasicSerializationNode parent() {
        return this.parent;
    }

    @Override
    public String key() {
        if(this.parent != null && this.parent.isArray()) {
            return "[" + this.parent.nodes.indexOf(this) + "]";
        }
        return this.key;
    }

    @Override
    public String path() {
        List<BasicSerializationNode> parents = new ArrayList<>();

        var parent = this;
        while (this.parent != null) {
            parents.add(parent);
            parent = parent.parent;
        }

        Collections.reverse(parents);

        var strBld = new StringBuilder();
        parents.forEach(key -> strBld.append(key).append("."));
        return strBld.substring(0, strBld.length() - 2);
    }

    @Override
    public Iterator<Object> iterator() {
        Collection<Object> objects = new ArrayList<>();
        this.nodes.forEach(node -> {
            if(node.isAtomic()) {
                objects.add(node.get(Object.class));
            } else objects.add(node);
        });

        return objects.iterator();
    }

    public static BasicSerializationNode of(Object val) {
        if (!(val instanceof BasicSerializationNode)) {
            var newNode = new BasicSerializationNode();
            if (ATOMIC_TYPES.contains(val.getClass())) {
                newNode.set(val);
            } else if (ARRAY_ATOMIC_TYPES.contains(val.getClass())) {
                var array = (Object[]) val;
                for (Object o : array) {
                    newNode.add(o);
                }
            } else {
                var toString = val.toString();
                newNode.set(toString);
            }
            return newNode;
        } else return (BasicSerializationNode) val;
    }
}
