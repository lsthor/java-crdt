package com.thor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElementCollection<E> {
    private final Map<E, Long> map;

    public ElementCollection() {
        this.map = new ConcurrentHashMap<>();
    }

    public ElementCollection(Map<E, Long> map) {
        this.map = map;
    }

    /*
    This method will replace the timestamp values in the collection if element exists and
    the element timestamp is less than the new timestamp.
     */
    public void add(E element, Long timestamp) {
        map.merge(element, timestamp, (key, value) -> {
            if(value <= timestamp) {
                value = timestamp;
            }
            return value;
        });
    }

    public void remove(E element) {
        map.remove(element);
    }

    public Long getTimestamp(E element) {
        return map.get(element);
    }

    public Stream<Map.Entry<E, Long>> stream() {
        return map.entrySet().stream();
    }

    public ElementCollection<E> merge(ElementCollection<E> anotherAddSet) {
        Map<E, Long> mergedMap = Stream.of(map, anotherAddSet.map).flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
        return new ElementCollection<E>(mergedMap);
    }
}
