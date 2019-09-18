import pandas as pd
import sqlite3

import matplotlib.pyplot as plt

from pandas import Grouper
import numpy as np


cnx = sqlite3.connect("/home/ivan/projects/chartpapa/market_research/report_tmp/stat.db")

df=pd.read_sql_query(con=cnx, sql="select ret*100 as ret, diff3 from highvolume")

# pp=df.plot.scatter(x='diff3', y='ret')

# pp.grid(True, which='both')

# plt.show()




df['qd'] = pd.qcut(df['diff3'], 6, duplicates='drop')
df.boxplot(by='qd', column='ret')
plt.show()

