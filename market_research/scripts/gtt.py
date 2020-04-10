import matplotlib.pyplot as plt
from pytrends.request import TrendReq

pytrend = TrendReq()

word='stocks'


pytrend.build_payload([word], cat=0, timeframe=f'2013-01-05 2013-03-05', geo='US')

df=pytrend.interest_over_time().plot()

pp=df.plot()

plt.show()



