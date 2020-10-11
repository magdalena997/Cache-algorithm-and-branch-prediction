package rs.ac.bg.etf.aor.loader;

import rs.ac.bg.etf.aor.memory.MemoryOperation;

public interface ITraceLoader {

    MemoryOperation getNextOperation();

    boolean isInstructionOperation();

    boolean hasOperationToLoad();

    void reset();
}
