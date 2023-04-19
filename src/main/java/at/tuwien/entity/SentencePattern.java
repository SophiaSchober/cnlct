package at.tuwien.entity;

public class SentencePattern {
    private String patternNumber;
    private String patternName;
    private String pattern;
    private String example;

    public SentencePattern(String csvString) {
        String[] parts = csvString.split(";");
        this.patternNumber = parts[0];
        this.patternName = parts[1];
        this.pattern = parts[2];
        this.example = parts[3];
    }

    public String getPatternNumber() {
        return patternNumber;
    }

    public void setPatternNumber(String patternNumber) {
        this.patternNumber = patternNumber;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String toString() {
        return patternNumber + " " + patternName + " " + pattern + " " + example;
    }
}
