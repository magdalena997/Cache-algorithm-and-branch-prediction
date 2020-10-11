package rs.ac.bg.etf.predictor.YAGS;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;
import rs.ac.bg.etf.predictor.bimodal.Bimodal;
import rs.ac.bg.etf.predictor.gshare.Gshare;

public class Yags implements Predictor {

    Bimodal  selector;
    Gshare taken, notTaken;
    BHR bhr;
    int [] niz1, niz2;
    int maskN;


    public Yags (int BHRsize, int numOfLastBitsAddressGroups, int numOfLastBitsAddressSelector, Automaton.AutomatonType type, int sizeGshare){
        bhr = new BHR(BHRsize);
        selector = new Bimodal(BHRsize, numOfLastBitsAddressGroups, numOfLastBitsAddressSelector, type);
        int rowSizeGroup = 1<<(BHRsize>numOfLastBitsAddressGroups?BHRsize:numOfLastBitsAddressGroups);
       int sizeGshare1 = 1<<sizeGshare;
        taken = new Gshare(BHRsize, sizeGshare, type);
        notTaken = new Gshare(BHRsize, sizeGshare, type);
        niz1 = new int[sizeGshare1];
        niz2 = new int[sizeGshare1];
        maskN = sizeGshare1-1;

        }

    @Override
    public boolean predict(Instruction branch) {
        boolean bim = selector.predict(branch);
        int ulaz =  (bhr.getValue() ^ (int) branch.getAddress()) & maskN;
        if (bim) {

            if (niz1[ulaz] == branch.getAddress()) {
                return notTaken.predict(branch);
            }
            else {
                return bim;
            }
        }
        else {
            if(niz2[ulaz] == branch.getAddress()) {
                return taken.predict(branch);
            }
            else return bim;
        }




    }

    @Override
    public void update(Instruction branch) {
        int ulaz =  (bhr.getValue() ^ (int) branch.getAddress()) & maskN;
        if((selector.predict(branch) != branch.isTaken() && selector.predict(branch)) || notTaken.predict(branch)){
            notTaken.update(branch);
            niz1[ulaz] = (int) branch.getAddress();
        }
        if((selector.predict(branch) != branch.isTaken() && !selector.predict(branch)) || taken.predict(branch)){
            taken.update(branch);
            niz2[ulaz] = (int) branch.getAddress();
        }

        selector.update(branch);

        boolean outcome=branch.isTaken();
        bhr.insertOutcome(outcome);
    }
}
