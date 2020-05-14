import sqlite3
from datetime import timedelta
import pandas as pd
pd.set_option('display.float_format', lambda x: '%.3f' % x)
import matplotlib.pyplot as plt

# Create your connection.
cnx = sqlite3.connect("./report.db")

trades = pd.read_sql_query(sql="SELECT * FROM support_resistance",
                           con=cnx).dropna()

trades.initialDate = pd.to_datetime(trades.initialDate, unit='s')
trades.startDate = pd.to_datetime(trades.startDate, unit='s')
trades.endDate = pd.to_datetime(trades.endDate, unit='s')

dfs={}

mdcnx = sqlite3.connect("/ddisk/globaldatabase/md/MOEX/Min10.db")

def getDf(ticker: str ):
    if not ticker in dfs:
        tmp : pd.DataFrame =pd.read_sql_query('select * from ' + ticker,con=mdcnx)
        tmp.set_index(pd.to_datetime(tmp.dt, unit='ms'), inplace=True)
        tmp.drop(columns=['dt'])
    return tmp




def showLevel(no : int):
    rec=trades.iloc[no, :]
    df=getDf(rec.ticker)

    df_l=df[rec.initialDate - pd.Timedelta(minutes=1240*2):rec.endDate + pd.Timedelta(minutes=1240*2)]
    ax=df_l.c.plot()
    ax.plot([rec.initialDate,rec.startDate],[rec.level,rec.level], color='blue')
    ax.plot([rec.startDate,rec.endDate],[rec.level,rec.level], color='green')
    plt.show()