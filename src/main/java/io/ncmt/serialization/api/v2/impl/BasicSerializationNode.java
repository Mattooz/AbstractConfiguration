package io.ncmt.serialization.api.v2.impl;

import io.ncmt.serialization.api.v2.Configuration;
import io.ncmt.serialization.api.v2.SerializationException;
import io.ncmt.serialization.api.v2.SerializationNode;

import java.util.*;

public class BasicSerializationNode implements SerializationNode {
    private Object atomic = null;
    private final List<BasicSerializationNode> nodes = new ArrayList<>();
    private String key = null;
    private BasicSerializationNode parent;

    public BasicSerializationNode(String atomic, BasicSerializationNode node, String key) {
        this.atomic = atomic;
        this.nodes.add(node);
        this.key = key;
    }

    @Override
    public Object get() {
        if (isAtomic()) {
            return this.atomic;
        } else {
            throw new SerializationException("this element doesn't contain a single node");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(int index) throws SerializationException {
        if (isArray()) {
            return (E) this.nodes.get(index);
        } else {
            throw new SerializationException("this element doesn't  contain an array");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(String key) throws SerializationException {
        if(!isRecord()) throw new SerializationException("This is not a record");

        Objects.requireNonNull(key, "Key can't be null");
        if(key.isBlank()) throw new SerializationException("Key is blank");

        var split = Arrays.asList(key.split(Configuration.getPathDelim()));

        if(split.size() == 0) throw new SerializationException("Split didn't work");

        var it = split.iterator();

        var tmp = this;
        while(it.hasNext()) {
            var tmpKey = it.next();

            if(it.hasNext() && !tmp.isRecord()) throw new SerializationException("Could not retrieve value at: " + key);

            for(var child : tmp.nodes) {
                if(child.key.equals(tmpKey)) {
                    tmp = child;
                    break;
                }
            }
        }

        E expected;

        try {
            if(tmp.isAtomic()) expected = (E) tmp.get();
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
        if (isArray()) {

            this.nodes.add((BasicSerializationNode) val);

        }
    }

    @Override
    public void add(Object val) {
        BasicSerializationNode node = (BasicSerializationNode) val;
        if (isArray()) {
            this.atomic = val;
        } else {
            throw new SerializationException("there is already an atomic value in this object or you tried to put a wrong object type");
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
        return !this.nodes.isEmpty() || isArray();
    }

    @Override
    public boolean isArray() {
        return this.nodes.size() > 1;
    }

    @Override
    public boolean isAtomic() {
        return !(this.atomic == null);
    }

    @Override
    public boolean isNull() {
        return !(isArray() || isAtomic() || isRecord());
    }

    @Override
    public SerializationNode parent() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public Iterator<Object> iterator() {
        return null;
    }
}
