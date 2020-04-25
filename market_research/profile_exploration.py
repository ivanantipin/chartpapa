import sqlite3

import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

cnx = sqlite3.connect("/home/ivan/projects/chartpapa/market_research/report_out/NoName/report.db")

df=pd.read_sql_query(con=cnx, sql="select * from ZiggiStat where volumeRatio > 0 and volumeRation < 20")



np.log(df['volumeRatio']).plot.hist(bins=120)


plt.show()



#
# df['qd'] = pd.qcut(df['gap'], 6, duplicates='drop')
# df.boxplot(by='qd', column='per21')
# plt.show()

