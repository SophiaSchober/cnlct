package at.tuwien.aspsolver.combinations;

import org.tweetyproject.lp.asp.syntax.ASPAtom;

import java.util.Objects;

public class FourWayParameterCombination implements ParameterCombination {

    private final String param1;
    private final String param2;
    private final String param3;
    private final String param4;
    private final String value1;
    private final String value2;
    private final String value3;
    private final String value4;

    public FourWayParameterCombination(ASPAtom coveredAtom) {
        this.param1 = coveredAtom.getTerm(0).toString();
        this.value1 = coveredAtom.getTerm(1).toString();
        this.param2 = coveredAtom.getTerm(2).toString();
        this.value2 = coveredAtom.getTerm(3).toString();
        this.param3 = coveredAtom.getTerm(4).toString();
        this.value3 = coveredAtom.getTerm(5).toString();
        this.param4 = coveredAtom.getTerm(6).toString();
        this.value4 = coveredAtom.getTerm(7).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FourWayParameterCombination that = (FourWayParameterCombination) o;
        return Objects.equals(param1, that.param1) && Objects.equals(param2, that.param2)
                && Objects.equals(param3, that.param3) && Objects.equals(param4, that.param4)
                && Objects.equals(value1, that.value1) && Objects.equals(value2, that.value2)
                && Objects.equals(value3, that.value3) && Objects.equals(value4, that.value4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(param1, param2, param3, param4, value1, value2, value3, value4);
    }

    @Override
    public String toString() {
        return "covered: " + param1 + "=" + value1 + ", " + param2 + "=" + value2  + ", " + param3 + "=" + value3 +
        ", " + param4 + "=" + value4;
    }

}
