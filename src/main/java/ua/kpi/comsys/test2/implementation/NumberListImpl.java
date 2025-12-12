/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package ua.kpi.comsys.test2.implementation;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import ua.kpi.comsys.test2.NumberList;

/**
 * Custom implementation of INumberList interface.
 *
 * Варіант 15:
 * C3=0 -> лінійний двонаправлений список
 * C5=0 -> двійкова система (base=2)
 * додаткова система для changeScale: (C5+1) mod 5 = 1 -> трійкова (base=3)
 * C7=1 -> віднімання (this - arg)
 *
 * @author Майстренко Тимофій Валентинович, ІК-33, ІК-4315
 *
 */
public class NumberListImpl implements NumberList {

    // ===== Variant constants =====
    private static final int BASE_MAIN = 2;   // C5=0
    private static final int BASE_EXTRA = 3;  // (C5+1) mod 5 = 1

    // ===== Internal node =====
    private static final class Node {
        byte value;
        Node prev;
        Node next;

        Node(byte value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private int modCount;

    // Current base of this list (2 by default, 3 for changeScale result)
    private final int base;


    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        this.base = BASE_MAIN;
    }

    private NumberListImpl(int base) {
        this.base = base;
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this.base = BASE_MAIN;

        if (file == null || !file.exists() || !file.isFile()) {
            return; // empty list
        }

        String s;
        try {
            s = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return; // empty list
        }

        s = (s == null) ? "" : s.trim();
        if (s.isEmpty() || !s.matches("\\d+")) {
            return; // empty list
        }

        fromBigInteger(new BigInteger(s, 10), this.base);
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this.base = BASE_MAIN;
        String s = (value == null) ? "" : value.trim();
        if (s.isEmpty() || !s.matches("\\d+")) {
            return; // empty list
        }
        fromBigInteger(new BigInteger(s, 10), this.base);
    }


    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        Objects.requireNonNull(file, "file");
        String dec = toDecimalString();
        try {
            Files.writeString(file.toPath(), dec, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot write file: " + file, e);
        }
    }


    /**
     * Returns student's record book number, which has 4 decimal digits.
     *
     * @return student's record book number.
     */
    public static int getRecordBookNumber() {
        return 4315;
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the same number
     * in other scale of notation, defined by personal test assignment.<p>
     *
     * Does not impact the original list.
     *
     * @return <tt>NumberListImpl</tt> in other scale of notation.
     */
    public NumberListImpl changeScale() {
        BigInteger v = toBigInteger(this.base);
        NumberListImpl res = new NumberListImpl(BASE_EXTRA);
        res.fromBigInteger(v, BASE_EXTRA);
        return res;
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the result of
     * additional operation, defined by personal test assignment.<p>
     *
     * Does not impact the original list.
     *
     * @param arg - second argument of additional operation
     *
     * @return result of additional operation.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        Objects.requireNonNull(arg, "arg");

        int argBase = this.base;
        if (arg instanceof NumberListImpl) {
            argBase = ((NumberListImpl) arg).base;
        }

        BigInteger a = this.toBigInteger(this.base);
        BigInteger b = toBigInteger(arg, argBase);

        if (b.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }

        BigInteger r = a.divide(b); // integer part

        NumberListImpl res = new NumberListImpl(this.base);
        res.fromBigInteger(r, this.base);
        return res;
    }


    /**
     * Returns string representation of number, stored in the list
     * in <b>decimal</b> scale of notation.
     *
     * @return string representation in <b>decimal</b> scale.
     */
    public String toDecimalString() {
       return toBigInteger(this.base).toString(10);
    }


    @Override
    public String toString() {
        if (size == 0) return "";
        StringBuilder sb = new StringBuilder(size);
        for (Node cur = head; cur != null; cur = cur.next) {
            int d = cur.value & 0xFF;
            sb.append(d);
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof List)) return false;

        List<?> other = (List<?>) o;
        if (other.size() != this.size) return false;

        int i = 0;
        for (Node cur = head; cur != null; cur = cur.next) {
            Object ov = other.get(i++);
            if (!(ov instanceof Byte)) return false;
            if (!Objects.equals(cur.value, ov)) return false;
        }
        return true;
    }


    @Override
    public int size() {
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte)) return false;
        byte b = (Byte) o;
        for (Node cur = head; cur != null; cur = cur.next) {
            if (cur.value == b) return true;
        }
        return false;
    }


    @Override
    public Iterator<Byte> iterator() {
        return new Itr();
    }


    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int i = 0;
        for (Node cur = head; cur != null; cur = cur.next) {
            arr[i++] = cur.value;
        }
        return arr;
    }


    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public boolean add(Byte e) {
        ensureDigit(e, base);
        linkLast(e);
        return true;
    }


    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte)) return false;
        byte b = (Byte) o;

        for (Node cur = head; cur != null; cur = cur.next) {
            if (cur.value == b) {
                unlink(cur);
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c, "c");
        for (Object x : c) {
            if (!contains(x)) return false;
        }
        return true;
    }


    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        Objects.requireNonNull(c, "c");
        boolean modified = false;
        for (Byte b : c) {
            ensureDigit(b, base);
            linkLast(b);
            modified = true;
        }
        return modified;
    }


    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        Objects.requireNonNull(c, "c");
        checkPositionIndex(index);
        if (c.isEmpty()) return false;

        Node succ = (index == size) ? null : nodeAt(index);
        Node pred = (succ == null) ? tail : succ.prev;

        for (Byte b : c) {
            ensureDigit(b, base);
            Node nn = new Node(b);
            if (pred == null) {
                head = nn;
            } else {
                pred.next = nn;
                nn.prev = pred;
            }
            pred = nn;
            size++;
            modCount++;
        }

        if (succ == null) {
            tail = pred;
        } else {
            succ.prev = pred;
            pred.next = succ;
        }

        return true;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c, "c");
        boolean modified = false;
        Iterator<Byte> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c, "c");
        boolean modified = false;
        Iterator<Byte> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }


    @Override
    public void clear() {
        Node cur = head;
        while (cur != null) {
            Node nxt = cur.next;
            cur.prev = null;
            cur.next = null;
            cur = nxt;
        }
        head = tail = null;
        size = 0;
        modCount++;
    }


    @Override
    public Byte get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }


    @Override
    public Byte set(int index, Byte element) {
        ensureDigit(element, base);
        checkElementIndex(index);
        Node n = nodeAt(index);
        byte old = n.value;
        n.value = element;
        return old;
    }


    @Override
    public void add(int index, Byte element) {
        ensureDigit(element, base);
        checkPositionIndex(index);
        if (index == size) {
            linkLast(element);
        } else {
            linkBefore(element, nodeAt(index));
        }
    }


    @Override
    public Byte remove(int index) {
        checkElementIndex(index);
        Node n = nodeAt(index);
        byte old = n.value;
        unlink(n);
        return old;
    }


    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        byte b = (Byte) o;
        int idx = 0;
        for (Node cur = head; cur != null; cur = cur.next, idx++) {
            if (cur.value == b) return idx;
        }
        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        byte b = (Byte) o;
        int idx = size - 1;
        for (Node cur = tail; cur != null; cur = cur.prev, idx--) {
            if (cur.value == b) return idx;
        }
        return -1;
    }


    @Override
    public ListIterator<Byte> listIterator() {
        return new ListItr(0);
    }


    @Override
    public ListIterator<Byte> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }


    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        NumberListImpl res = new NumberListImpl(this.base);
        if (fromIndex == toIndex) return res;

        Node cur = nodeAt(fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            res.add(cur.value);
            cur = cur.next;
        }
        return res;
    }


    @Override
    public boolean swap(int index1, int index2) {
        if (index1 == index2) return true;
        if (index1 < 0 || index2 < 0 || index1 >= size || index2 >= size) return false;

        Node n1 = nodeAt(index1);
        Node n2 = nodeAt(index2);

        byte tmp = n1.value;
        n1.value = n2.value;
        n2.value = tmp;
        modCount++;
        return true;
    }


    @Override
    public void sortAscending() {
        if (size < 2) return;

        for (Node i = head.next; i != null; i = i.next) {
            byte key = i.value;
            Node j = i.prev;

            while (j != null && (j.value & 0xFF) > (key & 0xFF)) {
                j.next.value = j.value;
                j = j.prev;
            }

            if (j == null) head.value = key;
            else j.next.value = key;
        }
        modCount++;
    }


    @Override
    public void sortDescending() {
        if (size < 2) return;

        for (Node i = head.next; i != null; i = i.next) {
            byte key = i.value;
            Node j = i.prev;

            while (j != null && (j.value & 0xFF) < (key & 0xFF)) {
                j.next.value = j.value;
                j = j.prev;
            }

            if (j == null) head.value = key;
            else j.next.value = key;
        }
        modCount++;
    }


    @Override
    public void shiftLeft() {
        if (size < 2) return;

        Node first = head;
        Node second = head.next;

        second.prev = null;
        head = second;

        first.next = null;
        first.prev = tail;
        tail.next = first;
        tail = first;

        modCount++;
    }


    @Override
    public void shiftRight() {
        if (size < 2) return;

        Node last = tail;
        Node beforeLast = tail.prev;

        beforeLast.next = null;
        tail = beforeLast;

        last.prev = null;
        last.next = head;
        head.prev = last;
        head = last;

        modCount++;
    }

    
    private static void ensureDigit(Byte b, int base) {
        if (b == null) throw new NullPointerException("Null elements are not allowed");
        int v = b & 0xFF;
        if (v < 0 || v >= base) {
            throw new IllegalArgumentException("Digit out of range for base " + base + ": " + v);
        }
    }

    private void linkLast(byte v) {
        Node nn = new Node(v);
        Node t = tail;
        tail = nn;
        if (t == null) {
            head = nn;
        } else {
            t.next = nn;
            nn.prev = t;
        }
        size++;
        modCount++;
    }

    private void linkBefore(byte v, Node succ) {
        Node pred = succ.prev;
        Node nn = new Node(v);
        nn.next = succ;
        succ.prev = nn;
        nn.prev = pred;
        if (pred == null) {
            head = nn;
        } else {
            pred.next = nn;
        }
        size++;
        modCount++;
    }

    private void unlink(Node n) {
        Node p = n.prev;
        Node x = n.next;

        if (p == null) head = x;
        else p.next = x;

        if (x == null) tail = p;
        else x.prev = p;

        n.prev = null;
        n.next = null;

        size--;
        modCount++;
    }

    private Node nodeAt(int index) {
        if (index < (size >>> 1)) {
            Node cur = head;
            for (int i = 0; i < index; i++) cur = cur.next;
            return cur;
        } else {
            Node cur = tail;
            for (int i = size - 1; i > index; i--) cur = cur.prev;
            return cur;
        }
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
    }

    private static BigInteger parseDecimalStrict(String s) {
        // only non-negative decimal integer
        if (!s.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid decimal number: " + s);
        }
        BigInteger v = new BigInteger(s, 10);
        if (v.signum() < 0) throw new IllegalArgumentException("Negative not allowed");
        return v;
    }

    private BigInteger toBigInteger(int base) {
        if (size == 0) return BigInteger.ZERO;
        BigInteger b = BigInteger.valueOf(base);
        BigInteger v = BigInteger.ZERO;
        for (Node cur = head; cur != null; cur = cur.next) {
            int d = cur.value & 0xFF;
            v = v.multiply(b).add(BigInteger.valueOf(d));
        }
        return v;
    }

    private static BigInteger toBigInteger(NumberList list, int base) {
        if (list.isEmpty()) return BigInteger.ZERO;
        BigInteger b = BigInteger.valueOf(base);
        BigInteger v = BigInteger.ZERO;
        for (Byte x : list) {
            if (x == null) throw new NullPointerException("Null digit in operand list");
            int d = x & 0xFF;
            if (d < 0 || d >= base) {
                throw new IllegalArgumentException("Digit out of range for base " + base + ": " + d);
            }
            v = v.multiply(b).add(BigInteger.valueOf(d));
        }
        return v;
    }

    private void fromBigInteger(BigInteger value, int base) {
        clear();
        if (value == null || value.signum() == 0) {
            // keep empty list as 0
            return;
        }
        if (value.signum() < 0) throw new IllegalArgumentException("Negative not allowed");

        BigInteger b = BigInteger.valueOf(base);

        // collect digits in reverse using temporary primitive array (no collections)
        int cap = 32;
        byte[] tmp = new byte[cap];
        int len = 0;

        BigInteger v = value;
        while (v.signum() > 0) {
            BigInteger[] qr = v.divideAndRemainder(b);
            int d = qr[1].intValue();
            if (len == cap) {
                cap <<= 1;
                byte[] n = new byte[cap];
                System.arraycopy(tmp, 0, n, 0, len);
                tmp = n;
            }
            tmp[len++] = (byte) d;
            v = qr[0];
        }

        // reverse into list (MSD -> LSD)
        for (int i = len - 1; i >= 0; i--) {
            linkLast(tmp[i]);
        }
    }

    // ======================
    // ===== Iterators ======
    // ======================

    private final class Itr implements Iterator<Byte> {
        private Node next = head;
        private Node lastReturned;
        private int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Byte next() {
            checkForComodification();
            if (next == null) throw new NoSuchElementException();
            lastReturned = next;
            next = next.next;
            return lastReturned.value;
        }

        @Override
        public void remove() {
            checkForComodification();
            if (lastReturned == null) throw new IllegalStateException();
            Node lr = lastReturned;
            lastReturned = null;
            unlink(lr);
            expectedModCount = modCount;
        }

        private void checkForComodification() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
        }
    }

    private final class ListItr implements ListIterator<Byte> {
        private Node next;
        private Node lastReturned;
        private int nextIndex;
        private int expectedModCount = modCount;

        ListItr(int index) {
            this.nextIndex = index;
            this.next = (index == size) ? null : nodeAt(index);
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public Byte next() {
            checkForComodification();
            if (!hasNext()) throw new NoSuchElementException();
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.value;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public Byte previous() {
            checkForComodification();
            if (!hasPrevious()) throw new NoSuchElementException();
            next = (next == null) ? tail : next.prev;
            lastReturned = next;
            nextIndex--;
            return lastReturned.value;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            checkForComodification();
            if (lastReturned == null) throw new IllegalStateException();

            Node lr = lastReturned;
            Node ln = lr.next;
            unlink(lr);

            if (next == lr) next = ln;
            else nextIndex--;

            lastReturned = null;
            expectedModCount = modCount;
        }

        @Override
        public void set(Byte e) {
            checkForComodification();
            if (lastReturned == null) throw new IllegalStateException();
            ensureDigit(e, base);
            lastReturned.value = e;
        }

        @Override
        public void add(Byte e) {
            checkForComodification();
            ensureDigit(e, base);

            if (next == null) {
                linkLast(e);
            } else {
                linkBefore(e, next);
            }
            nextIndex++;
            lastReturned = null;
            expectedModCount = modCount;
        }

        private void checkForComodification() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
        }
    }
}
