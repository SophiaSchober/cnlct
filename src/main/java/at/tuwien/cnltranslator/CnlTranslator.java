package at.tuwien.cnltranslator;

import org.tweetyproject.lp.asp.syntax.ASPRule;

import java.util.List;

public interface CnlTranslator {

    List<ASPRule> translate(List<String> cnlSpecification) throws UnknownSentencePatternException;

    List<String> getHeaderRow();

    int getSeedTestCounter();
}
