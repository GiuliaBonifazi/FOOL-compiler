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
push -1
push -1
lfp
push -3
add
lw
lfp
push -4
add
lw
beq label0
push 0
b label1
label0:
push 1
label1:
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