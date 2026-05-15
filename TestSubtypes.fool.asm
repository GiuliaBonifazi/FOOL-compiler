push 0
lhp
push function0
lhp
sw
lhp
push 1
add
shp
push function1
lhp
sw
lhp
push 1
add
shp
push function2
push -1
push 2
push 0
lhp
sw
lhp
push 1
add
shp
lhp
sw
lhp
push 1
add
shp
push 9998
lw
lhp
sw
lhp
lhp
push 1
add
shp
push -1
lfp
push -4
add
lw
print
halt

function0:
cfp
lra
lfp
lw
push -2
add
lw
print
stm
sra
pop
pop
sfp
ltm
lra
js

function1:
cfp
lra
lfp
push 4
lfp
lw
stm
ltm
ltm
lw
push 0
add
lw
js
stm
sra
pop
sfp
ltm
lra
js

function2:
cfp
lra
push 1
stm
sra
pop
pop
sfp
ltm
lra
js