package foo;


import java.util.Arrays;

class AList<T> implements ListInterface<T> {

    private T[] list; //Array of list entries; ignore list[0]
    private int numberOfEntries;
    private boolean initialized = false;
    private static final int DEFAULT_CAPACITY = 25;
    private static final int MAX_CAPACITY = 10000;
    
    

    public AList() {
        this(DEFAULT_CAPACITY);  
    }

    public AList(int initialCapacity) {
        
        if (initialCapacity < DEFAULT_CAPACITY)
            initialCapacity = DEFAULT_CAPACITY;
        else
            checkCapacity(initialCapacity);
        
        @SuppressWarnings("unchecked")
        T[] tempList = (T[]) new Object[initialCapacity + 1];
        list = tempList;
        numberOfEntries = 0;
        initialized = true;
    }

    public void add(T newEntry) {
        checkInitialization();
        list[numberOfEntries+1] = newEntry;
        numberOfEntries++;
        ensureCapacity();
    } 

    public void add(int newPosition, T newEntry) {
        checkInitialization();

        if ((newPosition >= 1) && (newPosition <= numberOfEntries + 1)) {
            if (newPosition <= numberOfEntries) {
                makeRoom(newPosition);
            }

            list[newPosition] = newEntry;
            numberOfEntries++;
            ensureCapacity();
        } else {
            throw new IndexOutOfBoundsException("Illegal position given to add operation.");
        }
    }

    public T remove(int givenPosition) {
        checkInitialization();
        
        if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
            assert !isEmpty();
            T result = list[givenPosition];
            if (givenPosition < numberOfEntries) {
                removeGap(givenPosition);
            }
            numberOfEntries--;
            return result;
        }
        else
             throw new IndexOutOfBoundsException("Illegal position given to remove operation.");

    }

    public void clear() {
        numberOfEntries = 0;
    } 

    public T replace(int givenPosition, T newEntry) {
        checkInitialization();

        if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
            assert !isEmpty();
            T originalEntry = list[givenPosition];
            list[givenPosition] = newEntry;
            return originalEntry;
        } else 
             throw new IndexOutOfBoundsException("Illegal position given to replace operation.");
 
    }

    public T getEntry(int givenPosition) {
        checkInitialization();
        
        if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
            assert !isEmpty();
            return list[givenPosition];
        } else 
             throw new IndexOutOfBoundsException("Illegal position give to getEntry operation: " + givenPosition);
    }

    public T[] toArray() {
        checkInitialization();
        
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[numberOfEntries];
        for (int index = 0; index < numberOfEntries; index++) {
            result[index] = list[index+1];
        } 

        return result;
    }

    public boolean contains(T anEntry) {
        checkInitialization();
        
        boolean found = false;
        int index = 1;

        while (!found && (index <= numberOfEntries)) {
            if (anEntry.equals(list[index])) {
                found = true;
            }
            index++;
        } 

        return found;
    }

    public int getLength() {
        return numberOfEntries;
    }

    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    private void ensureCapacity() {
        int capacity = list.length - 1;
        
        if (numberOfEntries >= capacity) {
            int newCapacity = 2 * capacity;
            checkCapacity(newCapacity); 
            list = Arrays.copyOf(list, newCapacity + 1);
        }
    }

    private void makeRoom(int newPosition) {
        assert (newPosition >= 1) && (newPosition <= numberOfEntries + 1);

        int newIndex = newPosition;
        int lastIndex = numberOfEntries;
      
        for (int index = lastIndex; index >= newIndex; index--) {
            list[index + 1] = list[index];
        }
    } 

    private void removeGap(int givenPosition) {
        assert (givenPosition >= 1) && (givenPosition < numberOfEntries);

        int removedIndex = givenPosition;
        int lastIndex = numberOfEntries;
        for (int index = removedIndex; index < lastIndex; index++) 
            list[index] = list[index + 1];
    }

    private void checkInitialization()
    {
        if(!initialized)
            throw new SecurityException ("AList object is not initialized properly.");
    }

    private void checkCapacity(int capacity)
    {
        if (capacity > MAX_CAPACITY)
            throw new IllegalStateException("Attempt to create a list " +
            "whose capacity exceeds " + "allowed maximum.");
    }

}
