package com.thor;

import java.util.Collections;
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

    public Map<E, Long> snapshot(){
        return Collections.unmodifiableMap(map);
    }

    public Stream<Map.Entry<E, Long>> stream() {
        return map.entrySet().stream();
    }

    /*
    When merging 2 maps, it will keep the element with the higher timestamp instead.
    Some implementation will keep both element because they are using timestamp as the key,
    for my case i think using the value of element as key is sufficient. Plus it will reduce the
    processing time, from o(n) to o(log n),because when doing lookup it doesnt have to traverse
    every element it has(including duplicated element).
     */
    public ElementCollection<E> merge(ElementCollection<E> anotherAddSet) {
        Map<E, Long> mergedMap = Stream.of(map, anotherAddSet.map).flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
        return new ElementCollection<E>(mergedMap);
    }
}
