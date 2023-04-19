import at.tuwien.cnltranslator.CnlTranslator;
import at.tuwien.cnltranslator.InputParameterTestTranslator;
import at.tuwien.cnltranslator.UnknownSentencePatternException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InputParameterTestingTranslatorTest {

    private CnlTranslator translator;

    @BeforeEach
    public void initialize() {
        translator = new InputParameterTestTranslator();
    }

    @Test
    public void testTranslateParamDefinition_noSpaceAfterCommaAndUpperCaseValues() throws UnknownSentencePatternException {
        String testParamDefinition1 = "Parameter has values Value1,Value2,Value3 and Value4.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition1)));
        assertAll(
                () -> assertEquals(5, result.size()),
                () -> assertEquals("params(parameter).", result.get(0).toString()),
                () -> assertEquals("paramvals(parameter,value1).", result.get(1).toString()),
                () -> assertEquals("paramvals(parameter,value2).", result.get(2).toString()),
                () -> assertEquals("paramvals(parameter,value3).", result.get(3).toString()),
                () -> assertEquals("paramvals(parameter,value4).", result.get(4).toString())
        );
    }

    @Test
    public void testTranslateParameterDefinition_SpaceAfterCommaAndDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition2 = "Parameter has values value1, value2, value3 and value4.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition2)));
        assertAll(
                () -> assertEquals(5, result.size()),
                () -> assertEquals("params(parameter).", result.get(0).toString()),
                () -> assertEquals("paramvals(parameter,value1).", result.get(1).toString()),
                () -> assertEquals("paramvals(parameter,value2).", result.get(2).toString()),
                () -> assertEquals("paramvals(parameter,value3).", result.get(3).toString()),
                () -> assertEquals("paramvals(parameter,value4).", result.get(4).toString())
        );
    }


    @Test
    public void testTranslateParameterDefinition_noCommaListAndDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition3 = "Parameter has values value1 and value2.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition3)));
        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("params(parameter).", result.get(0).toString()),
                () -> assertEquals("paramvals(parameter,value1).", result.get(1).toString()),
                () -> assertEquals("paramvals(parameter,value2).", result.get(2).toString())
        );
    }

    @Test
    public void testTranslateParameterDefinition_noAndValueAndNoDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition4 = "Parameter has values value1,value2,value3";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition4)));
        assertAll(
                () -> assertEquals(4, result.size()),
                () -> assertEquals("params(parameter).", result.get(0).toString()),
                () -> assertEquals("paramvals(parameter,value1).", result.get(1).toString()),
                () -> assertEquals("paramvals(parameter,value2).", result.get(2).toString()),
                () -> assertEquals("paramvals(parameter,value3).", result.get(3).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_lowercaseStart_varyingSpaces_isTestedEnding() throws UnknownSentencePatternException {
        String testSeedTest1 = "ensure that Param1 = Value1 and Param2=Value2 and Param3 =Value3 and Param4 =Value4 is tested.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testSeedTest1)));
        assertAll(
                () -> assertEquals(4, result.size()),
                () -> assertEquals("row(1,param1,value1).", result.get(0).toString()),
                () -> assertEquals("row(1,param2,value2).", result.get(1).toString()),
                () -> assertEquals("row(1,param3,value3).", result.get(2).toString()),
                () -> assertEquals("row(1,param4,value4).", result.get(3).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_uppercaseStart_varyingSpaces_isTestedEnding() throws UnknownSentencePatternException {
        String testSeedTest2 = "Ensure that Param1= Value1 and Param2=Value2 and Param3 =Value3 and Param4 =Value4 is tested";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testSeedTest2)));
        assertAll(
                () -> assertEquals(4, result.size()),
                () -> assertEquals("row(1,param1,value1).", result.get(0).toString()),
                () -> assertEquals("row(1,param2,value2).", result.get(1).toString()),
                () -> assertEquals("row(1,param3,value3).", result.get(2).toString()),
                () -> assertEquals("row(1,param4,value4).", result.get(3).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_uppercaseStart_twoParameterValues_noIsTestedEnding() throws UnknownSentencePatternException {
        String testSeedTest3 = "Ensure that Param1 =Value1 and Param2=Value2.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testSeedTest3)));
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("row(1,param1,value1).", result.get(0).toString()),
                () -> assertEquals("row(1,param2,value2).", result.get(1).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_uppercaseStart_threeParameterValues_noIsTestedEnding_noDotEnding() throws UnknownSentencePatternException {
        // seed test definition example sentences
        String testSeedTest4 = "Ensure that Param1 =Value1 and Param2=Value2 and Param3 =Value3";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testSeedTest4)));
        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("row(1,param1,value1).", result.get(0).toString()),
                () -> assertEquals("row(1,param2,value2).", result.get(1).toString()),
                () -> assertEquals("row(1,param3,value3).", result.get(2).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_multipleSeedTests_translatedToDifferentRowNumberConditions() throws UnknownSentencePatternException {
        String testSeedTest3 = "Ensure that Param1 =Value1 and Param2=Value2.";
        String testSeedTest4 = "Ensure that Param1 =Value1 and Param2=Value2 and Param3 =Value3";
        List<ASPRule> result1 = translator.translate(new ArrayList<>(Collections.singleton(testSeedTest3)));
        List<ASPRule> result2 = translator.translate(new ArrayList<>(Collections.singleton(testSeedTest4)));
        assertAll(
                () -> assertEquals(2, result1.size()),
                () -> assertEquals("row(1,param1,value1).", result1.get(0).toString()),
                () -> assertEquals("row(1,param2,value2).", result1.get(1).toString()),
                () -> assertEquals(3, result2.size()),
                () -> assertEquals("row(2,param1,value1).", result2.get(0).toString()),
                () -> assertEquals("row(2,param2,value2).", result2.get(1).toString()),
                () -> assertEquals("row(2,param3,value3).", result2.get(2).toString())
        );
    }

    @Test
    public void testExcludeDefinitions_lowercaseStart_varyingSpacesAfterEquals_IsTestedEnding() throws UnknownSentencePatternException {
        String testExclude1 = "exclude that Param1 = Value1 and Param2=Value2 and Param3 =Value3 and Param4 =Value4 is tested.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testExclude1)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- row(X,param1,value1), row(X,param2,value2), row(X,param3,value3), row(X,param4,value4).", result.get(0).toString())
        );
    }

    @Test
    public void testExcludeDefinitions_uppercaseStart_varyingSpacesAfterEquals_isTestedEnding() throws UnknownSentencePatternException {
        String testExclude2 = "Exclude that Param1= Value1 and Param2=Value2 and Param3 =Value3 and Param4 =Value4 is tested";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testExclude2)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- row(X,param1,value1), row(X,param2,value2), row(X,param3,value3), row(X,param4,value4).", result.get(0).toString())
        );
    }

    @Test
    public void testExcludeDefinitions_twoParameterValue_noIsTestedEnding() throws UnknownSentencePatternException {
        String testExclude3 = "Exclude that Param1 =Value1 and Param2=Value2.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testExclude3)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- row(X,param1,value1), row(X,param2,value2).", result.get(0).toString())
        );
    }

    @Test
    public void testExcludeDefinitions_threeParameterValues_noIsTestedEnding_noDotEnding() throws UnknownSentencePatternException {
        String testExclude4 = "Exclude that Param1 =Value1 and Param2=Value2 and Param3 =Value3";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testExclude4)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- row(X,param1,value1), row(X,param2,value2), row(X,param3,value3).", result.get(0).toString())
        );
    }

    @Test
    public void testConditionDefinition_lowercaseStart_varyingSpacesAfterEquals_dotEnding() throws UnknownSentencePatternException {
        String testCondition1 = "if Param1 = Value1 and Param2=Value2 then Param3 =Value3 and Param4 =Value4.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testCondition1)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("row(X,param3,value3),row(X,param4,value4) :- row(X,param1,value1), row(X,param2,value2).", result.get(0).toString())
        );
    }

    @Test
    public void testConditionDefinition_oneCondition_varyingSpacingAfterEquals_dotEnding() throws UnknownSentencePatternException {
        String testCondition2 = "If Param1= Value1 then Param2=Value2 and Param3 =Value3 and Param4 =Value4.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testCondition2)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("row(X,param2,value2),row(X,param3,value3),row(X,param4,value4) :- row(X,param1,value1).", result.get(0).toString())
        );
    }

    @Test
    public void testConditionDefinition_oneConclusion_varyingSpacingAfterEquals_dotEnding() throws UnknownSentencePatternException {
        String testCondition3 = "If Param1 =Value1 and Param2=Value2 and Param3 =Value3 then Param4 =Value4.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testCondition3)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("row(X,param4,value4) :- row(X,param1,value1), row(X,param2,value2), row(X,param3,value3).", result.get(0).toString())
        );
    }

    @Test
    public void testConditionDefinition_lowercaseStart_varyingSpacingAfterEquals_noDotEnding() throws UnknownSentencePatternException {
        String testCondition4 = "If Param1 =Value1 and Param2=Value2 then Param3 =Value3 and Param4 =Value4";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testCondition4)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("row(X,param3,value3),row(X,param4,value4) :- row(X,param1,value1), row(X,param2,value2).", result.get(0).toString())
        );
    }

}
