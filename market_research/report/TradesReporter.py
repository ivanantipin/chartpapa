import datetime
import inspect
import os
import sqlite3

import ipywidgets as widgets
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import pytz
from IPython.display import HTML
from scipy.interpolate import griddata

pd.set_option('display.float_format', lambda x: '%.3f' % x)


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


def pctPnl(row: pd.Series):
    return  row.BuySell*(row['ExitPrice'] - row['EntryPrice']) / row['EntryPrice'] * 100


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



    def annRet(self, pnls , holdHours : float):
        return pnls.sum() / (holdHours/(24.0*365))

    def statToHtml(self, tradesDF: pd.DataFrame):
        tradesDF = tradesDF.copy()

        tradesDF['HoldTimeHours'] = (tradesDF['ExitDate'] - tradesDF['EntryDate']).map(
            lambda x: x / np.timedelta64(1, 'h'))

        buysDF = tradesDF[tradesDF.BuySell > 0]
        sellsDF = tradesDF[tradesDF.BuySell < 0]

        buys = buysDF.apply(pctPnl, axis=1)
        sells = sellsDF.apply(pctPnl, axis=1)

        buyDict = dict((k, None if len(buys) == 0 else v(buys)) for k, v in self.metricsMap.items())
        sellDict = dict((k, None if len(sells) == 0 else v(sells)) for k, v in self.metricsMap.items())

        buyDict['HoldTimeMeanHours'] = None if len(buys) == 0 else buysDF['HoldTimeHours'].mean()
        buyDict['HoldTimeMedianHours'] = None if len(buys) == 0 else buysDF['HoldTimeHours'].median()
        buyDict['AnnRet'] = self.annRet(buys, buysDF.HoldTimeHours.sum())


        sellDict['HoldTimeMeanHours'] = None if len(sells) == 0 else sellsDF['HoldTimeHours'].mean()
        sellDict['HoldTimeMedianHours'] = None if len(sells) == 0 else sellsDF['HoldTimeHours'].median()
        sellDict['AnnRet'] = None if len(sells) == 0 else self.annRet(sells, sellsDF.HoldTimeHours.sum())

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

    def __init__(self, filename: str, tz=pytz.UTC):
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
        self.load(filename)

    def load(self, filename, tableName="trades"):

        # Create your connection.
        cnx = sqlite3.connect(filename)

        self.trades = pd.read_sql_query(sql="SELECT * FROM trades",
                                        con=cnx,
                                        # 2013-04-08T10:00:00Z
                                        parse_dates={'EntryDate': '%Y-%m-%dT%H:%M:%SZ',
                                                     'ExitDate': '%Y-%m-%dT%H:%M:%SZ'})

        # print(self.trades)

        # date_parser=vect_str_to_datetime)
        # self.trades.dropna(inplace=True)
        self.trades['EntryDate'] = self.trades['EntryDate']
        # .map(lambda x: x.tz_localize(tz))
        self.trades['ExitDate'] = self.trades['ExitDate']
        # .map(lambda x: x.tz_localize(tz))
        self.sort()

        try:
            self.opts = pd.read_sql_query(sql="SELECT * FROM opts",
                                          con=cnx)
            self.opts.fillna(0, inplace=True)
            self.opts.sort_values(by=[self.opts.columns[0]], inplace=True)
        except:
            print('no opts')

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
        trades = self.trades
        ret = plt.figure()
        tr = trades.copy(True) if ticker == None else trades[trades.Ticker == ticker]
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
        return ret

    def chunks(self, lst, n):
        return [lst[i:i + n] for i in range(0, len(lst), n)]

    def subSub(self, tickers_in):
        cont = [widgets.Output() for i in range(len(tickers_in))]

        mc = MetricsCalculator()

        tab = widgets.Tab(children=cont)
        for i in range(len(tickers_in)):
            tab.set_title(i, tickers_in[i])

        for idx, out in enumerate(cont):
            with out:
                ticker = tickers_in[idx]
                self.plot_equity_d2d_for_ticker(ticker)
                plt.show()
                display(HTML(mc.statToHtml(self.trades[self.trades.Ticker == ticker]).to_html()))
        return tab

    def makeGenStat(self):
        out = widgets.Output()
        mc = MetricsCalculator()
        with out:
            display(HTML(mc.statToHtml(self.trades).to_html()))
            self.plot_equity_d2d_for_ticker()
            plt.show()
        return out

    def makeTickersTab(self):
        ch = [self.subSub(aa) for aa in self.chunks(self.tickers(), 10)]
        ch.insert(0, self.makeGenStat())
        widgets_tab = widgets.Tab(children=ch)
        widgets_tab.set_title(0, 'gen stat')
        return widgets_tab

    def plotSeasonalitiesPnls(self, pnls: pd.DataFrame):
        if pnls.size == 0:
            return

        seasonalMapFunc = self.seasonalMapFunc

        outputs = list([widgets.Output() for i in seasonalMapFunc])

        tab = widgets.Tab(children=outputs)

        for idx, mapTitle in enumerate(seasonalMapFunc):
            tab.set_title(idx, mapTitle)
            mapFun = seasonalMapFunc[mapTitle]
            grp = pnls.groupby(mapFun)
            with outputs[idx]:
                grp.aggregate(len).plot(ax=plt.gca(), color='red', marker='o')
                plt.gca().set_ylabel('Count', color='red')
                grp.aggregate(MetricsCalculator.pf).plot(ax=plt.gca().twinx(), color='green', marker='o')
                plt.gca().set_ylabel('Pf', color='green')
                #             plt.gca().get_yticklabels().set_color('green')
                plt.gcf().set_size_inches(15, 6)
                plt.show()
        return tab

    def plotSeasonalities(self):
        tr = self.trades.copy()
        tr.set_index(keys='EntryDate', inplace=True)
        return self.plotSeasonalitiesPnls(tr.Pnl)

    def getFactorCols(self):
        return list(self.trades.columns[self.trades.columns.get_loc(self.lastStaticColumnInTrades) + 1:].values)

    def plotFactors(self):
        cols = self.getFactorCols()
        if (len(cols) == 0):
            return
        cont = [widgets.Output() for i in range(1 + len(cols))]
        tab = widgets.Tab(children=cont)

        for idx, out in enumerate(cont):
            tab.set_title(idx, cols[idx - 1])
            cat = pd.cut(self.trades[cols[idx - 1]], 10, duplicates='drop')
            grp = self.trades['Pnl'].groupby(cat)

            with out:
                grp.aggregate(len).plot(ax=plt.gca(), color='red', marker='o')
                plt.gca().set_ylabel('Count', color='red')
                grp.aggregate(MetricsCalculator.pf).plot(ax=plt.gca().twinx(), color='green', marker='o')
                plt.gca().set_ylabel('Pf', color='green')
                #             plt.gca().get_yticklabels().set_color('green')
                plt.gcf().set_size_inches(15, 6)
                plt.show()
        return tab

    def plotOptimization(self):

        optCols = [i for i in self.opts.columns if i.startswith('opt_')]

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

    sys.path.append('/home/ivan/projects/chartpapa/market_research/report/')
    #
    # importlib.reload(tr)
    bs = BacktestResults("/home/ivan/projects/chartpapa/market_research/report/report.db")

    print(bs.trades)

    mc = MetricsCalculator()
    # display(displayTitle('Overall stat '))
    return mc.statToHtml(bs.trades)

# df = test()
