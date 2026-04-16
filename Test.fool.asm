push 1
push 2
bleq label0
push 0
b label1
label0:
push 1
label1:
print
halt