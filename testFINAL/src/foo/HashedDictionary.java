package foo;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HashedDictionary<K, V> implements DictionaryInterface<K, V>
{
    private int numberOfEntries;
    private static final int DEFAULT_CAPACITY = 5;
    private static final int MAX_CAPACITY = 100;

    private TableEntry<K, V>[] hashTable;
    private int tableSize;
    private static final int MAX_SIZE = 2 * MAX_CAPACITY;
    private boolean initialized = false;
    private static final double MAX_LOAD_FACTOR = 0.5;

    public HashedDictionary()
    {
        this(DEFAULT_CAPACITY);
    }

    public HashedDictionary(int initialCapacity)
    {
        checkCapacity(initialCapacity);
        numberOfEntries = 0;

        int tableSize = getNextPrime(initialCapacity);
        checkSize(tableSize);

        @SuppressWarnings("unchecked")
        TableEntry<K, V>[] temp = (TableEntry<K, V>[])new TableEntry[tableSize];
        hashTable = temp;
        initialized = true;
    }

    public void displayHashTable()
    {
        checkInitialization();

        for (int index = 0; index < hashTable.length; index++)
        {
            if (hashTable[index] == null) {
                System.out.println("null");
            } else if (hashTable[index].isRemoved()) {
                System.out.println("removed state");
            } else {
                System.out.println(hashTable[index].getKey() + " " + hashTable[index].getValue());
            }
        }
        System.out.println();
    }

    public void displayValidEntries()
    {
        checkInitialization();

        for (int index = 0; index < hashTable.length; index++)
        {
            if (hashTable[index] == null) {
               // System.out.println("null");
            } else if (hashTable[index].isRemoved()) {
               // System.out.println("removed state");
            } else {
                System.out.println(hashTable[index].getKey() + " " + hashTable[index].getValue());
            }
        }
        System.out.println();
    }

    public V add(K key, V value)
    {
        checkInitialization();
        if ((key == null) || (value == null))
            throw new IllegalArgumentException();
        else
        {
            V oldValue;
            int index = getHashIndex(key);
            index = probe(index, key);
            assert (index >= 0) && (index < hashTable.length);
            if ((hashTable[index] == null) // not found
            		|| hashTable[index].isRemoved()) // slot was removed, ready for reuse
            {
                hashTable[index] = new TableEntry<>(key, value);
                numberOfEntries++;
                oldValue = null;
            }
            else // already in use
            {
                oldValue = hashTable[index].getValue();
                hashTable[index].setValue(value);
            }

            if (isHashTableTooFull())
                enlargeHashTable();
            
            return oldValue;
        }
    }

    public V remove(K key)
    {
        checkInitialization();
        V removedValue = null;

        int index = getHashIndex(key);
        index = locate(index, key);

        if (index != -1)
        {
            removedValue = hashTable[index].getValue();
            hashTable[index].setToRemoved();
            numberOfEntries--;
        }
        return removedValue;
    }

    public V getValue(K key)
    {
        checkInitialization();
        V result = null;

        int index = getHashIndex(key);
        index = locate(index, key);

        if (index != -1)
            result = hashTable[index].getValue();
        
        return result;
    }

    public boolean contains(K key)
    {
        return getValue(key) != null;
    }

    public boolean isEmpty()
    {
        return numberOfEntries == 0;
    }

    public int getSize()
    {
        return numberOfEntries;
    }

    public final void clear()
    {
        numberOfEntries = 0;
    }

    public Iterator<K> getKeyIterator()
    {
        return new KeyIterator();
    }

    public Iterator<V> getValueIterator()
    {
        return new ValueIterator();
    }

    private int getHashIndex(K key)
    {
        int hashIndex = key.hashCode() % hashTable.length;
        if (hashIndex < 0)
        {
            hashIndex = hashIndex + hashTable.length;
        }
        return hashIndex;
    }

    private int probe(int index, K key)
    {
        boolean found = false;
        int removedStateIndex = -1;
        while (!found && (hashTable[index] != null))
        {
            if (hashTable[index].isIn())
            {
                if (key.equals(hashTable[index].getKey()))
                    found = true;
                else
                    index = (index + 1) % hashTable.length;
            }
            else
            {
                if (removedStateIndex == -1)
                    removedStateIndex = index;
                index = (index + 1) % hashTable.length;
            }
        }
        if (found || (removedStateIndex == -1))
            return index;
        else
            return removedStateIndex;
    }

    private int locate(int index, K key)
    {
        boolean found = false;
        while (!found && (hashTable[index] != null))
        {
            if (hashTable[index].isIn() && key.equals(hashTable[index].getKey()))
                found = true;
            else
                index = (index + 1) % hashTable.length;
        }

        int result = -1;
        if (found)
            result = index;
        return result;
    }

    private void enlargeHashTable()
    {
        TableEntry<K, V>[] oldTable = hashTable;
        int oldSize = hashTable.length;
        int newSize = getNextPrime(oldSize + oldSize);

        @SuppressWarnings("unchecked")
        TableEntry<K, V>[] temp = (TableEntry<K, V>[]) new TableEntry[newSize];
        hashTable = temp;
        numberOfEntries = 0;

        for (int index = 0; index < oldSize; index++)
        {
            if ((oldTable[index] != null) && oldTable[index].isIn())
                add(oldTable[index].getKey(), oldTable[index].getValue());
        }
    }

    private boolean isHashTableTooFull()
    {
        return numberOfEntries > MAX_LOAD_FACTOR * hashTable.length;
    }

    private int getNextPrime(int integer)
    {
        if (integer % 2 == 0)
        {
            integer++;
        }

        while (!isPrime(integer))
        {
            integer = integer + 2;
        }
        return integer;
    }

    private boolean isPrime(int integer)
    {
        boolean result;
        boolean done = false;

        if ((integer == 1) || (integer % 2 == 0))
        {
            result = false;
        }
        else if ((integer == 2) || (integer == 3))
        {
            result = true;
        }
        else
        {
            assert (integer % 2 != 0) && (integer >= 5);
            result = true;

            for (int divisor = 3; !done && (divisor * divisor <= integer); divisor = divisor + 2)
            {
                if (integer % divisor == 0)
                {
                    result = false;
                    done = true;
                }
            }
        }
        return result;
    }

    private void checkInitialization()
    {
        if (!initialized)
            throw new SecurityException("HashedDictionary object is not initialized properly");
    }

    private void checkCapacity(int capacity)
    {
        if (capacity < DEFAULT_CAPACITY)
            capacity = DEFAULT_CAPACITY;
        else if (capacity > MAX_CAPACITY)
            throw new IllegalStateException("Attempt to create a dictionary " +
                                            "whose capacity is larger than " + 
                                            MAX_CAPACITY);
    }

    private void checkSize(int size)
    {
        if (tableSize > MAX_SIZE)
            throw new IllegalStateException("Dictionary has become too large");
    }

    private class KeyIterator implements Iterator<K>
    {
        private int currentIndex;
        private int numberLeft;
        
        private KeyIterator()
        {
            currentIndex = 0;
            numberLeft = numberOfEntries;
        }

        public boolean hasNext()
        {
            return numberLeft > 0;
        }

        public K next()
        {
            K result = null;

            if (hasNext())
            {
                while ((hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved())
                {
                    currentIndex++;
                }

                result = hashTable[currentIndex].getKey();
                numberLeft--;
                currentIndex++;
            }
            else
                throw new NoSuchElementException();

            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    private class ValueIterator implements Iterator<V>
    {
        private int currentIndex;
        private int numberLeft;
        
        private ValueIterator()
        {
            currentIndex = 0;
            numberLeft = numberOfEntries;
        }

        public boolean hasNext()
        {
            return numberLeft > 0;
        }

        public V next()
        {
            V result = null;

            if (hasNext())
            {
                while ((hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved())
                {
                    currentIndex++;
                }

                result = hashTable[currentIndex].getValue();
                numberLeft--;
                currentIndex++;
            }
            else
                throw new NoSuchElementException();

            return result;
        }
        

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class TableEntry<S, T>
    {
        private S key;
        private T value;
        private States state;
        private enum States {CURRENT, REMOVED}

        private TableEntry(S searchKey, T dataValue)
        {
            key = searchKey;
            value = dataValue;
            state = States.CURRENT;
        }

        private S getKey()
        {
            return key;
        }

        private T getValue()
        {
            return value;
        }

        private void setValue(T newValue)
        {
            value = newValue;
        }

        private boolean isIn()
        {
            return state == States.CURRENT;
        }

        private boolean isRemoved()
        {
            return state == States.REMOVED;
        }

        private void setToRemoved()
        {
            key = null;
            value = null;
            state = States.REMOVED;
        }

        private void setToIn()
        {
            state = States.CURRENT;
        }
    }
}