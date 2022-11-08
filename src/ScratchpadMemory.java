import java.lang.*;
import java.util.*;

public class ScratchpadMemory {
    private HashSet<Long> contents;
    private long size;
    private int hitTime = 1;
    private long hitEnergy;
    final int staticPowerPerByte = 5;
    int blockSize = 64;
    long energyFactor = 10000000;
    ScratchpadMemory(long size){
        this.size = size;
        contents = new HashSet<>();
        hitEnergy = hitTime * energyFactor;
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
    long getEnergyToAccess(long block){ return hitEnergy; }
    long getSizeInBytes(){
        return size * blockSize;
    }
    long staticEnergy(long time){
        return time * staticPowerPerByte * getSizeInBytes();
    }
}
