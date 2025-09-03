package miroshka.rasch.model;

import java.util.Objects;

public final class Person {
    private final int id;
    private final double ability;
    private final double infitMNSQ;
    private final double outfitMNSQ;
    private final double infitZSTD;
    private final double outfitZSTD;

    private Person(Builder builder) {
        this.id = builder.id;
        this.ability = builder.ability;
        this.infitMNSQ = builder.infitMNSQ;
        this.outfitMNSQ = builder.outfitMNSQ;
        this.infitZSTD = builder.infitZSTD;
        this.outfitZSTD = builder.outfitZSTD;
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

    public double getOutfitMNSQ() {
        return outfitMNSQ;
    }

    public double getInfitZSTD() {
        return infitZSTD;
    }

    public double getOutfitZSTD() {
        return outfitZSTD;
    }
    
    public Person withFitStatistics(double infitMNSQ, double outfitMNSQ, double infitZSTD, double outfitZSTD) {
        return new Builder(this.id, this.ability)
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
        Person person = (Person) obj;
        return id == person.id &&
               Double.compare(person.ability, ability) == 0 &&
               Double.compare(person.infitMNSQ, infitMNSQ) == 0 &&
               Double.compare(person.outfitMNSQ, outfitMNSQ) == 0 &&
               Double.compare(person.infitZSTD, infitZSTD) == 0 &&
               Double.compare(person.outfitZSTD, outfitZSTD) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ability, infitMNSQ, outfitMNSQ, infitZSTD, outfitZSTD);
    }

    @Override
    public String toString() {
        return String.format("Person{id=%d, ability=%.4f, infitMNSQ=%.4f, outfitMNSQ=%.4f, infitZSTD=%.4f, outfitZSTD=%.4f}",
            id, ability, infitMNSQ, outfitMNSQ, infitZSTD, outfitZSTD);
    }


    public static Builder builder(int id, double ability) {
        return new Builder(id, ability);
    }

    public static final class Builder {
        private final int id;
        private final double ability;
        private double infitMNSQ = 1.0;
        private double outfitMNSQ = 1.0;
        private double infitZSTD = 0.0;
        private double outfitZSTD = 0.0;

        private Builder(int id, double ability) {
            if (id <= 0) {
                throw new IllegalArgumentException("ID студента должен быть положительным числом");
            }
            this.id = id;
            this.ability = ability;
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

        public Person build() {
            return new Person(this);
        }
    }
} 