package at.tuwien.testextractor;

import javafx.beans.property.StringProperty;
import org.tweetyproject.lp.asp.semantics.AnswerSet;

import java.util.List;

public interface TestCaseExtractor {

    List<List<StringProperty>> extract(AnswerSet answerSet, List<String> paramOrder);
}
