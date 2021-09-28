package chemotaxis.g1.ids;

import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalPlacement;

/**
 * Wrapper class so that we can compare and de-dupe chemical placements.
 */
public class IDSCandidate implements Comparable<IDSCandidate> {
    private ChemicalPlacement placement;
    private String repr;

    IDSCandidate(ChemicalPlacement placement) {
        this.setPlacement(placement);
    }

    public ChemicalPlacement getPlacement() {
        return placement;
    }

    public void setPlacement(ChemicalPlacement placement) {
        this.placement = placement;
        StringBuilder sb = new StringBuilder();
        sb.append(placement.location.x);
        sb.append(',');
        sb.append(placement.location.y);
        if (placement.chemicals.contains(ChemicalCell.ChemicalType.BLUE)) {
            sb.append('B');
        }
        if (placement.chemicals.contains(ChemicalCell.ChemicalType.GREEN)) {
            sb.append('G');
        }
        if (placement.chemicals.contains(ChemicalCell.ChemicalType.RED)) {
            sb.append('R');
        }
        this.repr = sb.toString();
    }

    @Override
    public String toString() {
        return this.repr;
    }

    @Override
    public int hashCode() {
        return this.repr.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IDSCandidate)) {
            return false;
        }
        return this.repr.equals(((IDSCandidate) other).repr);
    }

    @Override
    public int compareTo(IDSCandidate other) {
        return this.repr.compareTo(other.repr);
    }
}