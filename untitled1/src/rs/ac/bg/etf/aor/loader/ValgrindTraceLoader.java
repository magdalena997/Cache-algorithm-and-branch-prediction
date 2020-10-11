package rs.ac.bg.etf.aor.loader;

import rs.ac.bg.etf.aor.memory.MemoryOperation;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;

public class ValgrindTraceLoader implements ITraceLoader, Closeable {

    private BufferedReader bufferedReader;

    private String fileName;

    private String currentLine = null;
    private String currentOperation;
    private long address;
    private int blockNumber;


    public ValgrindTraceLoader(String fileName) throws IOException {
        this.fileName = fileName;
        this.bufferedReader = new BufferedReader(new FileReader(fileName));
        readNewLine();
    }

    private void parseCurrentLine() {
        try {
            if (currentLine == null) return;
            /*
            possible format [operation address, number of data]:
                SB 04017e70
                I  04017e70,4
                 L 1ffefffe2f,1
                 S 1ffefffda8,8
                 M 0501c628,4
                 -------------
                 SB: superblock (basic block)
                 L:  load
                 S:  store
                 M:  modify (rd+wr)
            */
            currentOperation = currentLine.substring(0, 2).trim().toUpperCase();

            if ("SB".equals(currentOperation)) {
                currentLine = bufferedReader.readLine();
                parseCurrentLine();
            } else {
                int comma = currentLine.lastIndexOf(',');
                address = Long.parseLong(currentLine.substring(3, comma), 16);
                blockNumber = Integer.parseInt(currentLine.substring(comma + 1));
            }
        } catch (Exception e) {
            readNewLine();
        }
    }


    /**
     * This method is used to read next operation.
     *
     * @return MemoryOperation This returns MemoryOperation.
     */
    @Override
    public MemoryOperation getNextOperation() {
        if (!hasOperationToLoad()) return null;

        MemoryOperation result = null;
        switch (currentOperation) {
            case "I":
            case "L":
            case "M":
                result = MemoryOperation.read(address);
                break;
            case "S":
                result = MemoryOperation.write(address);
                break;
        }
        address++;
        blockNumber--;
        if (blockNumber == 0) {
            if (currentOperation.equals("M")) {
                currentLine = currentLine.replace('M', 'S');
                parseCurrentLine();
            } else
                readNewLine();
        }
        return result;
    }



    /**
     * This method is used to check if there is more operation to read.
     *
     * @return boolean This returns boolean
     */
    @Override
    public boolean hasOperationToLoad() {
        return currentLine != null;
    }

    @Override
    public void reset() {
        try {
            close();
        } catch (Exception e) {
        }
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            readNewLine();

        } catch (Exception e) {
        }
    }

    private void readNewLine() {
        try {
            currentLine = bufferedReader.readLine();
            parseCurrentLine();
        } catch (IOException e) {
        }

    }

    /**
     * This method is used to close file handle.
     */
    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }

    @Override
    public boolean isInstructionOperation() {
        return currentOperation!=null && "I".equals(currentOperation);
    }
}
