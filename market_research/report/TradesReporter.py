import datetime
import inspect
import os
import sqlite3

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import pytz
from IPython.display import HTML
from scipy.interpolate import griddata


class BacktestStats(object):
    """
    Struct that holds common statistic metrics for the list of trades.
    """

    def __init__(self):
        self.netPnl = 0
        self.profitFactor = 0
        self.sharpe = 0
        self.avgTrade = 0
        self.avgTradePct = 0
        self.nTrades = 0
        self.percentWin = 0
        self.maxDD = 0
        self.maxDDasPct = 0
        self.recoveryFactor = 0
        self.equity = None

    def asdict(self):
        return {
            'netPnl': self.netPnl,
            'profitFactor': self.profitFactor,
            'sharpe': self.sharpe,
            'avgTrade': self.avgTrade,
            'avgTradePct': self.avgTradePct,
            'nTrades': self.nTrades,
            'percentWin': self.percentWin,
            'maxDD': self.maxDD,
            'maxDDasPct': self.maxDDasPct,
            'recoveryFactor': self.recoveryFactor,
        }


def _str_to_datetime(d):
    try:
        return datetime.datetime.strptime(d, '%d.%m.%Y %H:%M:%S')
    except:
        return None


def vect_str_to_datetime(d):
    return np.vectorize(_str_to_datetime)(d)


def _get_main_script_filename():
    framelist = inspect.stack()
    return os.path.abspath(inspect.getfile(framelist[-1][0]))


def displayTitle(title):
    return HTML('<h3 align="center">$T</h3>'.replace('$T', title))


class MetricsCalculator:
    @staticmethod
    def sharpe(pnls):
        return np.mean(pnls) / np.std(pnls)

    @staticmethod
    def mean(pnls):
        return np.mean(pnls)

    @staticmethod
    def pl(pnls):
        return np.sum(pnls)

    @staticmethod
    def cnt(pnls):
        return len(pnls)

    @staticmethod
    def maxStat(pnls):
        return max(pnls)

    @staticmethod
    def minStat(pnls):
        return min(pnls)

    @staticmethod
    def medianStat(pnls):
        return np.median(pnls)

    @staticmethod
    def pf(pnls):
        a = pnls[pnls > 0].sum()
        b = pnls[pnls < 0].sum()
        if abs(b) < 0.001:
            return None
        return a / float(abs(b))

    def __init__(self):
        self.metricsMap = {'sharpe': self.sharpe, 'mean': self.mean, 'pl': self.pl, 'pf': self.pf, 'cnt': self.cnt,
                           'max': self.maxStat, 'min': self.minStat, 'median': self.medianStat}

    def statToHtml(self, tradesDF):
        tradesDF['HoldTimeHours'] = (tradesDF['ExitDate'] - tradesDF['EntryDate']).map(
            lambda x: x / np.timedelta64(1, 'h'))

        buysDF = tradesDF[tradesDF.BuySell > 0]
        sellsDF = tradesDF[tradesDF.BuySell < 0]

        buys = buysDF['Pnl'].dropna()
        sells = sellsDF['Pnl'].dropna()

        buyDict = dict((k, None if len(buys) == 0 else v(buys)) for k, v in self.metricsMap.items())
        sellDict = dict((k, None if len(sells) == 0 else v(sells)) for k, v in self.metricsMap.items())

        buyDict['HoldTimeMeanHours'] = None if len(buys) == 0 else buysDF['HoldTimeHours'].mean()
        buyDict['HoldTimeMedianHours'] = None if len(buys) == 0 else buysDF['HoldTimeHours'].median()
        sellDict['HoldTimeMeanHours'] = None if len(sells) == 0 else sellsDF['HoldTimeHours'].mean()
        sellDict['HoldTimeMedianHours'] = None if len(sells) == 0 else sellsDF['HoldTimeHours'].median()

        return pd.DataFrame(
            {
                'buyStat': pd.Series(buyDict),
                'sellStat': pd.Series(sellDict)
            })

        # .to_html()


class BacktestResults(object):
    """
    Class that wraps backtest results (contains pandas.DataFrame with trades and some methods to calc stats, plot graphs e.t.c.)
    """


    def __init__(self):
        """
        public self.trades attribute contains a pandas.DataFrame with the following columns:

           'Ticker'      - name of the ticker.
           'BuySell'        - BuySell of the trade, 1 for Long, -1 for Short.
           'EntryDate'   - entry date.
           'EntryPrice'  - entry price.
           'ExitDate'    - exit date.
           'ExitPrice'   - exit price.
           'Pnl'      - Pnl.
           'nContracts'  - number of contracts traded.
        """
        self.trades = pd.DataFrame()
        self.seasonalMapFunc = {'weekday': lambda x: x.weekday(), 'month': lambda x: x.month, 'hour': lambda x: x.hour}
        self.seasonalAggFunc = {'pf': MetricsCalculator.pf, 'cnt': len}
        self.seasonalAggColors = ['r', 'g']
        self.lastStaticColumnInTrades = 'MFE'

    def load(self, filename, tz=pytz.UTC):

        # Create your connection.
        cnx = sqlite3.connect(filename)

        self.trades = pd.read_sql_query(sql="SELECT * FROM trades",
                                        con=cnx ,
                                        parse_dates=['EntryDate', 'ExitDate'])

        # print(self.trades)

        # date_parser=vect_str_to_datetime)
        self.trades.dropna(inplace=True)
        self.trades['EntryDate'] = self.trades['EntryDate'].map(lambda x: x.tz_localize(tz))
        self.trades['ExitDate'] = self.trades['ExitDate'].map(lambda x: x.tz_localize(tz))
        self.sort()

    def loadOpts(self, filename):
        self.opts = pd.read_csv(filename, index_col=False, sep=';')
        self.opts.fillna(0, inplace=True)
        self.opts.sort_values(by=[self.opts.columns[0]], inplace=True)

    def sort(self):
        """
        Sort trades by EntryDate (assumes self.trades.index contains EntryDate).
        """
        if not self.trades is None:
            self.trades.sort_values(by='EntryDate', inplace=True)

    def plotHeatMap(self, XC, YC, ZC, title, xlab, ylab):
        fig = plt.figure(figsize=plt.figaspect(0.5))
        fig.set_size_inches([10, 10])

        xi = np.linspace(XC.min(), XC.max(), 100)
        yi = np.linspace(YC.min(), YC.max(), 100)

        # VERY IMPORTANT, to tell matplotlib how is your data organized
        zi = griddata((XC, YC), ZC, (xi[None, :], yi[:, None]))

        plt.imshow(zi, origin='lower')

        iii = range(0, len(xi), 10)
        plt.xticks(iii, map("{0:.0f}".format, xi[iii]))

        iii = range(0, len(yi), 10)
        plt.yticks(iii, map("{0:.0f}".format, yi[iii]))

        plt.title(title)

        plt.xlabel(xlab)
        plt.ylabel(ylab)

        plt.colorbar()

    def __repr__(self):
        """
        Return string with common statistical metrics.
        """
        return str(self.CalcStats())

    def tickers(self):
        return self.trades['Ticker'].unique()

    def plot_equity_d2d_for_ticker(self, ticker=None, figsize=(18, 7)):
        plt.figure()
        tr = self.trades.copy(True) if ticker == None else self.trades[self.trades.Ticker == ticker]
        title = 'All tickers' if ticker == None else 'Ticker=' + ticker
        assert len(tr) > 0, 'No trades for ticker present ' + ticker
        tr.set_index(keys='EntryDate', inplace=True)

        sells = tr[tr.BuySell == -1]['Pnl'].dropna()
        buys = tr[tr.BuySell == 1]['Pnl'].dropna()
        if len(sells) > 0:
            sells.cumsum().plot(color='red', marker='o')
        if len(buys) > 0:
            buys.cumsum().plot(color='blue', marker='o')
        currFigure = plt.gcf()
        currFigure.set_size_inches(figsize)
        plt.title(title)

    def plotSeasonalitiesPnls(self, pnls : pd.DataFrame):
        if pnls.size == 0:
            return
        fig, axes = plt.subplots(nrows=len(self.seasonalMapFunc), ncols=1)
        fig.set_size_inches(25, 25)
        fig.subplots_adjust(right=0.75)
        currAxes = 0
        for mapTitle, mapFun in self.seasonalMapFunc.items():
            grp = pnls.groupby(mapFun)
            currAgg = 0
            for aggTitle, aggFun in self.seasonalAggFunc.items():
                if currAgg == 0:
                    ax = axes[currAxes]
                else:
                    ax = axes[currAxes].twinx()
                '''if currAgg == 2:
                    ax.spines['right'].set_position(('axes', 1.2))
                    ax.set_frame_on(True)
                    ax.patch.set_visible(False)
                    ax.set_ylim((0, 2))
                '''
                for tl in ax.get_yticklabels():
                    tl.set_color(self.seasonalAggColors[currAgg])
                    ax.set_ylabel(aggTitle, color=self.seasonalAggColors[currAgg])
                grp.aggregate(aggFun).plot(ax=ax, title=mapTitle, color=self.seasonalAggColors[currAgg], marker='o')
                currAgg += 1
            currAxes += 1

    def plotSeasonalities(self):
        tr = self.trades.copy()
        tr.set_index(keys='EntryDate', inplace=True)
        self.plotSeasonalitiesPnls(tr.Pnl)

    def getFactorCols(self):
        return list(self.trades.columns[self.trades.columns.get_loc(self.lastStaticColumnInTrades) + 1:].values)

    def plotFactors(self):
        cols = self.getFactorCols()
        if len(cols) > 0:
            rn = int(len(cols) / 3)
            ncols = len(cols) if rn == 0 else 3
            if len(cols) % 3 != 0:
                rn = rn + 1
            g, axs = plt.subplots(nrows=rn, ncols=ncols)
            axs = np.reshape(axs, -1)
            g.set_size_inches(30, rn * 4)
            for i in range(0, len(cols)):
                try:
                    cat = pd.qcut(self.trades[cols[i]], 5)
                    self.trades['Pnl'].groupby(cat).aggregate(np.sum).plot(ax=axs[i])
                except ValueError:
                    print('error ' + cols[i])
                    continue

    def plotOptimization(self):
        optCols = [self.opts.columns[0]] if self.opts.columns[1] == 'Pf' else [self.opts.columns[0],
                                                                               self.opts.columns[1]]
        # optCols=filter(lambda x : len(x) > 0,optCols.split(';'))
        dfOpt = self.opts
        if len(optCols) == 2:
            fig = plt.figure(figsize=plt.figaspect(0.5))
            fig.set_size_inches([10, 10])
            X = dfOpt[optCols[0]]
            Y = dfOpt[optCols[1]]
            Z = dfOpt['Pf']
            self.plotHeatMap(X, Y, Z, 'pf', optCols[0], optCols[1])
        elif len(optCols) == 1:
            fig = plt.figure(figsize=plt.figaspect(0.3))
            fig.set_size_inches([20, 5])
            X = dfOpt[optCols[0]]
            Y = dfOpt['Pf']
            plt.plot(X, Y)
            fig = plt.figure(figsize=plt.figaspect(0.3))
            Y1 = dfOpt['Pnl']
            fig.set_size_inches([20, 5])
            ax = plt.plot(X, Y1)



def test():
    import sys


    sys.path.append('/home/ivan/projects/fbackend/market_research/report/')
    #
    # importlib.reload(tr)
    bs = BacktestResults()
    fname = '/home/ivan/projects/fbackend/market_research/report/report.db'
    bs.load(fname)

    print(bs.trades)


    mc=MetricsCalculator()
    # display(displayTitle('Overall stat '))
    mc.statToHtml(bs.trades)

    bs.plotFactors()
    # bs.plotSeasonalities()
    # plt.show()

test()

# bs.plot_equity_d2d_for_ticker(ticker='RSX')
# plt.show()
