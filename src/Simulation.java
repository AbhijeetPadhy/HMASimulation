import java.lang.*;
import java.util.*;
import java.io.*;

public class Simulation {
    static boolean LOAD = false;
    static boolean STORE = true;
    static HashMap<Long, Long> load_blocks_map;
    static HashMap<Long, Long> store_blocks_map;
    static HashMap<Long, Long> allops_blocks_map;
    static ArrayList<long[]> A;
    static PhaseChangeMemory PCMobj;
    static ScratchpadMemory SPMobj;
    static Cache L1;
    static Cache L2;
    static double THRESHOLD = 0.8;
    //static boolean PCM_ENABLE = true;
    //static boolean SPM_ENABLE;
    static void load_blocks(String filename, boolean operation){
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                long block = Long.parseLong(data.split(" ")[0]);
                long freq = Long.parseLong(data.split(" ")[1]);
                if(operation == LOAD)
                    load_blocks_map.put(block, freq);
                else
                    store_blocks_map.put(block, freq);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    static void find_block_freq(){
        for(Map.Entry<Long, Long> set : load_blocks_map.entrySet()){
            allops_blocks_map.put(set.getKey(), set.getValue());
        }
        for(Map.Entry<Long, Long> set : store_blocks_map.entrySet()){
            long block = set.getKey();
            long freq = set.getValue();
            if(allops_blocks_map.containsKey(block))
                freq += allops_blocks_map.get(block);

            allops_blocks_map.put(set.getKey(), set.getValue());
        }
    }
    private static boolean readHeavy(long block){
        long loadFreq = 0;
        long storeFreq = 0;
        if(load_blocks_map.containsKey(block))
            loadFreq += load_blocks_map.get(block);
        if(store_blocks_map.containsKey(block))
            storeFreq += store_blocks_map.get(block);
        if((double)loadFreq / (loadFreq + storeFreq) > THRESHOLD)
            return true;
        return false;
    }
    public static void allocate(){
        ArrayList<Long> list = new ArrayList<>(allops_blocks_map.keySet());
        Collections.sort(list, (a,b) ->
                (int)(allops_blocks_map.get(b) - allops_blocks_map.get(a))
        );
        for(long block : list){
            if(readHeavy(block) && !PCMobj.is_full())
                PCMobj.insert(block);
            else if(!SPMobj.is_full())
                SPMobj.insert(block);
        }
        System.out.println(PCMobj.fillCount());
        System.out.println(SPMobj.fillCount());
        System.out.println(list.size());
    }
    private static int blockAccess(long block, boolean operation, boolean PCM_ENABLE, boolean SPM_ENABLE){
        if(PCM_ENABLE && PCMobj.contains(block))
            return PCMobj.getTimeToAccess(block, operation);
        else if(SPM_ENABLE && SPMobj.contains(block))
            return SPMobj.getTimeToAccess(block);
        boolean hit = L1.Access(block);
        int time = 0;
        if(hit){
            return L1.hitTime;
        }else
            time += L1.missTime;
        hit = L2.Access(block);
        if(hit)
            time += L2.hitTime;
        else
            time += L2.missTime;
        return time;
    }
    private static long readBlocks(String filename, boolean PCM_ENABLE, boolean SPM_ENABLE){
        long time = 0;
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                char op = data.split(" ")[0].charAt(0);
                boolean operation = (op == 'L')? LOAD : STORE;
                long block = Long.parseLong(data.split(" ")[1].substring(2), 16)/64;
                time += blockAccess(block, operation, PCM_ENABLE, SPM_ENABLE);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return time;
    }
    public static void main(String[] args) {
        load_blocks_map = new HashMap<>();
        store_blocks_map = new HashMap<>();
        allops_blocks_map = new HashMap<>();
        load_blocks("loads.txt", LOAD);
        load_blocks("stores.txt", LOAD);
        find_block_freq();
        System.out.println(allops_blocks_map.size());
        PCMobj = new PhaseChangeMemory(8192);
        SPMobj = new ScratchpadMemory((16));
        L1 = new Cache();
        L2 = new Cache();
        L1.CacheInit(128, 4, 32);
        L2.CacheInit(512, 8, 64);

        allocate();
        String filename = "output.txt";
        System.out.println("Total time required is: " + readBlocks(filename, true, true));
        System.out.println("Total time required is: " + readBlocks(filename, false, false));
	/*
        long L1_hit_count,L1_miss_count;
	long Address;
	int MaxAddress=1<<16;

	Cache L1 = new Cache();
	Cache L2 = new Cache();
	L1.CacheInit(128, 4, 32);
	L2.CacheInit(512, 8, 64);
	for(int i = 0; i < 20000; i++) {
		Address = (long)(Math.random()*MaxAddress);
		boolean hit = L1.Access(Address);
		if(!hit) {
			hit = L2.Access(Address);
		}
	}//end for loop

	System.out.println("\nL1: hit " + L1.get_hit() + " miss " + L1.get_miss());
    System.out.println("\nL2: hit " + L2.get_hit() + " miss " + L2.get_miss());
*/

    }
}
