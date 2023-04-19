1{ hb(i,X,Y), hb(i,Y,X) }1 :- sym(X;Y), X != Y.

hb(i,X,Z) :- hb(i,X,Y), hb(i,Y,Z).
:- hb(i,X,X).

covered(X,Y) :- hb(N,X,Y).
#maximize[covered(X,Y)].