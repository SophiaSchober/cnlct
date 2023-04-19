package at.tuwien.gui.service;

import at.tuwien.entity.TestStrength;
import at.tuwien.entity.TestType;

import java.util.List;


public class MainGuiService implements IMainGuiService {

    private TestType testType = TestType.INPUT_PARAMETER;
    private TestStrength testStrength = TestStrength.PAIRWISE;

    @Override
    public void setTestType(TestType testType) {
        this.testType = testType;
    }

    @Override
    public TestType getTestType() {
        return testType;
    }

    @Override
    public void setTestStrength(TestStrength testStrength) {
        this.testStrength = testStrength;
    }

    @Override
    public TestStrength getTestStrength() {
        return testStrength;
    }
}
