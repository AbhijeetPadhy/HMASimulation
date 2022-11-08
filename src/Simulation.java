import java.lang.*;
import java.sql.SQLOutput;
import java.util.*;
import java.io.*;

public class Simulation {
    static boolean LOAD = false;
    static boolean STORE = true;
    static HashMap<Long, Long> load_blocks_map;
    static HashMap<Long, Long> store_blocks_map;
    static HashMap<Long, Long> allops_blocks_map;
    static PhaseChangeMemory PCMobj;
    static ScratchpadMemory SPMobj;
    static Cache L1;
    static Cache L2;
    static double THRESHOLD = 0.8;
    static int PCMCount = 0;
    static int SPMCount = 0;
    static int CacheCount = 0;
    static int PCMLoadCount = 0;
    static int PCMStoreCount = 0;
    static boolean PCMState;
    static boolean SPMState;
    static boolean ENABLED = true;
    static boolean DISABLED = false;
    static String MODEL = "mcm";

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
            allops_blocks_map.put(set.getKey(), freq);
        }
    }
    private static boolean readHeavy(long block){
        long loadFreq = 0;
        long storeFreq = 0;
        if(load_blocks_map.containsKey(block))
            loadFreq += load_blocks_map.get(block);
        if(store_blocks_map.containsKey(block))
            storeFreq += store_blocks_map.get(block);
        double formula = (double)loadFreq / (loadFreq + storeFreq);
        if(formula > THRESHOLD)
            return true;
        return false;
    }
    public static void allocate(boolean PCM_ENABLE, boolean SPM_ENABLE){
        ArrayList<Long> list = new ArrayList<>(allops_blocks_map.keySet());
        Collections.sort(list, (a,b) ->
                (int)(allops_blocks_map.get(b) - allops_blocks_map.get(a))
        );
        for(long block : list){
            if(PCM_ENABLE && !SPMobj.is_full())
                SPMobj.insert(block);
            else if(SPM_ENABLE && readHeavy(block) && !PCMobj.is_full())
                PCMobj.insert(block);
        }
        System.out.println("Allocation Statistics");
        System.out.println("---------------------");
        System.out.println("PCM Fill Count: " + PCMobj.fillCount());
        System.out.println("SPM Fill Count: " + SPMobj.fillCount());
        System.out.println("Total Number of Blocks: " + list.size());
    }
    private static int blockAccess(long block, boolean operation){
        if(PCMState == ENABLED && PCMobj.contains(block)) {
            PCMCount++;
            if(operation == LOAD)
                PCMLoadCount++;
            else
                PCMStoreCount++;
            return PCMobj.getTimeToAccess(block, operation);
        }else if(SPMState == ENABLED && SPMobj.contains(block)) {
            SPMCount++;
            return SPMobj.getTimeToAccess(block);
        }
        CacheCount++;
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
    private static long readBlocks(String filename){
        long time = 0;
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                char op = data.split(" ")[0].charAt(0);
                boolean operation = (op == 'L')? LOAD : STORE;
                long block = Long.parseLong(data.split(" ")[1].substring(2), 16)/64;
                time += blockAccess(block, operation);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return time;
    }
    private static void clear(){
        load_blocks_map.clear();
        store_blocks_map.clear();
        allops_blocks_map.clear();
        PCMCount = 0;
        SPMCount = 0;
        CacheCount = 0;
        PCMLoadCount = 0;
        PCMStoreCount = 0;
    }
    private static void configureMemory(boolean PCMState, boolean SPMState){
        PCMobj = new PhaseChangeMemory(8192);
        SPMobj = new ScratchpadMemory((16));
        L1 = new Cache(128, 4, 64, 1, 0);
        L2 = new Cache(512, 8, 64, 15, 40);
        Simulation.PCMState = PCMState;
        Simulation.SPMState = SPMState;
    }
    public static void main(String[] args) {
        System.out.println("Running for Model: "+MODEL + " with a threshold of " + THRESHOLD);
        load_blocks_map = new HashMap<>();
        store_blocks_map = new HashMap<>();
        allops_blocks_map = new HashMap<>();
        load_blocks("input_files/"+MODEL+"/loads"+"_"+MODEL+".txt", LOAD);
        load_blocks("input_files/"+MODEL+"/stores"+"_"+MODEL+".txt", STORE);
        find_block_freq();
        configureMemory(ENABLED, ENABLED);
        allocate(true, true);

        String filename = "input_files/"+MODEL+"/output"+"_"+MODEL+".txt";
        System.out.println("\nTest Case 1: PCM SPM Both Enabled");
        System.out.println("------------------------------------");
        long time1 = readBlocks(filename);
        System.out.println("Total time required is: " + time1);
        System.out.println("PCMCount: " + PCMCount + " SPMCount: " + SPMCount + " CacheCount: " + CacheCount);
        System.out.println("PCMLoadCount: " + PCMLoadCount);
        System.out.println("PCMStoreCount: " + PCMStoreCount);

        //for(long a : PCMobj.contents)
            //System.out.println(a);

        clear();
        configureMemory(DISABLED, DISABLED);
        System.out.println("\nTest Case 2: PCM SPM Both Disabled");
        System.out.println("--------------------------------------");
        long time2 = readBlocks(filename);
        System.out.println("Total time required is: " + time2);
        System.out.println("PCMCount: " + PCMCount + " SPMCount: " + SPMCount + " CacheCount: " + CacheCount);
        System.out.println("PCMLoadCount: " + PCMLoadCount);
        System.out.println("PCMStoreCount: " + PCMStoreCount);

        System.out.println("\nComparision");
        System.out.println("--------------------------------------");
        double percentage= ((double)time2-time1)*100/time2;
        System.out.printf("Our Model is %.2f %% better than the original", percentage);
    }
}
