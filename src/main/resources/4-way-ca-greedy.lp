1{ row(i,P,V):paramvals(P,V) }1 :- params(P).

covered(P1,V1,P2,V2,P3,V3,P4,V4) :- row(N,P1,V1), row(N,P2,V2), row(N,P3,V3), row(N,P4,V4), P1!=P2, P1!=P3, P1!=P4, P2!=P3, P2!=P4, P3!=P4.

#maximize[covered(P1,V1,P2,V2,P3,V3,P4,V4)].