package com.thor;

import org.junit.Assert.*;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LWWSetTest {

    @Test
    public void testAdd() {
        LWWSet<String> set = new LWWSet<>();
        set.add(timestamp(), "Add Line [1,2]");
        set.add(timestamp(), "Add Line [2,3]");
        assertEquals(2, set.lookup().size());
    }

    @Test
    public void testAddAndRemove() {
        LWWSet<String> set = new LWWSet<>();
        set.add(timestamp(), "Add Line [1,2]");
        set.add(timestamp(), "Add Line [2,3]");
        set.remove(timestamp(), "Add Line [2,3]");

        assertEquals(1, set.lookup().size());
    }


    @Test
    public void testAddAndRemoveScenario2() {
        final LWWSet<String> lwwSet = new LWWSet<>();

        lwwSet.add(1L, "dog");
        lwwSet.add(1L, "cat");
        lwwSet.add(1L, "ape");
        lwwSet.add(1L, "tiger");

        lwwSet.remove(2L, "cat");
        lwwSet.remove(2L, "dog");

        // Actual test
        final Set<String> lookup = lwwSet.lookup();

        assertTrue(lookup.size() == 2);
        assertTrue(lookup.contains("ape"));
        assertTrue(lookup.contains("tiger"));
    }

    @Test
    public void testMerge() {
        final LWWSet<String> firstLwwSet = new LWWSet<>();
        firstLwwSet.add(3L, "ape");
        firstLwwSet.add(1L, "dog");
        firstLwwSet.add(1L, "cat");
        firstLwwSet.remove(2L, "cat");

        final LWWSet<String> secondLwwSet = new LWWSet<>();
        secondLwwSet.add(1L, "ape");
        secondLwwSet.add(1L, "tiger");
        secondLwwSet.add(1L, "cat");
        secondLwwSet.remove(2L, "ape");

        // Actual test
        final LWWSet<String> resultSet = firstLwwSet.merge(secondLwwSet);
        Set<String> lookup = resultSet.lookup();
        assertEquals(lookup.size(), 3);
        assertTrue(lookup.contains("ape"));
        assertTrue(lookup.contains("dog"));
        assertTrue(lookup.contains("tiger"));
    }

    private Long timestamp() {
        return System.nanoTime();
    }
}
