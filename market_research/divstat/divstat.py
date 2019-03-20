import pandas as pd
import sqlite3

import matplotlib.pyplot as plt

cnx = sqlite3.connect("/home/ivan/projects/fbackend/report/divStrat0/stat.db")

df=pd.read_sql_query(con=cnx, parse_dates=['divDate'], sql="select divDate, ticker, gapPnl, intraPnl, gapPnl + intraPnl as pnl, gapPnl + intraPnl + divSize as realPnl, divSize, lastMonthReturn from divstat where days = 1")

pp=df.plot.scatter(x='divSize', y='pnl')

pp.grid(True, which='both')

plt.show()