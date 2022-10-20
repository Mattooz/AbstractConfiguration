package io.ncmt.serialization.api.v2.impl;

import io.ncmt.serialization.api.v2.SerializationException;
import io.ncmt.serialization.api.v2.atomizer.Atomizer;
import org.w3c.dom.TypeInfo;

import javax.lang.model.type.TypeKind;
import java.net.Proxy;
import java.util.*;

public class SerializationNode implements io.ncmt.serialization.api.v2.SerializationNode {
    private String atomic = null;
    private List<SerializationNode> nodes = new LinkedList<>();
    private String key = null;
    private SerializationNode parent;

    public SerializationNode(String atomic, SerializationNode node, String key) {
        this.atomic = atomic;
        this.nodes.add(node);
        this.key = key;
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
        String split[] = key.split(".");
        SerializationNode temp = this;
        for(int i = 0; i < split.length; i++){
            if(temp.checkKey(split[i]) && temp.isArray()){
                temp = temp.get(split[i+1]);
            }
            else {
                if(temp.isRecord()){
                    temp = temp.get();
                }
            }
        }

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

            this.nodes.add((SerializationNode) val);

        }
    }

    @Override
    public void add(Object val) {
        SerializationNode node = (SerializationNode) val;
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
