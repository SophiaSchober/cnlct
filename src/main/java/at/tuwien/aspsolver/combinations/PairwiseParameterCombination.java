package at.tuwien.aspsolver.combinations;

import org.tweetyproject.lp.asp.syntax.ASPAtom;

import java.util.Objects;

public class PairwiseParameterCombination implements ParameterCombination {

    private final String param1;
    private final String param2;
    private final String value1;
    private final String value2;

    public PairwiseParameterCombination(ASPAtom coveredAtom) {
        this.param1 = coveredAtom.getTerm(0).toString();
        this.value1 = coveredAtom.getTerm(1).toString();
        this.param2 = coveredAtom.getTerm(2).toString();
        this.value2 = coveredAtom.getTerm(3).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairwiseParameterCombination that = (PairwiseParameterCombination) o;
        return Objects.equals(param1, that.param1) && Objects.equals(param2, that.param2) && Objects.equals(value1, that.value1) && Objects.equals(value2, that.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(param1, param2, value1, value2);
    }

    @Override
    public String toString() {
        return "covered: " + param1 + "=" + value1 + ", " + param2 + "=" + value2;
    }
}
