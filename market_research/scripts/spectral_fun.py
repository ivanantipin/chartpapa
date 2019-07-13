from __future__ import division
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

df=pd.read_csv('/ddisk/globaldatabase/spy.csv',names=['ts','close'],index_col='ts')

ps = np.abs(np.fft.fft(df.close.pct_change(1)))**2


time_step = 1 / 30
freqs = np.fft.fftfreq(df.close.size, time_step)
idx = np.argsort(freqs)

plt.plot(freqs[idx], ps[idx])

plt.show()