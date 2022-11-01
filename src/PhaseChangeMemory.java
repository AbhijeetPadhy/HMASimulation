import java.lang.*;
import java.util.*;
public class PhaseChangeMemory {
     HashSet<Long> contents;
    private long size;
    private int storeTime = 8;
    private int loadTime = 4;
    private boolean LOAD = false;
    private boolean STORE = true;
    PhaseChangeMemory(long size){
        this.size = size;
        contents = new HashSet<>();
    }
    boolean is_full(){
        if(contents.size() < size)
            return false;
        return true;
    }
    boolean insert(long block){
        if(is_full())
            return false;
        contents.add(block);
        return true;
    }
    void remove(long block){
        contents.remove(block);
    }
    long fillCount(){
        return contents.size();
    }
    boolean contains(long block){
        return contents.contains(block);
    }
    int getTimeToAccess(long block, boolean operation){
        if(operation == LOAD)
            return loadTime;
        return storeTime;
    }
}