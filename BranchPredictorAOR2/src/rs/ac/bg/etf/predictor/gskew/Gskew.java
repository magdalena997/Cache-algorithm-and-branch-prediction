package rs.ac.bg.etf.predictor.gskew;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class Gskew implements Predictor {

    Automaton[] automata1;
    Automaton[] automata2;
    Automaton[] automata3;
    BHR bhr;
    int mask;

    public Gskew(int BHRsize, int numOfLastBitsAddress, Automaton.AutomatonType type){
        bhr = new BHR(BHRsize);
        int rowSizeGroup = 1<<(BHRsize>numOfLastBitsAddress?BHRsize:numOfLastBitsAddress);

        automata1 = Automaton.instanceArray(type, rowSizeGroup);
        automata2 = Automaton.instanceArray(type, rowSizeGroup);
        automata3 = Automaton.instanceArray(type, rowSizeGroup);

        //mask = rowSizeGroup-1;
        mask = 1<<numOfLastBitsAddress -1;

    }

    @Override
    public boolean predict(Instruction branch) {
        int indexRowGroup1 = f1(bhr.getValue(), (int) branch.getAddress());
        int indexRowGroup2 = f2(bhr.getValue(), (int) branch.getAddress());
        int indexRowGroup0 = f0(bhr.getValue(), (int) branch.getAddress());

        if (automata1[indexRowGroup1].predict()){
            if (automata2[indexRowGroup2].predict())
                return automata1[indexRowGroup1].predict();
            else {
                if (automata3[indexRowGroup0].predict())
                    return automata1[indexRowGroup1].predict();
            }
        }
        else {
            if (automata2[indexRowGroup2].predict()){
                if (automata3[indexRowGroup0].predict())
                    return automata2[indexRowGroup2].predict();
                else return automata3[indexRowGroup0].predict();
            }
            else return automata2[indexRowGroup2].predict();

        }


        return false;
    }

    public int f1(int bhrValue, int address){
        return  ((bhrValue ^ address + 123)&mask);
    }

    public int f2(int bhrValue, int address){
        return  ((bhrValue & address)&mask);
    }

    public int f0(int bhrValue, int address){
        return  ((bhrValue |~ address)&mask);
    }

    @Override
    public void update(Instruction branch) {
        int indexRowGroup1 = f1(bhr.getValue(), (int) branch.getAddress());
        int indexRowGroup2 = f2(bhr.getValue(), (int) branch.getAddress());
        int indexRowGroup0 = f0(bhr.getValue(), (int) branch.getAddress());

     if(automata1[indexRowGroup1].predict() == automata2[indexRowGroup2].predict() == automata3[indexRowGroup0].predict()){
         automata1[indexRowGroup1].updateAutomaton(branch.isTaken());
         automata2[indexRowGroup2].updateAutomaton(branch.isTaken());
         automata3[indexRowGroup0].updateAutomaton(branch.isTaken());
     }
     else {
         if(automata1[indexRowGroup1].predict() == automata2[indexRowGroup2].predict()) {
             automata1[indexRowGroup1].updateAutomaton(branch.isTaken());
             automata2[indexRowGroup2].updateAutomaton(branch.isTaken());
         }
         else {
             if(automata1[indexRowGroup1].predict() == automata3[indexRowGroup0].predict()) {
                 automata1[indexRowGroup1].updateAutomaton(branch.isTaken());
                 automata3[indexRowGroup0].updateAutomaton(branch.isTaken());
             }
             else {
                 if(automata3[indexRowGroup0].predict() == automata2[indexRowGroup2].predict()) {
                     automata3[indexRowGroup0].updateAutomaton(branch.isTaken());
                     automata2[indexRowGroup2].updateAutomaton(branch.isTaken());
                 }
             }
         }
     }




   bhr.insertOutcome(branch.isTaken());
    }
}
