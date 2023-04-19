1{ row(i,P,V):paramvals(P,V) }1 :- params(P).

covered(P1,V1,P2,V2,P3,V3) :- row(N,P1,V1), row(N,P2,V2), row(N,P3,V3), P1!=P2, P2!=P3, P1!=P3.

#maximize[covered(P1,V1,P2,V2,P3,V3)].