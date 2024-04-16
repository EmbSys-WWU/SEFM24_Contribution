package raid24contribution.engine.util;

/**
 * Container class that holds two values of arbitrary types. Useful if you want to return two values
 * at once without using Arrays or Containers with arbitrary size.
 * 
 * @param <E1>
 * @param <E2>
 */
public class Pair<E1, E2> {
    
    private E1 first;
    private E2 second;
    
    public Pair(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Returns the first element of the pair.
     *
     * @return
     */
    public E1 getFirst() {
        return this.first;
    }
    
    /**
     * Returns the second element of the pair.
     *
     * @return
     */
    public E2 getSecond() {
        return this.second;
    }
    
    @Override
    public int hashCode() {
        return this.first.hashCode() + this.second.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair<?, ?> op) {
            return this.first.equals(op.first) && this.second.equals(op.second);
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return this.first.toString() + " " + this.second.toString();
    }
    
    public boolean swap() {
        // this wont work if fst/snd super/sub classes
        if (this.first.getClass().equals(this.second.getClass())) {
            E1 tmp = this.first;
            this.first = (E1) this.second;
            this.second = (E2) tmp;
            return true;
        } else {
            return false;
        }
    }
}
