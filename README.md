# CRDT Sample Code

### Description
This is a simple implementation of LWW-Element-Set in Java 8. The project is build using Gradle.

### Notes
  - The implementation is based on the description in https://github.com/pfrazee/crdt_notes#lww-element-set
  - Some test cases are referring to https://github.com/ajantis/java-crdt/blob/master/src/test/java/io/dmitryivanov/crdt/LWWSetTests.java
  - The same element will only exists once in the set, with latest timestamp paired with it. For example, if element 'A' was added twice, the timestamp with higher value will be kept in the set instead.
  - Elements can be added or removed from the set, only during lookup will return the merged results. In my opinion, there are 2 ways of doing lookup
        1. When adding element or removing element, the code can start doing the filtering and merging
        2. Filtering and merging only happened when lookup is invoked.
    I implemented the first way previously but the code got a bit messy, so i refactored it to use
    second way instead. Reason being when using first way we might need to synchcronize to prevent multiple
    thread from adding/removing element at the same time. Synchronization might create complication.
    And using second way we can create a snapshot of the collection and return the lookup, so any changes
    to the collection during the lookup will not affected the return result.
### Test
To run the test case, go to the code folder and run the following command
`./gradlew test`