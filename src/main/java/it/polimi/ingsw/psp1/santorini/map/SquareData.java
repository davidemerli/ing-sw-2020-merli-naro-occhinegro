package it.polimi.ingsw.psp1.santorini.map;

public class SquareData {

    private final int level;
    private final boolean isDome;

    SquareData(int level, boolean isDome) {
        this.level = level;
        this.isDome = isDome;
    }

    public int getLevel() {
        return level;
    }

    public boolean isDome() {
        return isDome;
    }
}