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
    static double THRESHOLD = 0.7;
    static int PCMCount = 0;
    static int SPMCount = 0;
    static int CacheCount = 0;
    static int PCMLoadCount = 0;
    static int PCMStoreCount = 0;
    static boolean PCMState;
    static boolean SPMState;
    static boolean ENABLED = true;
    static boolean DISABLED = false;
    static String MODEL = "lbm";

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
    private static long[] blockAccess(long block, boolean operation){
        long energy = 0, time = 0;
        if(PCMState == ENABLED && PCMobj.contains(block)) {
            PCMCount++;
            if(operation == LOAD)
                PCMLoadCount++;
            else
                PCMStoreCount++;
            time = PCMobj.getTimeToAccess(block, operation);
            energy = PCMobj.getEnergyToAccess(block, operation);
            return new long[]{time, energy};
        }else if(SPMState == ENABLED && SPMobj.contains(block)) {
            SPMCount++;
            time = SPMobj.getTimeToAccess(block);
            energy = SPMobj.getEnergyToAccess(block);
            return new long[]{time, energy};
        }
        CacheCount++;
        boolean hit = L1.Access(block);
        if(hit){
            time = L1.hitTime;
            energy = L1.hitEnergy;
            return new long[]{time, energy};
        }else {
            time += L1.missTime;
            energy += L1.missEnergy;
        }
        hit = L2.Access(block);
        if(hit) {
            time += L2.hitTime;
            energy += L2.hitEnergy;
        }else {
            time += L2.missTime;
            energy += L2.missEnergy;
        }
        return new long[]{time, energy};
    }
    private static long[] readBlocks(String filename){
        long time = 0;
        long energy = 0;
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                char op = data.split(" ")[0].charAt(0);
                boolean operation = (op == 'L')? LOAD : STORE;
                long block = Long.parseLong(data.split(" ")[1].substring(2), 16)/64;
                long[] access = blockAccess(block, operation);
                time += access[0];
                energy += access[1];
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return new long[] {time, energy};
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
        int sets_L1 = 64; // 128
        int asso_L1 = 4;  // 4
        int sets_L2 = 256;// 512
        int asso_L2 = 8;  // 8
        int PCM_size = 8192;
        int SPM_size = 16;

        System.out.println("Memory Configurations");
        System.out.println("---------------------");
        System.out.println("L1 Cache: " + sets_L1 + "x" + asso_L1 + " = " + (sets_L1 * asso_L1) + " blocks");
        System.out.println("L2 Cache: " + sets_L2 + "x" + asso_L2 + " = " + (sets_L2 * asso_L2) + " blocks");
        System.out.println("Scratchpad Memory: " + ((SPMState)? SPM_size : 0) + " blocks");
        System.out.println("Phase Change Memory: " + ((PCMState)? PCM_size : 0) + " blocks");
        System.out.println();

        PCMobj = new PhaseChangeMemory(PCM_size);
        SPMobj = new ScratchpadMemory(SPM_size);


        L1 = new Cache(sets_L1, asso_L1, 64, 1, 0);
        L2 = new Cache(sets_L2, asso_L2, 64, 15, 40);
        Simulation.PCMState = PCMState;
        Simulation.SPMState = SPMState;
    }
    private static long getStaticEnergy(long time){
        long power = 0;
        if(PCMState == ENABLED)
            power += PCMobj.staticEnergy(time);
        if(SPMState == ENABLED)
            power += SPMobj.staticEnergy(time);
        power += L1.staticEnergy(time) + L2.staticEnergy(time);
        return power;
    }
    public static void main(String[] args) {
        System.out.println("Profile Configurations");
        System.out.println("----------------------");
        System.out.println("Model: " + MODEL);
        System.out.println("Threshold: " + THRESHOLD );
        System.out.println("Blocks having number of load operations greater than " + (THRESHOLD*100) + " % are selected to be placed in PCM.");
        System.out.println();

        load_blocks_map = new HashMap<>();
        store_blocks_map = new HashMap<>();
        allops_blocks_map = new HashMap<>();
        load_blocks("input_files/"+MODEL+"/loads"+"_"+MODEL+".txt", LOAD);
        load_blocks("input_files/"+MODEL+"/stores"+"_"+MODEL+".txt", STORE);
        find_block_freq();

        System.out.println("\nTest Case 1: PCM SPM Both Enabled");
        System.out.println("------------------------------------");
        configureMemory(ENABLED, ENABLED);
        allocate(true, true);
        String filename = "input_files/"+MODEL+"/output"+"_"+MODEL+".txt";
        long[] access1 = readBlocks(filename);
        long time1 = access1[0];
        long energy1 = access1[1];
        long staticEnergy1 = getStaticEnergy(time1);
        long totalEnergy1 = energy1 + staticEnergy1;

        System.out.println("\nMemory Simulation Statistics");
        System.out.println("------------------------------");
        System.out.println("Total time required is: " + time1);
        System.out.println("PCMCount: " + PCMCount + " SPMCount: " + SPMCount + " CacheCount: " + CacheCount);
        System.out.println("PCMLoadCount: " + PCMLoadCount);
        System.out.println("PCMStoreCount: " + PCMStoreCount);

        System.out.println("\nTotal energy required is: " + totalEnergy1);
        System.out.println("Static energy required is: " + staticEnergy1);
        System.out.println("Load/Store energy required is: " + energy1);

        System.out.println("\nTest Case 2: PCM SPM Both Disabled");
        System.out.println("--------------------------------------");
        clear();
        configureMemory(DISABLED, DISABLED);

        long[] access2 = readBlocks(filename);
        long time2 = access2[0];
        long energy2 = access2[1];
        long staticEnergy2 = getStaticEnergy(time2);
        long totalEnergy2 = energy2 + staticEnergy2;

        System.out.println("\nMemory Simulation Statistics");
        System.out.println("------------------------------");
        System.out.println("Total time required is: " + time2);
        System.out.println("PCMCount: " + PCMCount + " SPMCount: " + SPMCount + " CacheCount: " + CacheCount);
        System.out.println("PCMLoadCount: " + PCMLoadCount);
        System.out.println("PCMStoreCount: " + PCMStoreCount);

        System.out.println("\nTotal energy required is: " + totalEnergy2);
        System.out.println("Static energy required is: " + staticEnergy2);
        System.out.println("Load/Store energy required is: " + energy2);

        System.out.println("\nComparision");
        System.out.println("--------------------------------------");
        double timePercentage= ((double)time2-time1)*100/time2;
        double energyPercentage = ((double)totalEnergy2-totalEnergy1)*100/totalEnergy2;
        System.out.printf("Our Model is %.2f %% better than the original in time\n", timePercentage);
        System.out.printf("Our Model is %.2f %% better than the original in energy", energyPercentage);
    }
}
