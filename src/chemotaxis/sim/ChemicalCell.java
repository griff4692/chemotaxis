package chemotaxis.sim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChemicalCell implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Map<ChemicalType, Double> concentrations = new HashMap<>();
	private boolean isBlocked = false;
	

	public enum ChemicalType implements Serializable {
		RED, GREEN, BLUE
	}
	
	public ChemicalCell() {
		new ChemicalCell(true);
	}
	
	public ChemicalCell(boolean isOpen) {
		ChemicalType[] chemicalTypes = ChemicalType.values();
		for(ChemicalType chemicalType : chemicalTypes)
			concentrations.put(chemicalType, 0.0);
		this.isBlocked = !isOpen;
	}
	
	public void applyConcentration(ChemicalType chemicalType) {
		if(!isBlocked)
			concentrations.put(chemicalType, 1.0);
	}
	
	public Double getConcentration(ChemicalType chemicalType) {
		return concentrations.get(chemicalType);
	}
	
	public void setConcentration(ChemicalType chemicalType, Double concentration) {
		if(!isBlocked)
			concentrations.put(chemicalType, concentration);
	}
	
	public Map<ChemicalType, Double> getConcentrations() {
		return concentrations;
	}
	
	public void setConcentrations(Map<ChemicalType, Double> concentrations) {
		if(!isBlocked)
			this.concentrations = concentrations;
	}
	
	public boolean isBlocked() {
		return isBlocked;
	}
	
	public boolean isOpen() {
		return !isBlocked;
	}
		
	@Override
	public String toString() {
		List<String> stringFormatElements = new ArrayList<>();
		
		ChemicalType[] chemicalTypes = ChemicalType.values();
		for(ChemicalType chemicalType : chemicalTypes)
			stringFormatElements.add(String.format("%.2f", concentrations.get(chemicalType)));
		
		return "(" + String.join(", ", stringFormatElements) + ")";
	}
}