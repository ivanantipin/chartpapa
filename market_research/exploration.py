import sqlite3

import matplotlib.pyplot as plt
import pandas as pd

import numpy as np

cnx = sqlite3.connect("/home/ivan/projects/chartpapa/market_research/report_out/report.db")

df=pd.read_sql_query(con=cnx, sql="select * from EdgarStat where name = 'sberp_2019-07-30T15:10:00Z'")

np.npv(0.07, )


ret = plt.figure()


df.set_index(keys='price', inplace=True)

sells = df['value']
sells.plot(color='red', marker='o')

plt.axvline(x=df['entryPrice'].iloc[0], ymin=0, ymax=df.value.max() )
plt.axvline(x=df['prevPrice'].iloc[0], ymin=0, ymax=df.value.max(), color='magenta' )
plt.axvline(x=df['levelFalse0'].iloc[0], ymin=0, ymax=df.value.max(), color='black')
plt.axvline(x=df['levelFalse1'].iloc[0], ymin=0, ymax=df.value.max(),  color='black')

plt.axvline(x=df['levelTrue0'].iloc[0], ymin=0, ymax=df.value.max(), color='green')
plt.axvline(x=df['levelTrue1'].iloc[0], ymin=0, ymax=df.value.max(),  color='green')


currFigure = plt.gcf()
currFigure.set_size_inches((18, 7))


plt.show()



#
# df['qd'] = pd.qcut(df['gap'], 6, duplicates='drop')
# df.boxplot(by='qd', column='per21')
# plt.show()

