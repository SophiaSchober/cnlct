import at.tuwien.cnltranslator.CnlTranslator;
import at.tuwien.cnltranslator.EventSequenceTestTranslator;
import at.tuwien.cnltranslator.UnknownSentencePatternException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventSequenceTestingTranslatorTest {

    private CnlTranslator translator;

    @BeforeEach
    public void initialize() {
        translator = new EventSequenceTestTranslator();
    }

    @Test
    public void testEventDefinition_spaceAfterComma_uppercaseNames_dotEnding() throws UnknownSentencePatternException {
        String testParamDefinition1 = "Event1, Event2, Event3 and Event4 are events.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition1)));
        assertAll(
                () -> assertEquals(4, result.size()),
                () -> assertEquals("sym(event1).", result.get(0).toString()),
                () -> assertEquals("sym(event2).", result.get(1).toString()),
                () -> assertEquals("sym(event3).", result.get(2).toString()),
                () -> assertEquals("sym(event4).", result.get(3).toString())
        );
    }

    @Test
    public void testEventDefinition_noSpaceAfterComma_lowercaseNames_noAnd_dotEnding() throws UnknownSentencePatternException {
        String testParamDefinition2 = "event1,event2,event3,event4 are events.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition2)));
        assertAll(
                () -> assertEquals(4, result.size()),
                () -> assertEquals("sym(event1).", result.get(0).toString()),
                () -> assertEquals("sym(event2).", result.get(1).toString()),
                () -> assertEquals("sym(event3).", result.get(2).toString()),
                () -> assertEquals("sym(event4).", result.get(3).toString())
        );
    }

    @Test
    public void testEventDefinition_mixedSpaceAfterComma_lowercaseNames_noDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition3 = "event1, event2,event3, and event4 are events";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition3)));
        assertAll(
                () -> assertEquals(4, result.size()),
                () -> assertEquals("sym(event1).", result.get(0).toString()),
                () -> assertEquals("sym(event2).", result.get(1).toString()),
                () -> assertEquals("sym(event3).", result.get(2).toString()),
                () -> assertEquals("sym(event4).", result.get(3).toString())
        );
    }

    @Test
    public void testEventDefinition_noComma_lowercaseNames_noDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition3 = "event1 and event2 are events";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition3)));
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("sym(event1).", result.get(0).toString()),
                () -> assertEquals("sym(event2).", result.get(1).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_lowercaseStart_spaceAfterComma_uppercaseNames_dotEnding() throws UnknownSentencePatternException {
        String testParamDefinition1 = "ensure that (Event1, Event2, Event3, Event4) is tested.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition1)));
        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("hb(1,event1,event2).", result.get(0).toString()),
                () -> assertEquals("hb(1,event2,event3).", result.get(1).toString()),
                () -> assertEquals("hb(1,event3,event4).", result.get(2).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_uppercaseStart_noSpaceAfterComma_uppercaseNames_dotEnding() throws UnknownSentencePatternException {
        String testParamDefinition2 = "Ensure that (Event1,Event2,Event3,Event4) is tested.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition2)));
        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("hb(1,event1,event2).", result.get(0).toString()),
                () -> assertEquals("hb(1,event2,event3).", result.get(1).toString()),
                () -> assertEquals("hb(1,event3,event4).", result.get(2).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_uppercaseStart_mixedSpaceAfterComma_uppercaseNames_dotEnding() throws UnknownSentencePatternException {
        String testParamDefinition3 = "Ensure that (Event1, Event2,Event3, Event4) is tested.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition3)));
        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("hb(1,event1,event2).", result.get(0).toString()),
                () -> assertEquals("hb(1,event2,event3).", result.get(1).toString()),
                () -> assertEquals("hb(1,event3,event4).", result.get(2).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_longerSequence_mixedSpaceAfterComma_uppercaseNames_noDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition4 = "Ensure that (Event1, Event2,Event3, Event4, Event5, Event6) is tested";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition4)));
        assertAll(
                () -> assertEquals(5, result.size()),
                () -> assertEquals("hb(1,event1,event2).", result.get(0).toString()),
                () -> assertEquals("hb(1,event2,event3).", result.get(1).toString()),
                () -> assertEquals("hb(1,event3,event4).", result.get(2).toString()),
                () -> assertEquals("hb(1,event4,event5).", result.get(3).toString()),
                () -> assertEquals("hb(1,event5,event6).", result.get(4).toString())
        );
    }

    @Test
    public void testSeedTestDefinition_multipleSeedTests_generatesMultipleRowRules() throws UnknownSentencePatternException {
        String testParamDefinition1 = "Ensure that (Event1, Event2,Event3, Event4) is tested";
        String testParamDefinition2 = "Ensure that (Event5, Event6,Event7, Event8) is tested";
        List<ASPRule> result1 = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition1)));
        List<ASPRule> result2 = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition2)));
        assertAll(
                () -> assertEquals(3, result1.size()),
                () -> assertEquals(3, result2.size()),
                () -> assertEquals("hb(1,event1,event2).", result1.get(0).toString()),
                () -> assertEquals("hb(1,event2,event3).", result1.get(1).toString()),
                () -> assertEquals("hb(1,event3,event4).", result1.get(2).toString()),
                () -> assertEquals("hb(2,event5,event6).", result2.get(0).toString()),
                () -> assertEquals("hb(2,event6,event7).", result2.get(1).toString()),
                () -> assertEquals("hb(2,event7,event8).", result2.get(2).toString())
        );
    }

    @Test
    public void testOrderExclusionDefinition_lowercaseStart_lowercaseNames_dotEnding() throws UnknownSentencePatternException {
        String testParamDefinition1 = "exclude that event1 happens before event2.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition1)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- hb(X,event1,event2).", result.get(0).toString())
        );
    }

    @Test
    public void testOrderExclusionDefinition_uppercaseStart_lowercaseNames_noDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition2 = "Exclude that event1 happens before event2";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition2)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- hb(X,event1,event2).", result.get(0).toString())
        );
    }

    @Test
    public void testOrderExclusionDefinition_uppercaseStart_uppercaseNames_noDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition3 = "Exclude that Event1 happens before Event2";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition3)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- hb(X,event1,event2).", result.get(0).toString())
        );
    }

    @Test
    public void testOrderFixationDefinition_lowercaseStart_lowercaseNames_dotEnding() throws UnknownSentencePatternException {
        String testParamDefinition1 = "ensure that event1 happens before event2.";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition1)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- hb(X,event2,event1).", result.get(0).toString())
        );
    }

    @Test
    public void testOrderFixationDefinition_uppercaseStart_lowercaseNames_noDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition2 = "Ensure that event1 happens before event2";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition2)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- hb(X,event2,event1).", result.get(0).toString())
        );
    }

    @Test
    public void testOrderFixationDefinition_uppercaseStart_uppercaseNames_noDotEnding() throws UnknownSentencePatternException {
        String testParamDefinition3 = "Ensure that Event1 happens before Event2";
        List<ASPRule> result = translator.translate(new ArrayList<>(Collections.singleton(testParamDefinition3)));
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(" :- hb(X,event2,event1).", result.get(0).toString())
        );
    }

}
