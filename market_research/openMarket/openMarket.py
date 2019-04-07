import pandas as pd
import sqlite3

import matplotlib.pyplot as plt

cnx = sqlite3.connect("/home/ivan/projects/fbackend/report/marketOpen/report.db")

df=pd.read_sql_query(con=cnx, sql="select * from gaps where gapPct > 0.02")


pp=df.plot.scatter(x='gapPct', y='h1')

pp.grid(True, which='both')

plt.show()