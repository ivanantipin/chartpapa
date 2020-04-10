import sqlite3

import matplotlib.pyplot as plt
import pandas as pd

cnx = sqlite3.connect("/home/ivan/projects/chartpapa/market_research/report_out/RegressionModel/report.db")

df=pd.read_sql_query(con=cnx, sql="select * from RegParam")




pp=df.plot()

plt.show()



#
# df['qd'] = pd.qcut(df['gap'], 6, duplicates='drop')
# df.boxplot(by='qd', column='per21')
# plt.show()

