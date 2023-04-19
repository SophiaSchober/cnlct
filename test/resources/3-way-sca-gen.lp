row(1..n).
1{ hb(N,X,Y), hb(N,Y,X) }1 :- row(N), sym(X;Y), X!=Y.

hb(N,X,Z) :- hb(N,X,Y), hb(N,Y,Z).
:- hb(N,X,X).

covered(X,Y,Z) :- hb(N,X,Y), hb(N,Y,Z).
:- sym(X;Y;Z), X != Y, Y != Z, X != Z, not covered(X,Y,Z).

