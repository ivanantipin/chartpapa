import datetime as dt
import sqlite3

import numpy as np
from pandas._libs.tslibs.timestamps import Timestamp
from pytrends.request import TrendReq

pytrend = TrendReq()

dbPath = '/ddisk/globaldatabase/md/meta.db'




conn = sqlite3.connect(dbPath)
c = conn.cursor()

# c.execute("drop table if exists google_trends")

c.execute('create table if not exists google_trends_15d (word varchar not null, start integer not null, dt integer not null, idx int  not null, primary key (word,start,dt))')

stmt = 'insert or replace into google_trends_15d (word,start, dt,idx) values (?,?,?,?)'

epochStart = dt.datetime(1970, 1, 1)

word='sell stocks'

def toEpoch(ts : Timestamp):
    return  np.long((ts - epochStart).total_seconds())

kw_list = [word]

# c.execute('delete from google_trends where word = ?', [word])


def fetchData(start : str, end : str):
    pytrend.build_payload([word], cat=0, timeframe=f'{start} {end}', geo='US', gprop='')
    return pytrend.interest_over_time()

d = dt.datetime(2019, 1, 1)

while d < dt.datetime(2020, 1, 1):
    try:
        st=d - dt.timedelta(days=5)
        end = d + dt.timedelta(days=15)
        df=fetchData(st.strftime("%Y-%m-%d"), end.strftime("%Y-%m-%d"))
        rows= [(word, toEpoch(st), toEpoch(r[0]), r[1])  for r in list(df.itertuples(index=True))]
        c.executemany(stmt, rows)
        print(f'inserted data for {st.strftime("%Y-%m-%d")} to {end.strftime("%Y-%m-%d")} rows {len(rows)}')
        conn.commit()
        d = end
    except:
        print('exception')


c.close()