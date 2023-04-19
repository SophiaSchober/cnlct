import at.tuwien.aspsolver.AspSolver;
import at.tuwien.aspsolver.InputParameterTestAspSolver;
import at.tuwien.entity.TestStrength;
import org.junit.jupiter.api.Test;
import org.tweetyproject.lp.asp.parser.ASPParser;
import org.tweetyproject.lp.asp.parser.ParseException;
import org.tweetyproject.lp.asp.semantics.AnswerSet;
import org.tweetyproject.lp.asp.syntax.ASPLiteral;
import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class AspSolverTest {

    @Test
    public void testPairwiseInputParameterTestSolver() throws IOException, ParseException {
        AspSolver solver = new InputParameterTestAspSolver(TestStrength.PAIRWISE);
        File file = new File("test/resources/userSpecification1.lp");
        List<ASPRule> userSpecification = new ArrayList<>();
        Scanner ruleScanner = new Scanner(file);
        while (ruleScanner.hasNext()) {
            String rule = String.valueOf(ruleScanner.next());
            if (rule != null && rule.length() > 0) {
                userSpecification.add(ASPParser.parseRule(rule));
            }
        }

        AnswerSet answerSet = solver.solve(userSpecification, 1);
        List<ASPLiteral> literals = answerSet.getLiteralsWithName("row").stream().toList();
        System.out.println("-------------------------------------");
        System.out.println("Greedy Solution: ");
        System.out.println("-------------------------------------");
        for (ASPLiteral literal : literals) {
            System.out.println(literal.getAtom().toString());
        }

        assertEquals(12, answerSet.getLiteralsWithName("row").size());

    }

    @Test
    public void testThreeWayInputParameterTestSolver() throws IOException, ParseException {
        AspSolver solver = new InputParameterTestAspSolver(TestStrength.THREE_WAY);
        File file = new File("test/resources/userSpecification1.lp");
        List<ASPRule> userSpecification = new ArrayList<>();
        Scanner ruleScanner = new Scanner(file);
        while (ruleScanner.hasNext()) {
            String rule = String.valueOf(ruleScanner.next());
            if (rule != null && rule.length() > 0) {
                userSpecification.add(ASPParser.parseRule(rule));
            }
        }

        AnswerSet answerSet = solver.solve(userSpecification, 1);
        List<ASPLiteral> literals = answerSet.getLiteralsWithName("row").stream().toList();
        System.out.println("-------------------------------------");
        System.out.println("Greedy Solution: ");
        System.out.println("-------------------------------------");
        for (ASPLiteral literal : literals) {
            System.out.println(literal.getAtom().toString());
        }

        assertEquals(24, answerSet.getLiteralsWithName("row").size());

    }
}
