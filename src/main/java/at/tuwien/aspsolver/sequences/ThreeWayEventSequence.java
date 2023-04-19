package at.tuwien.aspsolver.sequences;

import org.tweetyproject.lp.asp.syntax.ASPAtom;

import java.util.Objects;

public class ThreeWayEventSequence implements EventSequence {

    private final String event1;
    private final String event2;
    private final String event3;

    public ThreeWayEventSequence(ASPAtom coveredAtom) {
        this.event1 = coveredAtom.getTerm(0).toString();
        this.event2 = coveredAtom.getTerm(1).toString();
        this.event3 = coveredAtom.getTerm(2).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreeWayEventSequence that = (ThreeWayEventSequence) o;
        return Objects.equals(event1, that.event1) && Objects.equals(event2, that.event2) && Objects.equals(event3, that.event3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event1,event2, event3);
    }

    @Override
    public String toString() {
        return "covered: (" + event1 + ", " + event2 + ", " + event3 + ")";
    }
}
