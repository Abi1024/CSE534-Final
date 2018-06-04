import sys
import subprocess
import os

os.system("ip route > R1_rt")
weightfile = 'R1_rt'
with open(weightfile) as f:
     data = [l for l in f.read().split('\n') if l.strip() != '']
     f1 = open("R1_routes","w+")
     for block in data:
         newline = block.strip()
         l = newline.split(' ')
         ln = len(l)
         e = newline.split(' ')[0].strip()
         c = newline.split(' ')[ln-1].strip()
         f1.write(e+" "+c+'\n')
     f1.close()
