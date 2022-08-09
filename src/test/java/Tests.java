import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class Tests {

    static <T> List<T> ListOf(T... elems) {
        return Arrays.stream(elems).collect(Collectors.toList());
    }

    static <A, B> void assertEqualsAsSortedStrings(List<List<A>> a, List<List<B>> b) {
        var sa = a.stream().map(List::toString).sorted().toList();
        var sb = b.stream().map(List::toString).sorted().toList();
        Assertions.assertIterableEquals(sa, sb);
    }


    @Test
    public void empty() {
        var sol = new SuperkassaTzSolver<>(List.of()).solve();
        Assertions.assertTrue(sol.isEmpty());
    }

    @Test
    public void emptyEmpty() {
        var sol = new SuperkassaTzSolver<>(List.of(List.of())).solve();
        Assertions.assertTrue(sol.isEmpty());
    }

    @Test
    public void one() {
        var sol = new SuperkassaTzSolver<>(ListOf(ListOf(1))).solve();
        var expected = ListOf(ListOf(1));
        Assertions.assertIterableEquals(expected, sol);
    }

    @Test
    public void noSolution() {
        var lines = ListOf(
                ListOf(null, 1),
                ListOf(null, 2));
        var sol = new SuperkassaTzSolver<>(lines).solve();
        Assertions.assertTrue(sol.isEmpty());
    }


    @Test
    public void threeAndEmpty() {
        List<List<Integer>> lines = List.of(
                ListOf(1, null, null),
                ListOf(null, 2, null),
                ListOf(null, null, 3),
                ListOf(null, null, null)
        );
        var expected = ListOf(ListOf(1, 2, 3));
        var sol = new SuperkassaTzSolver<>(lines).solve();
        assertEqualsAsSortedStrings(expected, sol);
    }

    @Test
    public void exampleNo1() {
        var lines = ListOf(
                ListOf("a1", "a2", "a3", "a4"),
                ListOf("b1", null, null, "b4"),
                ListOf(null, "c2", "c3", null),
                ListOf("d1", null, null, "d4"),
                ListOf(null, "e2", "e3", null),
                ListOf(null, "f2", "f3", "f4"),
                ListOf("h1", null, null, null),
                ListOf("g1", null, null, null));
        var expected = ListOf(
                ListOf("g1", "f2", "f3", "f4"),
                ListOf("h1", "f2", "f3", "f4"),
                ListOf("d1", "e2", "e3", "d4"),
                ListOf("d1", "c2", "c3", "d4"),
                ListOf("b1", "e2", "e3", "b4"),
                ListOf("b1", "c2", "c3", "b4"),
                ListOf("a1", "a2", "a3", "a4"));
        var sol = new SuperkassaTzSolver<>(lines).solve();
        assertEqualsAsSortedStrings(expected, sol);
    }

    @Test
    public void multi() {
        List<List<String>> lines = ListOf(
                ListOf("a1", "a2", null, null),
                ListOf("b1", null, null, null),
                ListOf(null, null, "c3", null),
                ListOf(null, null, null, "d4"),
                ListOf(null, "f2", "f3", null),
                ListOf(null, null, null, null),
                ListOf(null, null, null, "e4"));
        var expected = ListOf(
                ListOf("a1", "a2", "c3", "d4"),
                ListOf("a1", "a2", "c3", "e4"),
                ListOf("b1", "f2", "f3", "d4"),
                ListOf("b1", "f2", "f3", "e4"));
        var sol = new SuperkassaTzSolver<>(lines).solve();
        assertEqualsAsSortedStrings(expected, sol);
    }
}
