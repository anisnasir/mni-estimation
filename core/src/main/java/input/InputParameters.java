package input;

import com.beust.jcommander.Parameter;

public class InputParameters {
    @Parameter(names = "--simulatorType", required =  true)
    private Integer simulatorType;

    @Parameter(names = "--directory", required = true)
    private String directory;

    @Parameter(names = "--inputFileName", required = true)
    private String inputFileName;

    @Parameter(names = "--output", required = true)
    private String outputDirectory;

    @Parameter(names = "--windowSize", required = true)
    private Integer windowSize;

    @Parameter(names = "--epsilon")
    private Double epsilon;

    @Parameter(names = "--delta")
    private Double delta;

    @Parameter (names = "--Tk")
    private Integer Tk;

    @Parameter (names = "--k")
    private Integer k;

    public InputParameters() {
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Integer getSimulatorType() {
        return simulatorType;
    }

    public void setSimulatorType(Integer simulatorType) {
        this.simulatorType = simulatorType;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Integer windowSize) {
        this.windowSize = windowSize;
    }

    public Double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(Double epsilon) {
        this.epsilon = epsilon;
    }

    public Double getDelta() {
        return delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public Integer getTk() {
        return Tk;
    }

    public void setTk(Integer tk) {
        Tk = tk;
    }

    public Integer getK() {
        return k;
    }

    public void setK(Integer k) {
        this.k = k;
    }
}
