import java.lang.*;
import java.util.*;
public class PhaseChangeMemory {
    HashSet<Long> contents;
    private long size;
    private int storeTime = 10;
    private int loadTime = 2;
    private long storeEnergy;
    private long loadEnergy;
    private boolean LOAD = false;
    private boolean STORE = true;
    final int staticPowerPerByte = 1;
    int blockSize = 64;
    long energyFactor = 10000000;
    PhaseChangeMemory(long size){
        this.size = size;
        contents = new HashSet<>();
        storeEnergy = storeTime * energyFactor;
        loadEnergy = loadTime * energyFactor;
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
    long getEnergyToAccess(long block, boolean operation){
        if(operation == LOAD)
            return loadEnergy;
        return storeEnergy;
    }
    long getSizeInBytes(){
        return size * blockSize;
    }
    long staticEnergy(long time){
        return time * staticPowerPerByte * getSizeInBytes();
    }
}
