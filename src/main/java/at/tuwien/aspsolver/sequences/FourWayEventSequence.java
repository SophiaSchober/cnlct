package at.tuwien.aspsolver.sequences;

import org.tweetyproject.lp.asp.syntax.ASPAtom;

import java.util.Objects;

public class FourWayEventSequence implements EventSequence {

    private final String event1;
    private final String event2;
    private final String event3;
    private final String event4;

    public FourWayEventSequence(ASPAtom coveredAtom) {
        this.event1 = coveredAtom.getTerm(0).toString();
        this.event2 = coveredAtom.getTerm(1).toString();
        this.event3 = coveredAtom.getTerm(2).toString();
        this.event4 = coveredAtom.getTerm(3).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FourWayEventSequence that = (FourWayEventSequence) o;
        return Objects.equals(event1, that.event1) && Objects.equals(event2, that.event2)
                && Objects.equals(event3, that.event3) && Objects.equals(event4, that.event4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event1,event2,event3,event4);
    }

    @Override
    public String toString() {
        return "covered: (" + event1 + ", " + event2 + ", " + event3 + ", " + event4 + ")";
    }

}
