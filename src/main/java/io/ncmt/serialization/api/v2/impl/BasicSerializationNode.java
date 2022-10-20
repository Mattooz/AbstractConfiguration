package io.ncmt.serialization.api.v2.impl;

import io.ncmt.serialization.api.v2.Configuration;
import io.ncmt.serialization.api.v2.SerializationException;
import io.ncmt.serialization.api.v2.SerializationNode;
import io.ncmt.serialization.api.v2.atomizer.Atomizer;


import java.util.*;

enum Types {
    RECORD,
    ATOMIC,
    ARRAY,
    NULL
}

public class BasicSerializationNode implements SerializationNode {
    private Object atomic = null;
    private List<BasicSerializationNode> nodes = new LinkedList<>();
    private String key = null;
    private Types content;
    private BasicSerializationNode parent;

    private BasicSerializationNode() {
        this.content = Types.NULL;
    }

    @Override
    public <E> E get() {
        if (isAtomic()) {
            return (E) this.atomic;
        } else {
            throw new SerializationException("this element doesn't contain a single node");
        }
    }

    @Override
    public <E> E get(int index) throws SerializationException {
        if (isArray()) {
            return (E) this.nodes.get(index);
        } else {
            throw new SerializationException("this element doesn't  contain an array");
        }
    }

    @Override
    public <E> E get(String key) throws SerializationException {
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
            if (tmp.isAtomic()) expected = (E) tmp.get();
            else expected = (E) tmp;
        } catch (Exception e) {
            throw new SerializationException("Couldn't cast");
        }

        return expected;

    }

    @Override
    public <E> Optional<E> opt() {
        return Optional.empty();
    }

    @Override
    public <E> Optional<E> opt(int index) {
        return Optional.empty();
    }

    @Override
    public <E> Optional<E> opt(String key) {
        return Optional.empty();
    }

    @Override
    public void put(String key, Object val) {
        if (key.isEmpty()) {
            throw new SerializationException("the key is empty");
        }
        if (isRecord() || isNull()) {
            Objects.requireNonNull(key, "Key can't be null");
            if (key.isBlank()) throw new SerializationException("Key is blank");
            var split = Arrays.asList(key.split(Configuration.getPathDelim()));
            if (split.size() == 0) throw new SerializationException("Split didn't work");
            var it = split.iterator();
            var tmp = this;


            while (it.hasNext() && tmp.isRecord()) {
                var tmpKey = it.next();
                BasicSerializationNode tmpNode = tmp.get(tmpKey);

                if(tmpNode == null) {
                    var newNode = new BasicSerializationNode();
                    tmp.put(tmpKey, newNode);
                    newNode.key = tmpKey;
                    newNode.parent = tmp;
                } else if(!tmpNode.isRecord()) {
                    throw new SerializationException("");
                }

                tmp =  tmpNode;
            }

        } else {
            throw new SerializationException("the key is probably wrong");
        }


    }

    @Override
    public void add(Object val) {
        if (isArray() || isNull()) {
            this.nodes.add((BasicSerializationNode) val);
            this.content = Types.ARRAY;
        } else {
            throw new SerializationException("there is already an atomic value in this object or you tried to put a wrong object type");
        }
    }

    @Override
    public void set(Object atomic) {
        if (isNull()) {
            this.atomic = atomic;
            this.content = Types.ATOMIC;
        } else {
            throw new SerializationException("this object already contains other data");
        }
    }

    @Override
    public void remove(Object val) {

    }

    @Override
    public void remove(int index) {

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
    public io.ncmt.serialization.api.v2.SerializationNode empty() {
        return new BasicSerializationNode();
    }

    @Override
    public io.ncmt.serialization.api.v2.SerializationNode parent() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public Iterator<Object> iterator() {
        return null;
    }
}
