package miroshka.rasch.logic;

import java.io.File;
import java.io.IOException;

public class RaschModelProcessor {

    private final DataReader dataReader;
    private final RaschModel raschModel;
    
    public RaschModelProcessor() {
        this.dataReader = new DataReader();
        this.raschModel = new RaschModel();
    }
    
    public double[][] readDataFromFile(File file) throws IOException {
        return dataReader.readData(file);
    }
    
    public RaschModel.RaschResult calculateRaschModel(double[][] data) {
        return raschModel.calculate(data);
    }

    public static class RaschResult {
        public final double[] personAbilities;
        public final double[] itemDifficulties;

        public RaschResult(double[] personAbilities, double[] itemDifficulties) {
            this.personAbilities = personAbilities;
            this.itemDifficulties = itemDifficulties;
        }
    }
} 