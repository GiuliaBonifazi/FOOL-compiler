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
push 2
push 1
push 1
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
shp
lhp
lhp
push 1
add
shp
push function2
lfp
lfp
stm
ltm
ltm
push -5
add
lw
js
halt

function0:
cfp
lra
lfp
push 1
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
lw
push -1
add
lw
print
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
lfp

lfp

lw
push -4
add
lw
stm
ltm
ltm
push 1
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