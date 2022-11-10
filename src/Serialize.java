import java.io.*;
import java.util.*;

public class Serialize {
    static boolean LOAD = false;
    static boolean STORE = true;
    static HashMap<Long, Long> load_blocks_map;
    static HashMap<Long, Long> store_blocks_map;
    static ArrayList<Long> serialAddress;
    static ArrayList<Boolean> serialOperation;
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
    private static void readBlocks(String filename){
        long lineNumber = 0;
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            System.out.println("Progress : ");
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                char op = data.split(" ")[0].charAt(0);
                boolean operation = (op == 'L')? LOAD : STORE;
                long block = Long.parseLong(data.split(" ")[1].substring(2), 16)/64;
                serialAddress.add(block);
                serialOperation.add(operation);
                double percentage = ((lineNumber++)*100)/14181751.0;
                System.out.printf("\r%.2f %%", percentage);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    private static void serialize(SerialData sData){
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("serial/"+MODEL+".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(sData);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in serial/"+MODEL+".ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    private static void deserialise(){
        SerialData e = null;
        try {
            FileInputStream fileIn = new FileInputStream("serial/"+MODEL+".ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            e = (SerialData) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("SerialData class not found");
            c.printStackTrace();
            return;
        }
    }
    public static void main(String[] args) {
        /*
        SerialData sData = new SerialData();
        load_blocks_map = sData.load_blocks_map;
        store_blocks_map = sData.store_blocks_map;
        serialAddress = sData.serialAddress;
        serialOperation = sData.serialOperation;

        load_blocks("input_files/"+MODEL+"/loads"+"_"+MODEL+".txt", LOAD);
        load_blocks("input_files/"+MODEL+"/stores"+"_"+MODEL+".txt", STORE);

        String filename = "input_files/"+MODEL+"/output"+"_"+MODEL+".txt";
        readBlocks(filename);
        */

        //serialize(sData);
        deserialise();
    }
}
