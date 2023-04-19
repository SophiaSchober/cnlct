package at.tuwien.aspsolver.sequences;

import org.tweetyproject.lp.asp.syntax.ASPAtom;

import java.util.Objects;

public class PairwiseEventSequence implements EventSequence {

    private final String event1;
    private final String event2;

    public PairwiseEventSequence(ASPAtom coveredAtom) {
        this.event1 = coveredAtom.getTerm(0).toString();
        this.event2 = coveredAtom.getTerm(1).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairwiseEventSequence that = (PairwiseEventSequence) o;
        return Objects.equals(event1, that.event1) && Objects.equals(event2, that.event2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event1,event2);
    }

    @Override
    public String toString() {
        return "covered: (" + event1 + ", " + event2 + ")";
    }
}
