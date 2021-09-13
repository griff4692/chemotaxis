package chemotaxis.sim;

import java.awt.Point;
import java.util.List;

import chemotaxis.sim.ChemicalCell.ChemicalType;

import java.util.ArrayList;

public class ChemicalPlacement {
	public Point location = null;
	public List<ChemicalType> chemicals = new ArrayList<>();
}