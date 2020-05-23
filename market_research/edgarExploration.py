import sqlite3

import matplotlib.pyplot as plt
import pandas as pd

cnx = sqlite3.connect("/home/ivan/projects/chartpapa/market_research/report_out/report.db")


df=pd.read_sql_query(con=cnx, sql="select idx, ret from EdgarStats where prev10 > 0.05")

print(df.size)

mean = df.groupby('idx')['ret'].mean()
std = df.groupby('idx')['ret'].std()

plt.errorbar(mean.index, mean, xerr=0.5, yerr=2*std, linestyle='')

plt.plot(mean)
plt.show()

