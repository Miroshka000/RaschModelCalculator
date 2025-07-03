package miroshka.rasch.logic;

public class RaschModel {
    private double[] personAbilities;
    private double[] itemDifficulties;
    
    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_CRITERION = 0.001;
    
    public RaschModel() {
        this.personAbilities = new double[0];
        this.itemDifficulties = new double[0];
    }
    
    public RaschResult calculate(double[][] data) {
        if (data == null || data.length == 0 || data[0].length == 0) {
            return new RaschResult(new double[0], new double[0], new double[0], new double[0], new double[0], new double[0], new double[0], new double[0], new double[0], new double[0]);
        }

        int numPersons = data.length;
        int numItems = data[0].length;

        personAbilities = new double[numPersons];
        itemDifficulties = new double[numItems];

        binarizeData(data);
        performIterativeComputation(data);
        validateResults();
        
        FitStatistics fitStats = calculateFitStatistics(data);

        return new RaschResult(
            personAbilities, itemDifficulties,
            fitStats.personInfitMNSQ, fitStats.personOutfitMNSQ,
            fitStats.personInfitZSTD, fitStats.personOutfitZSTD,
            fitStats.itemInfitMNSQ, fitStats.itemOutfitMNSQ,
            fitStats.itemInfitZSTD, fitStats.itemOutfitZSTD
        );
    }
    
    private void binarizeData(double[][] data) {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (data[i][j] != 0.0 && data[i][j] != 1.0) {
                    data[i][j] = data[i][j] > 0.5 ? 1.0 : 0.0;
                }
            }
        }
    }
    
    private void performIterativeComputation(double[][] data) {
        int numPersons = data.length;
        int numItems = data[0].length;
        
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double maxChange = 0;

            maxChange = Math.max(maxChange, updateItemDifficulties(data));
            maxChange = Math.max(maxChange, updatePersonAbilities(data));

            if (maxChange < CONVERGENCE_CRITERION) {
                System.out.println("Converged after " + (iteration + 1) + " iterations.");
                break;
            }
        }
    }
    
    private double updateItemDifficulties(double[][] data) {
        double maxChange = 0;
        int numPersons = data.length;
        int numItems = data[0].length;
        
        double[] newItemDifficulties = new double[numItems];
        for (int i = 0; i < numItems; i++) {
            double sumProb = 0;
            double correctCount = 0;
            for (int p = 0; p < numPersons; p++) {
                correctCount += data[p][i];
                double expValue = Math.max(Math.min(personAbilities[p] - itemDifficulties[i], 30), -30);
                double prob = 1.0 / (1.0 + Math.exp(-expValue));
                sumProb += prob;
            }
            
            double denominator = (sumProb * (1 - sumProb / numPersons));
            double delta = denominator > 0.0001 ? (correctCount - sumProb) / denominator : 0;
            
            delta = Math.max(Math.min(delta, 1.0), -1.0);
            newItemDifficulties[i] = itemDifficulties[i] - delta;
        }

        double meanDifficulty = calculateMean(newItemDifficulties);
        for (int i = 0; i < numItems; i++) {
            newItemDifficulties[i] -= meanDifficulty;
            double change = Math.abs(newItemDifficulties[i] - itemDifficulties[i]);
            if (change > maxChange) {
                maxChange = change;
            }
            itemDifficulties[i] = newItemDifficulties[i];
        }
        
        return maxChange;
    }
    
    private double updatePersonAbilities(double[][] data) {
        double maxChange = 0;
        int numPersons = data.length;
        int numItems = data[0].length;
        
        double[] newPersonAbilities = new double[numPersons];
        for (int p = 0; p < numPersons; p++) {
            double sumProb = 0;
            double correctCount = 0;
            for (int i = 0; i < numItems; i++) {
                correctCount += data[p][i];
                double expValue = Math.max(Math.min(personAbilities[p] - itemDifficulties[i], 30), -30);
                double prob = 1.0 / (1.0 + Math.exp(-expValue));
                sumProb += prob;
            }
            
            double denominator = (sumProb * (1 - sumProb / numItems));
            double delta = denominator > 0.0001 ? (correctCount - sumProb) / denominator : 0;
            
            delta = Math.max(Math.min(delta, 1.0), -1.0);
            newPersonAbilities[p] = personAbilities[p] + delta;
        }

        for (int p = 0; p < numPersons; p++) {
            double change = Math.abs(newPersonAbilities[p] - personAbilities[p]);
            if (change > maxChange) {
                maxChange = change;
            }
            personAbilities[p] = newPersonAbilities[p];
        }
        
        return maxChange;
    }
    
    private double calculateMean(double[] values) {
        double sum = 0;
        for (double val : values) {
            sum += val;
        }
        return values.length > 0 ? sum / values.length : 0;
    }
    
    private void validateResults() {
        for (int i = 0; i < itemDifficulties.length; i++) {
            if (Double.isNaN(itemDifficulties[i]) || Double.isInfinite(itemDifficulties[i])) {
                itemDifficulties[i] = 0.0;
            }
        }
        
        for (int i = 0; i < personAbilities.length; i++) {
            if (Double.isNaN(personAbilities[i]) || Double.isInfinite(personAbilities[i])) {
                personAbilities[i] = 0.0;
            }
        }
    }
    
    private FitStatistics calculateFitStatistics(double[][] data) {
        int numPersons = data.length;
        int numItems = data[0].length;

        double[] personInfitMNSQ = new double[numPersons];
        double[] personOutfitMNSQ = new double[numPersons];
        double[] personInfitZSTD = new double[numPersons];
        double[] personOutfitZSTD = new double[numPersons];

        double[] itemInfitMNSQ = new double[numItems];
        double[] itemOutfitMNSQ = new double[numItems];
        double[] itemInfitZSTD = new double[numItems];
        double[] itemOutfitZSTD = new double[numItems];

        for (int p = 0; p < numPersons; p++) {
            double sumResidualSq = 0;
            double sumStdResidualSq = 0;
            double sumVariance = 0;
            double sumKurtosis = 0;
            int count = 0;

            for (int i = 0; i < numItems; i++) {
                double prob = 1.0 / (1.0 + Math.exp(-(personAbilities[p] - itemDifficulties[i])));
                double variance = prob * (1.0 - prob);
                if (variance < 1e-6) continue;

                double residual = data[p][i] - prob;
                double stdResidual = residual / Math.sqrt(variance);
                
                sumResidualSq += residual * residual;
                sumStdResidualSq += stdResidual * stdResidual;
                sumVariance += variance;

                double kurtosis = -2.0; 
                sumKurtosis += variance * kurtosis;
                count++;
            }
            
            if (count > 0) {
                personOutfitMNSQ[p] = sumStdResidualSq / count;
                personInfitMNSQ[p] = sumResidualSq / sumVariance;

                if (personOutfitMNSQ[p] > 0) {
                    personOutfitZSTD[p] = (Math.pow(personOutfitMNSQ[p], 1.0/3.0) - 1.0) * (3.0 / Math.sqrt(2.0/count)) + (Math.sqrt(2.0/count)/3.0);
                }
                 if (personInfitMNSQ[p] > 0) {
                    double q_p = sumKurtosis / sumVariance;
                    double sd_infit = Math.sqrt(q_p);
                    personInfitZSTD[p] = (Math.pow(personInfitMNSQ[p], 1.0/3.0) - 1.0) * (3.0 / sd_infit) + (sd_infit/3.0);
                }
            }
        }

        for (int i = 0; i < numItems; i++) {
            double sumResidualSq = 0;
            double sumStdResidualSq = 0;
            double sumVariance = 0;
            double sumKurtosis = 0;
            int count = 0;
            
            for (int p = 0; p < numPersons; p++) {
                double prob = 1.0 / (1.0 + Math.exp(-(personAbilities[p] - itemDifficulties[i])));
                double variance = prob * (1.0 - prob);
                if (variance < 1e-6) continue;

                double residual = data[p][i] - prob;
                double stdResidual = residual / Math.sqrt(variance);

                sumResidualSq += residual * residual;
                sumStdResidualSq += stdResidual * stdResidual;
                sumVariance += variance;
                
                double kurtosis = -2.0; 
                sumKurtosis += variance * kurtosis;
                count++;
            }

            if (count > 0) {
                itemOutfitMNSQ[i] = sumStdResidualSq / count;
                itemInfitMNSQ[i] = sumResidualSq / sumVariance;
                
                if (itemOutfitMNSQ[i] > 0) {
                    itemOutfitZSTD[i] = (Math.pow(itemOutfitMNSQ[i], 1.0/3.0) - 1.0) * (3.0 / Math.sqrt(2.0/count)) + (Math.sqrt(2.0/count)/3.0);
                }
                if (itemInfitMNSQ[i] > 0) {
                    double q_i = sumKurtosis / sumVariance;
                    double sd_infit = Math.sqrt(q_i);
                    itemInfitZSTD[i] = (Math.pow(itemInfitMNSQ[i], 1.0/3.0) - 1.0) * (3.0 / sd_infit) + (sd_infit/3.0);
                }
            }
        }

        return new FitStatistics(personInfitMNSQ, personOutfitMNSQ, personInfitZSTD, personOutfitZSTD,
                                 itemInfitMNSQ, itemOutfitMNSQ, itemInfitZSTD, itemOutfitZSTD);
    }
    
    public double[] getPersonAbilities() {
        return personAbilities;
    }
    
    public double[] getItemDifficulties() {
        return itemDifficulties;
    }
    
    private static class FitStatistics {
        final double[] personInfitMNSQ, personOutfitMNSQ, personInfitZSTD, personOutfitZSTD;
        final double[] itemInfitMNSQ, itemOutfitMNSQ, itemInfitZSTD, itemOutfitZSTD;

        FitStatistics(double[] personInfitMNSQ, double[] personOutfitMNSQ, double[] personInfitZSTD, double[] personOutfitZSTD,
                      double[] itemInfitMNSQ, double[] itemOutfitMNSQ, double[] itemInfitZSTD, double[] itemOutfitZSTD) {
            this.personInfitMNSQ = personInfitMNSQ;
            this.personOutfitMNSQ = personOutfitMNSQ;
            this.personInfitZSTD = personInfitZSTD;
            this.personOutfitZSTD = personOutfitZSTD;
            this.itemInfitMNSQ = itemInfitMNSQ;
            this.itemOutfitMNSQ = itemOutfitMNSQ;
            this.itemInfitZSTD = itemInfitZSTD;
            this.itemOutfitZSTD = itemOutfitZSTD;
        }
    }
    
    public static class RaschResult {
        private final double[] personAbilities;
        private final double[] itemDifficulties;
        private final double[] personInfitMNSQ;
        private final double[] personOutfitMNSQ;
        private final double[] personInfitZSTD;
        private final double[] personOutfitZSTD;
        private final double[] itemInfitMNSQ;
        private final double[] itemOutfitMNSQ;
        private final double[] itemInfitZSTD;
        private final double[] itemOutfitZSTD;

        public RaschResult(double[] personAbilities, double[] itemDifficulties,
                           double[] personInfitMNSQ, double[] personOutfitMNSQ,
                           double[] personInfitZSTD, double[] personOutfitZSTD,
                           double[] itemInfitMNSQ, double[] itemOutfitMNSQ,
                           double[] itemInfitZSTD, double[] itemOutfitZSTD) {
            this.personAbilities = personAbilities;
            this.itemDifficulties = itemDifficulties;
            this.personInfitMNSQ = personInfitMNSQ;
            this.personOutfitMNSQ = personOutfitMNSQ;
            this.personInfitZSTD = personInfitZSTD;
            this.personOutfitZSTD = personOutfitZSTD;
            this.itemInfitMNSQ = itemInfitMNSQ;
            this.itemOutfitMNSQ = itemOutfitMNSQ;
            this.itemInfitZSTD = itemInfitZSTD;
            this.itemOutfitZSTD = itemOutfitZSTD;
        }
        
        public double[] getPersonAbilities() {
            return personAbilities;
        }
        
        public double[] getItemDifficulties() {
            return itemDifficulties;
        }
        
        public double[] getPersonInfitMNSQ() { return personInfitMNSQ; }
        public double[] getPersonOutfitMNSQ() { return personOutfitMNSQ; }
        public double[] getPersonInfitZSTD() { return personInfitZSTD; }
        public double[] getPersonOutfitZSTD() { return personOutfitZSTD; }
        public double[] getItemInfitMNSQ() { return itemInfitMNSQ; }
        public double[] getItemOutfitMNSQ() { return itemOutfitMNSQ; }
        public double[] getItemInfitZSTD() { return itemInfitZSTD; }
        public double[] getItemOutfitZSTD() { return itemOutfitZSTD; }
        
        public boolean isEmpty() {
            return personAbilities.length == 0 || itemDifficulties.length == 0;
        }
    }
} 