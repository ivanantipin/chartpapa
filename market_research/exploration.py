import pandas as pd
import sqlite3

import matplotlib.pyplot as plt

from pandas import Grouper
import numpy as np


cnx = sqlite3.connect("/home/ivan/projects/chartpapa/market_research/report_out/SiStrat/report.db")

df=pd.read_sql_query(con=cnx, sql="select * from si_stat where  idx =  34 and ret < 1")




pp=df.plot.scatter(x='gap', y='ret')

pp.grid(True, which='both')

plt.show()



#
# df['qd'] = pd.qcut(df['gap'], 6, duplicates='drop')
# df.boxplot(by='qd', column='per21')
# plt.show()

