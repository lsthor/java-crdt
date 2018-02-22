package com.thor;

import java.util.*;
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


    /*
    In my opinion, there are 2 ways of doing lookup
    1. When adding element or removing element, the code can start doing the filtering and merging
    2. Filtering and merging only happened when lookup is invoked.
    I implemented the first way previously but the code got a bit messy, so i refactored it to use
    second way instead. Reason being when using first way we might need to synchcronize to prevent multiple
    thread from adding/removing element at the same time. Synchronization might create complication.
    And using second way we can create a snapshot of the collection and return the lookup, so any changes
    to the collection during the lookup will not affected the return result.
     */
    public Set<E> lookup() {
        final Map<E, Long> addSetSnapshot = addSet.snapshot();
        final Map<E, Long> removeSetSnapshot = removeSet.snapshot();

        return addSetSnapshot.entrySet().stream().filter(entry -> {
            Long timestamp = Optional.ofNullable(removeSetSnapshot.get(entry.getKey()))
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
