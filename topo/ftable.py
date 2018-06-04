import sys
import subprocess
import os

node = sys.argv[0]
fname = node + "_rt"
os.system("ip route > " + fname)
with open(fname) as f:
     data = [l for l in f.read().split('\n') if l.strip() != '']
     f1 = open(node+"_routes","w+")
     for block in data:
         newline = block.strip()
         l = newline.split(' ')
         ln = len(l)
         e = newline.split(' ')[0].strip()
         c = newline.split(' ')[ln-1].strip()
         f1.write(e+" "+c+'\n')
     f1.close()
