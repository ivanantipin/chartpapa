import sqlite3

import pandas as pd

from statsmodels.tsa.vector_ar.vecm import coint_johansen



pd.set_option('display.float_format', lambda x: '%.3f' % x)


mdcnx = sqlite3.connect("/ddisk/globaldatabase/md/MOEX/Min10.db")

def getDf(t0: str, t1 : str ):
    tmp : pd.DataFrame =pd.read_sql_query(f'select a.dt, a.c as {t0}, b.c as {t1} from {t0} a, {t1} b where a.dt = b.dt ',con=mdcnx)
    tmp.set_index(pd.to_datetime(tmp.dt, unit='ms'), inplace=True)
    tmp.drop(columns=['dt'], inplace=True)
    return tmp




print(getDf('sberp', 'sber'))



