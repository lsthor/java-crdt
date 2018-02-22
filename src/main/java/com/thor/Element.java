package com.thor;

import java.util.Comparator;

public class Element<E> implements Comparable<Element> {
    private final Long id;
    private final E e;

    public Element(Long id, E e) {
        assert id != null;

        this.id = id;
        this.e = e;
    }

    public Long getId() {
        return id;
    }

    public E getE() {
        return e;
    }

    @Override
    public int compareTo(Element o) {
        return o.getId().compareTo(this.getId());
    }

    @Override
    public String toString() {
        return "Element{" +
                "id=" + id +
                ", e=" + e +
                '}';
    }
}
