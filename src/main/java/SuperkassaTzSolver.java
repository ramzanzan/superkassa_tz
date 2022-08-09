import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.stream.IntStream;

public class SuperkassaTzSolver<T> {


    public static void main(String[] args) {

    }

    public SuperkassaTzSolver(List<List<T>> lines) {
        fillingsToLines = new HashMap<>();
        List<T> prevLine = null;
        for (var line : lines) {
            if (prevLine != null && line.size() != prevLine.size()) throw new IllegalArgumentException("all lines must have equal size");
            prevLine = line;
            var lf = new ListFilling(line);
            if (lf.isEmpty()) continue;
            var lineList = fillingsToLines.getOrDefault(lf, new LinkedList<>());
            lineList.add(line);
            fillingsToLines.put(lf, lineList);
        }
        fillings = new ArrayList<>(fillingsToLines.keySet());
    }

    ArrayList<ListFilling> fillings;
    Map<ListFilling, List<List<T>>> fillingsToLines;
    int lineSize;

    public List<List<T>> solve() {
        if (fillings.isEmpty()) return List.of();
        lineSize = fillings.get(0).size;
        var full = ListFilling.getComplete(lineSize);
        var parts = findParts(full, 0);
        return buildResult(parts);
    }

    List<List<T>> buildResult(List<List<ListFilling>> parts) {
        var solution = new LinkedList<List<T>>();
        List<T> emptyLine = Collections.nCopies(lineSize, null);
        parts.stream().parallel().forEach(listOfParts -> {
            var listOfListsOfSimilarLines = listOfParts.stream().map(lf -> fillingsToLines.get(lf)).toList();
            var localSolution = new LinkedList<List<T>>();
            getCombinations(listOfListsOfSimilarLines, 0, localSolution, new ArrayList<>(emptyLine));
            synchronized (solution) {
                solution.addAll(localSolution);
            }
        });
        return solution;
    }

    void getCombinations(List<List<List<T>>> groups, int currGroup, List<List<T>> combinations, ArrayList<T> currentLine) {
        if (currGroup == groups.size()) {
            combinations.add(List.copyOf(currentLine));
            return;
        }
        var group = groups.get(currGroup);
        for (var line : group) {
            var i = 0;
            for (var elem : line) {
                if (elem != null) {
                    currentLine.set(i, elem);
                }
                i++;
            }
            getCombinations(groups, currGroup + 1, combinations, currentLine);
        }
    }

    Map<ListFilling, List<List<ListFilling>>> memoized = new HashMap<>();

    List<List<ListFilling>> findParts(ListFilling target, int fillingIdx) {
        if (memoized.containsKey(target)) {
            return memoized.get(target);
        }
        if (target.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        if (fillingIdx == fillings.size()) {
            return null;
        }
        ListFilling part = fillings.get(fillingIdx);
        List<List<ListFilling>> listOfListsOfParts = null;
        if (part.isPartOf(target)) {
            if (target.isEmptyWithSubtraction(part)) {
                listOfListsOfParts = new LinkedList<>();
                listOfListsOfParts.add(List.of(part));
            } else {
                var nextParts = findParts(target.subtract(part), fillingIdx + 1);
                if (nextParts != null) {
                    listOfListsOfParts = new LinkedList<>();
                    for(var parts : nextParts) {
                        List<ListFilling> newParts = new ArrayList<>(parts.size() + 1);
                        newParts.addAll(parts);
                        newParts.add(part);
                        listOfListsOfParts.add(newParts);
                    }
                }
            }
        }
        var moreParts = findParts(target, fillingIdx + 1);
        if (moreParts != null) {
            if (listOfListsOfParts != null)
                listOfListsOfParts.addAll(moreParts);
            else
                listOfListsOfParts = moreParts;
        }
        if (listOfListsOfParts != null) {
            memoized.put(target, listOfListsOfParts);
        }
        return listOfListsOfParts;
    }


    @EqualsAndHashCode
    static class ListFilling {

        static final long FULL = 0xffff_ffff_ffff_ffffL;

        static ListFilling getEmpty(int size) {
            return new ListFilling(size);
        }

        static ListFilling getComplete(int size) {
            var lf = new ListFilling(size);
            for (int i = 0; i < lf.filling.length - 1; i++)
                lf.filling[i] = FULL;
            lf.filling[lf.filling.length - 1] = getLastLongFilled(size);
            return lf;
        }

        static long getLastLongFilled(int size) {
            return (FULL << (size % Long.SIZE)) ^ FULL;
        }

        final int size;
        final long[] filling;

        ListFilling(int listSize) {
            this.size = listSize;
            filling = new long[listSize / Long.SIZE + (int) Math.signum(listSize % Long.SIZE)];
        }

        ListFilling(List<?> list) {
            this(list.size());
            long bit = 1;
            int i = 0;
            for (var elem : list) {
                if (elem != null) {
                    filling[i++ / Long.SIZE] ^= bit;
                }
                bit <<= 1;
            }
        }

        boolean isEmpty() {
            for (long l : filling) if (l != 0) return false;
            return true;
        }

        boolean isEmptyWithSubtraction(ListFilling other) {
            var lastIdx = filling.length - 1;
            for (int i = 0; i < lastIdx; i++)
                if ((filling[i] ^ other.filling[i]) != 0) return false;
            return (filling[lastIdx] ^ other.filling[lastIdx]) == 0;
        }

        boolean isPartOf(ListFilling other) {
            for (int i = 0; i < filling.length; i++)
                if (filling[i] != (filling[i] & other.filling[i])) return false;
            return true;
        }

        ListFilling subtract(ListFilling other) {
            var res = new ListFilling(size);
            for (int i = 0; i < filling.length; i++)
                res.filling[i] = filling[i] & (~other.filling[i]);
            return res;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < filling.length - 1; i++)
                sb.append(String.format("%s64", Long.toBinaryString(filling[i])).replace(' ', '0'));
            sb.append(Long.toBinaryString(filling[filling.length - 1]));
            sb.reverse();
            return sb.toString();
        }
    }
}
