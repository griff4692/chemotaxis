package chemotaxis.g1.ids;

import chemotaxis.sim.ChemicalCell.ChemicalType;

import java.awt.*;

public class IDSCandidate {
    public Point location;
    public ChemicalType color;

    IDSCandidate(Point p, ChemicalType c) {
        this.location = new Point(p.x, p.y);
        this.color = c;
    }

    IDSCandidate(int x, int y, ChemicalType c) {
        this.location = new Point(x, y);
        this.color = c;
    }
}