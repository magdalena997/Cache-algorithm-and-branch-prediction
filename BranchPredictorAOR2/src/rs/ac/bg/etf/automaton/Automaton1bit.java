package rs.ac.bg.etf.automaton;

public class Automaton1bit implements Automaton {

	private int state;
	Automaton1bit(int state) {
		this.state = state;
	}
    Automaton1bit() {
        this(0);
    }
	
	@Override
	public void updateAutomaton(boolean outcome) {
		if(outcome) state = 1;
		else state = 0;
	}

	@Override
	public boolean predict() {
		if(state != 0) return true;
		return false;
	}

}
