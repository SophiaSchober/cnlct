1{ hb(i,X,Y), hb(i,Y,X) }1 :- sym(X;Y), X != Y.

hb(i,X,Z) :- hb(i,X,Y), hb(i,Y,Z).
:- hb(i,X,X).

covered(W,X,Y,Z) :- hb(N,W,X), hb(N,X,Y), hb(N,Y,Z).
#maximize[covered(W,X,Y,Z)].