import java.io.*;
import java.util.*;

public class SerialData implements Serializable {
    HashMap<Long, Long> load_blocks_map;
    HashMap<Long, Long> store_blocks_map;
    ArrayList<Long> serialAddress;
    ArrayList<Boolean> serialOperation;
    SerialData(){
        load_blocks_map = new HashMap<>();
        store_blocks_map = new HashMap<>();
        serialAddress = new ArrayList<>();
        serialOperation = new ArrayList<>();
    }
}
