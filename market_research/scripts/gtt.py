from pandas._libs.tslibs.timestamps import Timestamp
from pytrends.request import TrendReq
import sqlite3
import datetime as dt
import numpy as np

import pandas as pd
import sqlite3

import matplotlib.pyplot as plt

from pandas import Grouper
import numpy as np


pytrend = TrendReq()

word='stocks'


pytrend.build_payload([word], cat=0, timeframe=f'2013-01-05 2013-03-05', geo='US')

df=pytrend.interest_over_time().plot()

pp=df.plot()

plt.show()



