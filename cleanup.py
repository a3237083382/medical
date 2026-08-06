import os, glob
for f in glob.glob("D:/work/proj2/gen_run*.py") + glob.glob("D:/work/proj2/gen_final*.py") + glob.glob("D:/work/proj2/vfy*.py") + glob.glob("D:/work/proj2/gen_pdf*.py") + glob.glob("D:/work/proj2/test_*.py") + glob.glob("D:/work/proj2/fix*.py") + glob.glob("D:/work/proj2/*test*.txt"):
    try: os.remove(f)
    except: pass
print("Cleaned up")