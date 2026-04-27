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
lw
lhp
push 1
add
shp
lhp
lw
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
push 1
push 1
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