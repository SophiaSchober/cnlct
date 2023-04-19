package at.tuwien.gui.service;

import at.tuwien.entity.TestStrength;
import at.tuwien.entity.TestType;

import java.util.List;

public interface IMainGuiService {

    void setTestType(TestType testType);

    TestType getTestType();

    void setTestStrength(TestStrength testStrength);

    TestStrength getTestStrength();
}
