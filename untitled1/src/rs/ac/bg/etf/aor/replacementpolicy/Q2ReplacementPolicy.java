package rs.ac.bg.etf.aor.replacementpolicy;

import rs.ac.bg.etf.aor.memory.MemoryOperation;
import rs.ac.bg.etf.aor.memory.cache.CacheMemoryWriteBackWriteAllocated;
import rs.ac.bg.etf.aor.memory.cache.ICacheMemory;

public class Q2ReplacementPolicy implements IReplacementPolicy {

    FIFOReplacementPolicy AIn, AOut;
    LRUReplacementPolicy An;
    ICacheMemory cache;

    int[] niz;

    @Override
    public void init(ICacheMemory cacheMemory) {
        cache = cacheMemory;
        ICacheMemory ch = cache;
       // ICacheMemory ch = new CacheMemoryWriteBackWriteAllocated((int)cacheMemory.getAdrSize(), (int) cacheMemory.getSetAsociativity(),
           //     (int)  cacheMemory.getBlockNum() / 3, (int) cacheMemory.getBlockSize(), null, null);
        AIn = new FIFOReplacementPolicy();
        AIn.init(ch);
        AOut = new FIFOReplacementPolicy();
        AOut.init(ch);
        An = new LRUReplacementPolicy();
        An.init(ch);
        niz = new int[(int) cacheMemory.getBlockNum()];

    }
//AIn = 1 AOut= 2 An = 3
    @Override
    public int getBlockIndexToReplace(long adr) {

       // int block = (int) cache.extractBlock(adr);
      int izAIn =  AIn.getBlockIndexToReplace(adr);
        niz[izAIn] = 2;
        int izAout = AOut.getBlockIndexToReplace(adr);
        niz[izAout] = 1;

        return izAout;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        int block = (int) cache.extractBlock(operation.getAddress());
        if(niz[block] == 3){
            An.doOperation(operation);
        }
        else {
            if(niz[block] == 1){
                int izAIn = AIn.getBlockIndexToReplace(operation.getAddress());
                niz[izAIn] = 3;
                int izAn = An.getBlockIndexToReplace(operation.getAddress());
                niz[izAn] = 1;
                An.doOperation(operation);
                AIn.doOperation(operation);
            }
            else {
                int izAOut = AOut.getBlockIndexToReplace(operation.getAddress());
                niz[izAOut] = 3;
                int izAn = An.getBlockIndexToReplace(operation.getAddress());
                niz[izAn] = 1;
                int izAIn = AIn.getBlockIndexToReplace(operation.getAddress());
                niz[izAIn] = 2;
                AOut.doOperation(operation);
                AIn.doOperation(operation);
                An.doOperation(operation);
             }
        }

    }

    @Override
    public String printValid() {
        return null;
    }

    @Override
    public String printAll() {
        return null;
    }

    @Override
    public void reset() {

    }
}
