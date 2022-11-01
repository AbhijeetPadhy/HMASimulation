public class Cache {
    int set, asso, LS;
    long hit_counter, miss_counter;
    int TAG[][];
    boolean HIT = true;
    boolean MISS = false;
    int missTime = 40;
    int hitTime = 1;
    long get_hit(){return hit_counter;}
    long get_miss(){return miss_counter;}
    Cache(int sets, int associativity, int LineSize, int hitTime, int missTime){
        int i, j;
        TAG = new int[sets][];
        for(i = 0; i < sets; i++)
            TAG[i] = new int [associativity];
        for(i=0;i<sets;i++)
            for(j=0;j<associativity;j++) TAG[i][j]=-1;
        asso = associativity; set = sets; LS = LineSize;
        hit_counter=miss_counter=0;
        this.hitTime = hitTime;
        this.missTime = missTime;
    }

    boolean Access(long block) {
        int i, x;
        int index = (int)(block % set);
        int Tag = (int)(block/set);
        // HIT
        for(i = 0; i < asso; i++) {
            if (TAG[index][i] == Tag) {
                hit_counter++;
                return HIT;
            }
        }
        miss_counter++;
        // MISS
        x = (int)(Math.random()*3000) % asso; /*used random policy for replacement, You have to LRU policy*/
        TAG[index][x] = Tag;
        return MISS;
    }
    int getTimeToAccess(long block){
        if(Access(block))
            return hitTime;
        return missTime;
    }
}
