import java.lang.*;
import java.util.*;

public class ScratchpadMemory {
    private HashSet<Long> contents;
    private long size;
    private int hitTime = 1;
    ScratchpadMemory(long size){
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
    int getTimeToAccess(long block){
        return hitTime;
    }
}
