import pandas as pd
import sqlite3

import matplotlib.pyplot as plt

cnx = sqlite3.connect("/home/ivan/projects/fbackend/report/divStrat0/stat.db")

df=pd.read_sql_query(con=cnx, sql="select * from gaps")

pp=df.plot.scatter(x='gapPct', y='fstHourPct')

pp.grid(True, which='both')

plt.show()