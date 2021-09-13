package chemotaxis.sim;

public class SimPrinter {

	boolean enablePrints = true;
	
	public SimPrinter(boolean enablePrints) {
		this.enablePrints = enablePrints;
	}

	public void println() {
		if(enablePrints)
			System.out.println();
	}
	
	public void println(Object obj) {
		if(enablePrints)
			System.out.println(obj);
	}
	
	public void print(Object obj) {
		if(enablePrints)
			System.out.print(obj);
	}
}