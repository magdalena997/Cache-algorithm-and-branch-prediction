package rs.ac.bg.etf.aor.replacementpolicy;

import rs.ac.bg.etf.aor.memory.MemoryOperation;
import rs.ac.bg.etf.aor.memory.cache.ICacheMemory;

public interface IReplacementPolicy {

    void init(ICacheMemory cacheMemory);

    int getBlockIndexToReplace(long adr);

    void doOperation(MemoryOperation operation);

    String printValid();

    String printAll();

    void reset();
}
