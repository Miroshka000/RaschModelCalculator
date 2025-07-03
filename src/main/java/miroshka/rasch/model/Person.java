package miroshka.rasch.model;

public class Person {
    private final int id;
    private final double ability;
    private double infitMNSQ;
    private double outfitMNSQ;
    private double infitZSTD;
    private double outfitZSTD;

    public Person(int id, double ability) {
        this.id = id;
        this.ability = ability;
    }

    public int getId() {
        return id;
    }

    public double getAbility() {
        return ability;
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