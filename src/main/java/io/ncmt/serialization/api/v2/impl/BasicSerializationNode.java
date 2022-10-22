package io.ncmt.serialization.api.v2.impl;

import io.ncmt.serialization.api.v2.Configuration;
import io.ncmt.serialization.api.v2.SerializationException;
import io.ncmt.serialization.api.v2.SerializationNode;


import java.io.StringWriter;
import java.util.*;
import java.util.function.ObjIntConsumer;


@SuppressWarnings("unchecked")
public class BasicSerializationNode implements SerializationNode {

    enum Types {
        RECORD,
        ATOMIC,
        ARRAY,
        NULL
    }

    private Object atomic = null;
    private List<BasicSerializationNode> nodes = new ArrayList<>();
    private String key = null;
    private Types content;
    private BasicSerializationNode parent;

    public BasicSerializationNode() {
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
        if (!isRecord() && !isNull()) throw new SerializationException("This is not a record");

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

            var found = false;
            for (var child : tmp.nodes) {
                if (child.key.equals(tmpKey)) {
                    tmp = child;
                    found = true;
                    break;
                }
            }
            if (!found)
                throw new SerializationException("Couldn't find node at: " + key + "; Subkey \"" + tmpKey + "\" doesn't exist!");
        }

        E expected;

        try {
            if (tmp.isAtomic() && eClass != BasicSerializationNode.class) expected = tmp.get(eClass);
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
        } catch (ClassCastException | SerializationException exception) {
            return Optional.empty();
        }
    }

    @Override
    public <E> Optional<E> opt(int index, Class<E> eClass) {
        try {
            return Optional.of(get(index, eClass));
        } catch (ClassCastException | SerializationException exception) {
            return Optional.empty();
        }
    }

    @Override
    public <E> Optional<E> opt(String key, Class<E> eClass) {
        try {
            return Optional.of(get(key, eClass));
        } catch (ClassCastException | SerializationException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object val) {
        if (!isRecord() && !isNull()) throw new SerializationException("This is not a record");
        this.content = Types.RECORD;

        Objects.requireNonNull(key, "Key can't be null");
        if (key.isBlank()) throw new SerializationException("Key is blank");

        var split = Arrays.asList(key.split(Configuration.getPathDelim()));

        if (split.size() == 0) throw new SerializationException("Split didn't work");

        var currentNode = this;
        for (var i = 0; i < split.size() - 1; i++) {
            var tmpKey = split.get(i);
            var tmpNode = currentNode.opt(tmpKey, BasicSerializationNode.class).orElse(new BasicSerializationNode());

            if (tmpNode.isNull())
                currentNode.put(tmpKey, tmpNode);
            currentNode = tmpNode;
        }
        currentNode.content = Types.RECORD;

        var newNode = of(val);
        newNode.key = split.get(split.size() - 1);
        newNode.parent = currentNode;

        var tmpNode = currentNode.opt(split.get(split.size() - 1), BasicSerializationNode.class).orElse(null);
        if (tmpNode == null) currentNode.nodes.add(newNode);
        else currentNode.nodes.set(currentNode.nodes.indexOf(tmpNode), newNode);

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
    public boolean isRoot() {
        return this.parent == null && this.key == null;
    }

    @Override
    public BasicSerializationNode parent() {
        return this.parent;
    }

    @Override
    public String key() {
        if (this.parent != null && this.parent.isArray()) {
            return "[" + this.parent.nodes.indexOf(this) + "]";
        }
        return this.key == null ? "root" : this.key;
    }

    @Override
    public String path() {
        List<BasicSerializationNode> parents = new ArrayList<>();

        var parent = this;
        while (parent.parent != null) {
            parents.add(parent);
            parent = parent.parent;
        }

        Collections.reverse(parents);

        var strBld = new StringBuilder();
        parents.forEach(p -> strBld.append(p.key()).append("."));
        return strBld.substring(0, strBld.length() - 1);
    }

    @Override
    public Iterator<Object> iterator() {
        Collection<Object> objects = new ArrayList<>();
        this.nodes.forEach(node -> {
            if (node.isAtomic()) {
                objects.add(node.get(Object.class));
            } else objects.add(node);
        });

        return objects.iterator();
    }

    public int totalTreeSize() {
        return 1 + nodes
                .stream()
                .mapToInt(BasicSerializationNode::totalTreeSize)
                .sum();
    }

    public String treeView() {
        /*
         * EXAMPLE
         * root
         * |
         * \--> chiave
         * |     |
         * |     \--> chiave2 = "string"
         * |
         * \--> chiave2 = 14
         */
        final var bld = new StringBuilder();

        bld.append(isRoot() ? "root" : key());
        if (isAtomic() || isNull()) {
            bld.append(" = ").append(this.atomic);
            return bld.toString();
        }
        bld.append("\n");

        var it = nodes.iterator();
        while(it.hasNext()) {
            var node = it.next();
            bld.append("|").append("\n");
            var split = Arrays.asList(node.treeView().split("\n")).iterator();
            bld.append("\\--> ").append(split.next()).append("\n");
            split.forEachRemaining(line -> {
                if (it.hasNext()) bld.append("|    ");
                else bld.append("     ");
                bld.append(line).append("\n");
            });
        }

        return bld.toString();
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
