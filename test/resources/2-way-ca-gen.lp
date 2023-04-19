rows(1..n).
1 { row(R,P,V) : paramvals(P,V) } 1 :- rows(R), params(P).

:- row(N,P1,V1), row(N,P2,V2), illegal_values(P1,V1,P2,V2).

covered(P1,V1,P2,V2) :- row(N,P1,V1), row(N,P2,V2), P1 != P2.

:- not covered(P1,V1,P2,V2), params(P1;P2), paramvals(P1,V1),
    paramvals(P2,V2), P1 != P2.

