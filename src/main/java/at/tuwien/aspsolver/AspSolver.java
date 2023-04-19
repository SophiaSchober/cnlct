package at.tuwien.aspsolver;

import org.tweetyproject.lp.asp.semantics.AnswerSet;
import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.io.IOException;
import java.util.List;

public interface AspSolver {

    AnswerSet solve (List<ASPRule> aspRules, int minIterations) throws IOException;
}
