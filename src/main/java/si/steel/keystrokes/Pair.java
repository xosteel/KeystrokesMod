package si.steel.keystrokes;

import java.util.*;

public class Pair<K, V> {

    private K key;
    private V value;

    private Pair() {}

    public static <K, V> Pair<K, V> of(K key, V value) {
        Pair<K, V> pair = new Pair<>();
        pair.value = value;
        pair.key = key;
        return pair;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode() * 13) + (value == null ? 0 : value.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof Pair) {
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }

        return false;
    }
}
