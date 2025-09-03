package miroshka.rasch.model;

import java.util.Objects;

public final class Item {
    private final int id;
    private final double difficulty;
    private final double infitMNSQ;
    private final double outfitMNSQ;
    private final double infitZSTD;
    private final double outfitZSTD;

    private Item(Builder builder) {
        this.id = builder.id;
        this.difficulty = builder.difficulty;
        this.infitMNSQ = builder.infitMNSQ;
        this.outfitMNSQ = builder.outfitMNSQ;
        this.infitZSTD = builder.infitZSTD;
        this.outfitZSTD = builder.outfitZSTD;
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

    public double getOutfitMNSQ() {
        return outfitMNSQ;
    }

    public double getInfitZSTD() {
        return infitZSTD;
    }

    public double getOutfitZSTD() {
        return outfitZSTD;
    }
    
    public Item withFitStatistics(double infitMNSQ, double outfitMNSQ, double infitZSTD, double outfitZSTD) {
        return new Builder(this.id, this.difficulty)
            .withInfitMNSQ(infitMNSQ)
            .withOutfitMNSQ(outfitMNSQ)
            .withInfitZSTD(infitZSTD)
            .withOutfitZSTD(outfitZSTD)
            .build();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return id == item.id &&
               Double.compare(item.difficulty, difficulty) == 0 &&
               Double.compare(item.infitMNSQ, infitMNSQ) == 0 &&
               Double.compare(item.outfitMNSQ, outfitMNSQ) == 0 &&
               Double.compare(item.infitZSTD, infitZSTD) == 0 &&
               Double.compare(item.outfitZSTD, outfitZSTD) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, difficulty, infitMNSQ, outfitMNSQ, infitZSTD, outfitZSTD);
    }

    @Override
    public String toString() {
        return String.format("Item{id=%d, difficulty=%.4f, infitMNSQ=%.4f, outfitMNSQ=%.4f, infitZSTD=%.4f, outfitZSTD=%.4f}",
            id, difficulty, infitMNSQ, outfitMNSQ, infitZSTD, outfitZSTD);
    }

    public static Builder builder(int id, double difficulty) {
        return new Builder(id, difficulty);
    }

    public static final class Builder {
        private final int id;
        private final double difficulty;
        private double infitMNSQ = 1.0;
        private double outfitMNSQ = 1.0;
        private double infitZSTD = 0.0;
        private double outfitZSTD = 0.0;

        private Builder(int id, double difficulty) {
            if (id <= 0) {
                throw new IllegalArgumentException("ID задания должен быть положительным числом");
            }
            this.id = id;
            this.difficulty = difficulty;
        }

        public Builder withInfitMNSQ(double infitMNSQ) {
            this.infitMNSQ = validateStatistic(infitMNSQ, "Infit MNSQ");
            return this;
        }

        public Builder withOutfitMNSQ(double outfitMNSQ) {
            this.outfitMNSQ = validateStatistic(outfitMNSQ, "Outfit MNSQ");
            return this;
        }

        public Builder withInfitZSTD(double infitZSTD) {
            this.infitZSTD = validateStatistic(infitZSTD, "Infit ZSTD");
            return this;
        }

        public Builder withOutfitZSTD(double outfitZSTD) {
            this.outfitZSTD = validateStatistic(outfitZSTD, "Outfit ZSTD");
            return this;
        }

        private double validateStatistic(double value, String statisticName) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException(statisticName + " не может быть NaN или бесконечностью");
            }
            return value;
        }

        public Item build() {
            return new Item(this);
        }
    }
} 