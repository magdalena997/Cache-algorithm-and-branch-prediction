package rs.ac.bg.etf.aor;

import rs.ac.bg.etf.aor.loader.ITraceLoader;
import rs.ac.bg.etf.aor.loader.MyXZLoader;
import rs.ac.bg.etf.aor.loader.NativeArrayTraceLoader;
import rs.ac.bg.etf.aor.loader.NativeArrayTraceLoader.*;
import rs.ac.bg.etf.aor.loader.ValgrindTraceLoader;
import rs.ac.bg.etf.aor.memory.MemoryOperation;
import rs.ac.bg.etf.aor.memory.PrimaryMemory;
import rs.ac.bg.etf.aor.memory.cache.CacheMemoryWriteBackWriteAllocated;
import rs.ac.bg.etf.aor.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor.replacementpolicy.IReplacementPolicy;
import rs.ac.bg.etf.aor.replacementpolicy.Q2ReplacementPolicy;

import javax.script.ScriptException;
import java.io.*;

public class TestCache {

    public static void main(String[] args) {
        int adrSize = 32;
        String fNameStatistic = "./stats/tryout.txt";// "./stats/tryout.txt";
        try(FileWriter fileWriter = new FileWriter(fNameStatistic)) {
            fileWriter.append(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\n\r----------------------------------------------------------------------------------\n",
                    "Set asociativity",
                    "Block size",
                    "Cache size[KB]",
                    "Block number",
                    "Cache Hit",
                    "Cache Miss",
                    "Hit ratio"
            ));


        } catch (IOException e) {
            e.printStackTrace();
        }
        int[] setAsocs = {4,8,16,32};
        //int[] blockNums = {1,2,4,8,16,32,64,128,256,512,1024,2048,4096,9192};
        int[] blockSizes = {128,64,32,16};
        int[] cacheSizesKB = {2 ,8, 32, 128, 512,  2048};//, 512 ,2048};

        long start = System.currentTimeMillis();

        for (int cs = 0; cs < cacheSizesKB.length; cs++) {

            int cacheSize = cacheSizesKB[cs]*1024;
            for (int bs = 0; bs < blockSizes.length; bs++) {

                int blockSize = blockSizes[bs];
                int blockNum = cacheSize/blockSize;

                for (int sa = 0; sa < setAsocs.length; sa++) {

                    long before = System.currentTimeMillis();

                    int setAsoc = setAsocs[sa];

                   // if(setAsoc == 4 || setAsoc == 8)continue;
                  //  if(blockSize != 16)continue;

                    if(blockNum/3<setAsoc) continue;

                    System.out.println(setAsoc+"    "+blockSize+"   "+cacheSize);
                    createCacheAndTest(adrSize,setAsoc,blockNum,blockSize,fNameStatistic);


                    long after = System.currentTimeMillis();

                    long durationInMillis = after - before;
                    long millis = durationInMillis % 1000;
                    long second = (durationInMillis / 1000) % 60;
                    long minute = (durationInMillis / (1000 * 60)) % 60;
                    long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

                    String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);

                    System.out.println(setAsoc+" setAsoc   "+blockSize+"B blocks   "+cacheSize+"B cache");
                    System.out.println("Elapsed: "+ time);


                }

            }
        }


        long end = System.currentTimeMillis();

        long durationInMillis = end - start;
        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);

        System.out.println("totaltime: " + time);
    }

    private static void createCacheAndTest(int adrSize, int setAsoc, int blockNum, int blockSize, String fNameStatistic) {

        PrimaryMemory primaryMemory = new PrimaryMemory(adrSize);
       // IReplacementPolicy policy = new LRUReplacementPolicy();
        //IReplacementPolicy policy = new SegmentedLRU(0.2);
        //

        IReplacementPolicy policy = new Q2ReplacementPolicy();
        ICacheMemory cache = new CacheMemoryWriteBackWriteAllocated(adrSize, setAsoc, blockNum, blockSize, primaryMemory, policy);

        //myNativeTest(cache);
        myValgrindTest(cache);
        //myXZTraceTest(cache);

        long hits_cnt = cache.getCacheHitNum();
        long miss_cnt = cache.getCacheMissNum();

        System.out.println("\nSTATISTICS");
        System.out.println("----------------------------------------");
        System.out.println("Hit count:\t " + hits_cnt);
        System.out.println("Miss count:\t " + miss_cnt);
        System.out.println("Hit rate:\t " + String.format("%.2f", ((double) hits_cnt * 100 / (hits_cnt + miss_cnt))) + "%");
       // printTime(cache);

        printStatisticsToFile(cache,fNameStatistic);

        // System.out.println(cache.printValid());
        // System.out.println(cache.getReplacementPolicy().printValid());

    }

    private static void printStatisticsToFile(ICacheMemory cache, String fName) {

        try(FileWriter fileWriter = new FileWriter(fName,true)) {
            fileWriter.append(String.format("%d\t%d\t%d\t%d\t%d\t%d\t%.2f\n\r",
                    cache.getSetAsociativity(),
                    cache.getBlockSize(),
                    cache.getBlockSize()*cache.getBlockNum()/1024,
                    cache.getBlockNum(),
                    cache.getCacheHitNum(),
                    cache.getCacheMissNum(),
                    ((double) cache.getCacheHitNum() * 100 / (cache.getCacheHitNum() + cache.getCacheMissNum()))
                    ));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void printTime(ICacheMemory cache) {
        String time = cache.getAccessTime();
        System.out.println("Time expression: " + time);
        // racunanje vremena
        time = time.replaceAll("tb", "60").replaceAll("tcm", "2");
        javax.script.ScriptEngineManager mgr = new javax.script.ScriptEngineManager();
        javax.script.ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            System.out.println("Cache time:\t " + engine.eval(time) + " ns");
        } catch (ScriptException e) {
        }

    }

    public static void testNoLoopFusion(ICacheMemory cache) {
        // ICacheMemory cache = new CacheMemoryWriteBackWriteAllocated(30, 4, 1024,16);
        int N = 0x100;
        int startA = 0x100000;
        int startB = 0x200000;
        int startC = 0x300000;
        int startD = 0x400000;
        int tempA, tempB, tempC, tempD;
        for (int i = 0; i < N; i = i + 1) {
            for (int j = 0; j < N; j = j + 1) {
                tempB = (int) cache.read(startB + i * N + j);
                tempC = (int) cache.read(startC + i * N + j);
                tempA = 1 + tempB * tempC;
                cache.write(startA + i * N + j, tempA);
            }
        }
        for (int i = 0; i < N; i = i + 1) {
            for (int j = 0; j < N; j = j + 1) {
                tempA = (int) cache.read(startA + i * N + j);
                tempC = (int) cache.read(startC + i * N + j);
                tempD = tempA + tempC;
                cache.write(startD + i * N + j, tempD);
            }
        }
    }

    public static void testLoopFusion(ICacheMemory cache) {
        // ICacheMemory cache = new CacheMemoryWriteBackWriteAllocated(30, 4, 1024, 16);
        int N = 0x100;
        int startA = 0x100000;
        int startB = 0x200000;
        int startC = 0x300000;
        int startD = 0x400000;
        int tempA, tempB, tempC, tempD;
        for (int i = 0; i < N; i = i + 1) {
            for (int j = 0; j < N; j = j + 1) {
                tempB = (int) cache.read(startB + i * N + j);
                tempC = (int) cache.read(startC + i * N + j);
                tempA = 1 + tempB * tempC;
                cache.write(startA + i * N + j, tempA);
                tempD = tempA + tempC;
                cache.write(startD + i * N + j, tempD);
            }
        }
    }

    public static void testMatrixMultiplicationNoBlocking(ICacheMemory cache) {
        // ICacheMemory cache = new CacheMemoryWriteBackWriteAllocated(30, 4,
        // 1024,
        // 16);
        int N = 0x100;
        int startX = 0x100000;
        int startY = 0x200000;
        int startZ = 0x300000;
        int tempX, tempY, tempZ;
        for (int i = 0; i < N; i = i + 1) {
            for (int j = 0; j < N; j = j + 1) {
                int r = 0;
                for (int k = 0; k < N; k = k + 1) {
                    tempY = (int) cache.read(startY + i * N + k);
                    tempZ = (int) cache.read(startZ + k * N + j);
                    r = r + tempY * tempZ;
                }
                tempX = r;
                cache.write(startX + i * N + j, tempX);
            }
        }
    }

    public static void testMatrixMultiplicationBlocking(ICacheMemory cache) {
        // ICacheMemory cache = new CacheMemoryWriteBackWriteAllocated(30, 4,
        // 1024,
        // 16);
        int B = 2;
        int N = 0x100;
        int startX = 0x100000;
        int startY = 0x200000;
        int startZ = 0x300000;
        int tempX, tempY, tempZ;

        for (int jj = 0; jj < N; jj = jj + B) {
            for (int kk = 0; kk < N; kk = kk + B) {
                for (int i = 0; i < N; i = i + 1) {
                    for (int j = jj; j < Math.min(jj + B - 1, N); j = j + 1) {
                        int r = 0;
                        for (int k = kk; k < Math.min(kk + B - 1, N); k = k + 1) {
                            tempY = (int) cache.read(startY + i * N + k);
                            tempZ = (int) cache.read(startZ + k * N + j);
                            r = r + tempY * tempZ;
                        }
                        tempX = (int) cache.read(startX + i * N + j);
                        tempX = tempX + r;
                        cache.write(startX + i * N + j, tempX);
                    }
                }
            }
        }
    }

    public static void myNativeTest(ICacheMemory cache) {

        // kreiranje niza strukture {akcija, adresa}

        ITraceLoader loader = new NativeArrayTraceLoader(

                NativeOperation.ReadInst(0x80C0AA20L),
                NativeOperation.WriteData(0x40C0AA20L),

                NativeOperation.ReadInst(0x20C0AA20L),
                NativeOperation.WriteData(0x80C0AA25L),
                NativeOperation.WriteData(0x10C0AA20L),

                NativeOperation.WriteData(0x20C0AA3AL),
                NativeOperation.ReadInst(0x80C0AA21L),
                NativeOperation.ReadData(0x08C0AA20L),

                NativeOperation.ReadInst(0x04C0AA20L),
                NativeOperation.WriteData(0x20C0AA30L)
        );


        testTrace(cache, loader);
    }

    private static void myValgrindTest(ICacheMemory cache) {

        try (ValgrindTraceLoader loader = new ValgrindTraceLoader("./traces/HelloTrac1e.txt")) {

            testTrace(cache, loader);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static void myXZTraceTest(ICacheMemory cache) {

        try (MyXZLoader loader = new MyXZLoader("./traces/calculix_2655B.trace.xz")) {

            testTrace(cache, loader);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void testTrace(ICacheMemory cache, ITraceLoader loader) {
        long data = 0;
        int i = 0;
        while (loader.hasOperationToLoad() && i <= 10000000) {
            MemoryOperation operation = loader.getNextOperation();

            switch (operation.getType()) {
                case READ:
                    data = cache.read(operation.getAddress());
                    i++;
                    break;
                case WRITE:
                    cache.write(operation.getAddress(), data);
                    i++;
                    break;
                default:
            }
            //System.out.println(cache.printValid());
            //System.out.println(cache.printLastAccess());
        }
    }
}


