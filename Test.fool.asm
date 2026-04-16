push 0
push 0
beq label1
push 1
push 1
beq label0
label1:
push 0
b label2
label0:
push 1
label2:
print
halt