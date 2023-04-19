package at.tuwien.cnltranslator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetyproject.logics.commons.syntax.Constant;
import org.tweetyproject.logics.commons.syntax.Predicate;
import org.tweetyproject.logics.commons.syntax.Variable;
import org.tweetyproject.lp.asp.syntax.ASPAtom;
import org.tweetyproject.lp.asp.syntax.ASPBodyElement;
import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.util.ArrayList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputParameterTestTranslator implements CnlTranslator {

    private final Logger logger = LoggerFactory.getLogger(InputParameterTestTranslator.class);

    private final Predicate paramsPredicate = new Predicate("params", 1);
    private final Predicate paramValsPredicate = new Predicate("paramvals", 2);
    private final Predicate rowPredicate = new Predicate("row", 3);


    // param-name and one value are required, optional comma-separated list of further params (with optional space after comma), optional last value after " and ", optional dot ending
    private final String paramDefinitionRegex = "(?<name>\\w+) has values (?<firstval>\\w+)(?<commavals>\\s{0}(?:,\\s?\\w+)*)(?:,? and (?<lastval>\\w+))?\\.?";

    // Case-insensitive start, list with at least 2 parameter assignments separated by " and ", optional " is tested" and dot ending.
    private final String seedTestRegex = "(?:Ensure|ensure) that (?<firstparam>\\w+)\\s?=\\s?(?<firstval>\\w+)(?<othervals>\\s{0}(?: and \\w+\\s?=\\s?\\w+)+)( is tested)?\\.?";

    // Case-insensitive start, list with at least 2 parameter assignments separated by " and ", optional " is tested" and dot ending.
    private final String exclusionRegex = "(?:Exclude|exclude) that (?<firstparam>\\w+)\\s?=\\s?(?<firstval>\\w+)(?<othervals>\\s{0}(?:,? and \\w+\\s?=\\s?\\w+)+)( is tested)?\\.?";

    // Case-insensitive start, if ... then ... structure, two lists with at least 2 parameter assignments separated by " and ", optional dot ending.
    private final String conditionalRegex = "(?:If|if) (?<firstcondparam>\\w+)\\s?=\\s?(?<firstcondval>\\w+)(?<conditions>\\s{0}(?:,? and \\w+\\s?=\\s?\\w+)*) then (?<firstconcparam>\\w+)\\s?=\\s?(?<firstconcval>\\w+)(?<conclusions>\\s{0}(?:,? and \\w+\\s?=\\s?\\w+)*)\\.?";

    private int seedTestCounter = 0;

    private final List<String> headerRow = new ArrayList<>();

    public List<ASPRule> translate(List<String> cnlSpecification) throws UnknownSentencePatternException {
        logger.info("STARTING TRANSLATION...");
        headerRow.add("Nr.");

        List<ASPRule> translatedRules = new ArrayList<>();
        for (String line : cnlSpecification) {
            if (line == null || line.replace(" ", "").length() == 0) {
                continue; // skip empty lines
            }

           if (Pattern.matches(paramDefinitionRegex, line)) {
                logger.debug("\"{}\" matches paramDefinitionPattern", line);
                translatedRules.addAll(translateParameterDefinition(line));
            }
            else if (Pattern.matches(seedTestRegex, line)) {
                logger.debug("\"{}\" matches seedTestPattern", line);
                translatedRules.addAll(translateSeedTest(line));
            }
            else if (Pattern.matches(exclusionRegex, line)) {
                logger.debug("\"{}\" matches exclusionPattern", line);
                translatedRules.add(translateExclusion(line));
            }
            else if (Pattern.matches(conditionalRegex, line)) {
                logger.debug("\"{}\" matches conditionalPattern", line);
                translatedRules.add(translateConditional(line));
            } else {
                throw new UnknownSentencePatternException("Error while translating sentence \"" + line + "\": Unknown pattern");
            }
        }

        return translatedRules;
    }

    private List<ASPRule> translateParameterDefinition(String parameterDefinitionSentence) {
        logger.info("Translating parameter definition");
        List<ASPRule> rules = new ArrayList<>();
        Matcher matcher = Pattern.compile(paramDefinitionRegex).matcher(parameterDefinitionSentence);

        if (matcher.find()) {
            // extract name and first value
            String paramName = matcher.group("name");
           logger.debug("Extracted parameter name: {}", paramName);

           if (!headerRow.contains(paramName)) {
               headerRow.add(paramName);
           }

            List<String> values = new ArrayList<>();
            values.add(matcher.group("firstval"));

            // extract optional additional values separated by commas
            String commaValuesGroup = matcher.group("commavals");
            String[] commaValues = new String[]{};
            if (commaValuesGroup != null && commaValuesGroup.length() > 0) {
                commaValues = commaValuesGroup.substring(1).split(",");
            }
            for (String value: commaValues) {
                value = value.replace(" ", "");
                values.add(value);
            }

            // extract optional last value after "and" keyword
            String lastVal = matcher.group("lastval");
            values.add(lastVal);

            // map to ASP rules
            String lowerCaseParamName = Character.toLowerCase(paramName.charAt(0)) + paramName.substring(1);
            Constant paramConstant = new Constant(lowerCaseParamName);
            rules.add(createParamDefinitionASPRule(paramConstant));
            for (String valueString: values) {
                if (valueString != null) {
                    logger.debug("Extracted value: {}", valueString);
                    rules.add(createParamValueASPRule(valueString, paramConstant));
                }
            }
        }
        return rules;
    }


    private List<ASPRule> translateSeedTest(String seedTestDefinition) {
        logger.info("Translating seedTest");
        List<ASPRule> rules = new ArrayList<>();
        Matcher matcher = Pattern.compile(seedTestRegex).matcher(seedTestDefinition);
        seedTestCounter++;

        if(matcher.find()) {
            String firstParameter = matcher.group("firstparam");
            String firstValue = matcher.group("firstval");
            logger.debug("Extracted assignment: {} =  {}", firstParameter, firstValue);
            rules.add(createSeedTestASPRule(seedTestCounter, firstParameter, firstValue));

            String[] assignments = new String[]{};
            String otherParamAssignments = matcher.group("othervals");
            if (otherParamAssignments != null && otherParamAssignments.length() > 0) {
                assignments = otherParamAssignments.substring(5).split(" and ");
            }
            for (String assignment: assignments) {
                assignment = assignment.replace(" ", "");
                String[] components = assignment.split("=");
                String param = components[0];
                String value = components[1];
                logger.debug("Extracted assignment: {} =  {}", param, value);
                rules.add(createSeedTestASPRule(seedTestCounter, param, value));
            }
        }

        return rules;
    }

    private ASPRule translateExclusion(String exclusionDefinition) {
        logger.info("Translating exclusion");
        Matcher matcher = Pattern.compile(exclusionRegex).matcher(exclusionDefinition);
        List<String> params = new ArrayList<>();
        List<String> values = new ArrayList<>();

        if(matcher.find()) {
            // extract first assignment
            String firstParameter = matcher.group("firstparam");
            String firstValue = matcher.group("firstval");
            logger.debug("Extracted assignment: {} = {}", firstParameter, firstValue);
            params.add(firstParameter);
            values.add(firstValue);

            // extract assignments from list separated by " and "
            String[] assignments = new String[]{};
            String otherParamAssignments = matcher.group("othervals");
            if (otherParamAssignments != null && otherParamAssignments.length() > 0) {
                assignments = otherParamAssignments.substring(5).split(" and ");
            }
            for (String assignment: assignments) {
                assignment = assignment.replace(" ", "");
                String[] components = assignment.split("=");
                String param = components[0];
                String value = components[1];
                logger.debug("Extracted assignment: {} = {}", param, value);
                params.add(param);
                values.add(value);
            }
        }

        return createExclusionASPRule(params, values);
    }

    private ASPRule translateConditional(String conditionDefinition) {
        logger.info("Translating conditional sentence");
        Matcher matcher = Pattern.compile(conditionalRegex).matcher(conditionDefinition);
        List<String> conditionParams = new ArrayList<>();
        List<String> conditionValues = new ArrayList<>();
        List<String> conclusionParams = new ArrayList<>();
        List<String> conclusionValues = new ArrayList<>();

        if (matcher.find()) {
            // extract first condition and conclusion assignment
            String firstCondParam = matcher.group("firstcondparam");
            String firstCondVal = matcher.group("firstcondval");
            String firstConcParam = matcher.group("firstconcparam");
            String firstConcVal = matcher.group("firstconcval");
            logger.debug("Extracted condition assignment: {} = {}", firstCondParam, firstCondVal);
            logger.debug("Extracted conclusion assignment: {} = {}", firstConcParam, firstConcVal);
            conditionParams.add(firstCondParam);
            conditionValues.add(firstCondVal);
            conclusionParams.add(firstConcParam);
            conclusionValues.add(firstConcVal);

            // extract condition assignments from list separated by " and "
            String[] assignments = new String[]{};
            String conditionAssignments = matcher.group("conditions");
            if (conditionAssignments != null && conditionAssignments.length() > 0) {
                assignments = conditionAssignments.substring(5).split(" and ");
            }
            for (String assignment : assignments) {
                assignment = assignment.replace(" ", "");
                String[] components = assignment.split("=");
                String param = components[0];
                String value = components[1];
                logger.debug("Extracted condition assignment: {} = {}", param, value);
                conditionParams.add(param);
                conditionValues.add(value);
            }

            // extract conclusion assignments from list separated by " and "
            assignments = new String[]{};
            String conclusionAssignments = matcher.group("conclusions");
            if (conclusionAssignments != null && conclusionAssignments.length() > 0) {
                assignments = conclusionAssignments.substring(5).split(" and ");
            }
            for (String assignment : assignments) {
                assignment = assignment.replace(" ", "");
                String[] components = assignment.split("=");
                String param = components[0];
                String value = components[1];
                logger.debug("Extracted conclusion assignment: {} = {}", param, value);
                conclusionParams.add(param);
                conclusionValues.add(value);
            }
        }

        return createConditionRule(conditionParams, conditionValues, conclusionParams, conclusionValues);
    }

    private ASPRule createParamDefinitionASPRule(Constant paramConstant) {
        ASPRule paramRule = new ASPRule();
        ASPAtom paramAtom = new ASPAtom();

        paramAtom.setPredicate(paramsPredicate);
        paramAtom.addArgument(paramConstant);
        paramRule.addToHead(paramAtom);

        logger.debug("Created ASP rule: {}", paramRule);
        return paramRule;
    }

    private ASPRule createParamValueASPRule(String value, Constant paramConstant) {
        ASPRule paramValsRule = new ASPRule();
        ASPAtom paramValsAtom = new ASPAtom();
        paramValsAtom.setPredicate(paramValsPredicate);

        paramValsAtom.addArgument(paramConstant);
        String lowerCaseValue = Character.toLowerCase(value.charAt(0)) + value.substring(1);
        paramValsAtom.addArgument(new Constant(lowerCaseValue));

        paramValsRule.addToHead(paramValsAtom);
        logger.debug("Created ASP parameter definition rule: {}", paramValsRule);
        return paramValsRule;
    }

    private ASPRule createSeedTestASPRule(int seedTestNumber, String param, String value) {
        ASPRule seedTestRule = new ASPRule();
        ASPAtom seedTestAtom = new ASPAtom();
        seedTestAtom.setPredicate(rowPredicate);

        seedTestAtom.addArgument(new Constant(String.valueOf(seedTestNumber)));
        String lowerCaseParam = Character.toLowerCase(param.charAt(0)) + param.substring(1);
        String lowerCaseValue = Character.toLowerCase(value.charAt(0)) + value.substring(1);
        seedTestAtom.addArgument(new Constant(lowerCaseParam));
        seedTestAtom.addArgument(new Constant(lowerCaseValue));

        seedTestRule.addToHead(seedTestAtom);
        logger.debug("Created ASP seed test rule: {}", seedTestRule);
        return seedTestRule;
    }

    private ASPRule createExclusionASPRule(List<String> params, List<String> values) {
        ASPRule exclusionRule = new ASPRule();
        Variable lineVar = new Variable("X");
        List<ASPBodyElement> bodyElements = new ArrayList<>();

        for (int i = 0; i < params.size(); i++) {
            ASPAtom exclusionAtom = createRowAtom(params.get(i), values.get(i), lineVar);
            bodyElements.add(exclusionAtom);
        }

        exclusionRule.addBodyElements(bodyElements);
        logger.debug("Created ASP exclusion rule: {}", exclusionRule);
        return exclusionRule;
    }


    private ASPRule createConditionRule(List<String> conditionParams, List<String> conditionValues,
                                               List<String> conclusionParams, List<String> conclusionValues) {
        ASPRule conditionalRule = new ASPRule();
        Variable lineVar = new Variable("X");
        List<ASPBodyElement> bodyElements = new ArrayList<>();

        for (int i = 0; i < conditionParams.size(); i++) {
            ASPAtom conditionAtom = createRowAtom(conditionParams.get(i), conditionValues.get(i), lineVar);
            bodyElements.add(conditionAtom);
        }

        for (int i = 0; i < conclusionParams.size(); i++) {
            ASPAtom conclusionAtom = createRowAtom(conclusionParams.get(i), conclusionValues.get(i), lineVar);
            conditionalRule.addToHead(conclusionAtom);
        }

        conditionalRule.addBodyElements(bodyElements);
        logger.debug("Created ASP conditional rule: {}", conditionalRule);
        return conditionalRule;
    }

    private ASPAtom createRowAtom(String param, String value, Variable row) {
        ASPAtom rowAtom = new ASPAtom();
        rowAtom.setPredicate(rowPredicate);
        rowAtom.addArgument(row);
        String lowerCaseParam = Character.toLowerCase(param.charAt(0)) + param.substring(1);
        String lowerCaseValue = Character.toLowerCase(value.charAt(0)) + value.substring(1);
        rowAtom.addArgument(new Constant(lowerCaseParam));
        rowAtom.addArgument(new Constant(lowerCaseValue));
        return rowAtom;
    }

    public List<String> getHeaderRow() {
        return headerRow;
    }

    public int getSeedTestCounter() {
        return seedTestCounter;
    }
}
