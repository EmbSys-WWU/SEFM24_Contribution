package raid24contribution.engine.util;

public class MutableInteger implements Comparable<MutableInteger> {
    
    int i = 0;
    
    public MutableInteger(int i) {
        this.i = i;
    }
    
    public int getI() {
        return this.i;
    }
    
    public void setI(int i) {
        this.i = i;
    }
    
    @Override
    public String toString() {
        return this.i + "";
    }
    
    @Override
    public int compareTo(MutableInteger o) {
        return new Integer(this.i).compareTo(new Integer(o.getI()));
    }
}
