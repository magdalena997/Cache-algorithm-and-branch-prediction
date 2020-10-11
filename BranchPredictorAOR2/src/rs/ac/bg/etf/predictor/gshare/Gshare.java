package rs.ac.bg.etf.predictor.gshare;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class Gshare implements Predictor {


    Automaton[] automata;
    BHR bhr;
    int mask;


    public Gshare(int BHRsize, int numOfLastBitsAddress, Automaton.AutomatonType type){
        bhr = new BHR(BHRsize);
        int rowSizeGroup = 1<<(BHRsize>numOfLastBitsAddress?BHRsize:numOfLastBitsAddress);

        automata = Automaton.instanceArray(type, rowSizeGroup);

        mask = rowSizeGroup-1;


    }

    @Override
    public boolean predict(Instruction branch) {

        int indexRowGroup= (int) ((bhr.getValue() ^ branch.getAddress())&mask);

        return automata[indexRowGroup].predict();
    }

    @Override
    public void update(Instruction branch) {
        int indexRowGroup= (int) ((bhr.getValue() ^ branch.getAddress())&mask);
        automata[ indexRowGroup].updateAutomaton(branch.isTaken());

        bhr.insertOutcome(branch.isTaken());

    }
}
