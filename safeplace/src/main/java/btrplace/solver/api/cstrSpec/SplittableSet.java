package btrplace.solver.api.cstrSpec;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class SplittableSet<T> implements Set<T> {

    private List<T> cnt;

    private Set<T> s;

    public SplittableSet(Set<T> s) {
        cnt = new ArrayList<>(s);
        this.s = s;
    }

    @Override
    public int size() {
        return cnt.size();
    }

    @Override
    public boolean isEmpty() {
        return s.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return s.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return cnt.iterator();
    }

    @Override
    public Object[] toArray() {
        return cnt.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
