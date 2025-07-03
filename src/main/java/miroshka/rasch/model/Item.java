package miroshka.rasch.model;

public class Item {
    private final int id;
    private final double difficulty;
    private double infitMNSQ;
    private double outfitMNSQ;
    private double infitZSTD;
    private double outfitZSTD;

    public Item(int id, double difficulty) {
        this.id = id;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public double getInfitMNSQ() {
        return infitMNSQ;
    }

    public void setInfitMNSQ(double infitMNSQ) {
        this.infitMNSQ = infitMNSQ;
    }

    public double getOutfitMNSQ() {
        return outfitMNSQ;
    }

    public void setOutfitMNSQ(double outfitMNSQ) {
        this.outfitMNSQ = outfitMNSQ;
    }

    public double getInfitZSTD() {
        return infitZSTD;
    }

    public void setInfitZSTD(double infitZSTD) {
        this.infitZSTD = infitZSTD;
    }

    public double getOutfitZSTD() {
        return outfitZSTD;
    }

    public void setOutfitZSTD(double outfitZSTD) {
        this.outfitZSTD = outfitZSTD;
    }
} 