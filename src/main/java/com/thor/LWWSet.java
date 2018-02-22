package com.thor;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LWWSet<E> {

    /*
    A description on how LWWSet works.

    An alternative LWW-based approach, which we call LWW-element-Set,
    attaches a timestamp to each element (rather than to the whole set).
    Consider add-set A and remove-set R, each containing (element, timestamp) pairs.
    To add (resp. remove) an element e, add the pair (e, now()), where now was specified earlier, to A (resp. to R).
    Merging two replicas takes the union of their add-sets and remove-sets. An element e is in the set if it is
    in A, and it is not in R with a higher timestamp: lookup(e) = ∃ t, ∀ t 0 > t: (e,t) ∈ A ∧ (e,t0) / ∈ R).
    Since it is based on LWW, this data type is convergent.
     */

    private final ElementCollection<E> addSet;
    private final ElementCollection<E> removeSet;

    public LWWSet() {
        this.addSet = new ElementCollection<E>();
        this.removeSet = new ElementCollection<E>();
    }

    public LWWSet(ElementCollection<E> addSet, ElementCollection<E> removeSet) {
        this.addSet = addSet;
        this.removeSet = removeSet;
    }

    public void add(Long timestamp, E payload) {
        addSet.add(payload, timestamp);
    }

    public void remove(Long timestamp, E payload){
        removeSet.add(payload, timestamp);
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("Add Set \n")
                .append(addSet
                        .stream()
                        .map(elem -> "\t" + elem.getKey().toString())
                        .collect(Collectors.joining("\n"))
                );

        sb.append("\nRemove Set \n")
                .append(removeSet
                        .stream()
                        .map(elem ->  "\t" + elem.getKey().toString())
                        .collect(Collectors.joining("\n"))
                );
        return sb.toString();
    }

    public Set<E> lookup() {
        return addSet.stream().filter(entry -> {
            Long timestamp = Optional.ofNullable(removeSet.getTimestamp(entry.getKey()))
                    .orElse(Long.MIN_VALUE);
            return timestamp.compareTo(entry.getValue()) < 1;
        }).map(entry -> entry.getKey()).collect(Collectors.toSet());
    }

    public LWWSet<E> merge(LWWSet<E> anotherSet) {
        return new LWWSet<E>(addSet.merge(anotherSet.addSet), removeSet.merge(removeSet));
    }

    private void log(Object obj) {
        System.out.println(obj.toString());
    }
}
