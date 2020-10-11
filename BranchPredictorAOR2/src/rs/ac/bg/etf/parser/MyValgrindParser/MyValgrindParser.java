package rs.ac.bg.etf.parser.MyValgrindParser;

import rs.ac.bg.etf.parser.CisPenn2011.CISPENN2011_Instruction;
import rs.ac.bg.etf.parser.Parser;
import rs.ac.bg.etf.predictor.Instruction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyValgrindParser implements Parser {


    BufferedReader r;
    String currentLine;


    public MyValgrindParser(String pathToTrace){

            try {
                r = new BufferedReader(
                        new InputStreamReader(new FileInputStream(pathToTrace), "US-ASCII"));

                currentLine = loadFirstLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private String loadFirstLine() throws IOException {

        String line = r.readLine();
        while(line.contains("=="))line = r.readLine();
        return line;

    }


    @Override
    public boolean hasNext()  {
        throw new UnsupportedOperationException();
    }

    @Override
    public Instruction getNext() {
        try {

            if(currentLine.contains("==")) return null;

            String nextLine = r.readLine();
            //System.out.println(line);


            String[] currentLineTokens = currentLine.split(" ");
            String[] nextLineTokens = currentLine.split(" ");

            long pc = Long.parseLong(currentLineTokens[0], 16);

            boolean outcome = false;

            if(currentLineTokens[0].equals(nextLineTokens[0])){

                if(currentLineTokens.length == 2 ) outcome = false;
                else outcome = true;

                currentLine = r.readLine();


            }
            else{

                if(currentLineTokens.length == 2) outcome = true;
                else outcome = false;

                currentLine = nextLine;
            }


            return new CISPENN2011_Instruction(pc, outcome, true, true, false);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
