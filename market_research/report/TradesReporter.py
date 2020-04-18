import datetime
import inspect
import os
import sqlite3

import ipywidgets as widgets
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from IPython.display import HTML
from IPython.display import display
from scipy.interpolate import griddata
from typing import List, Callable

pd.set_option('display.float_format', lambda x: '%.3f' % x)


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
    return row.BuySell * (row['ExitPrice'] - row['EntryPrice']) / row['EntryPrice'] * 100


def sharpe(pnls: pd.Series):
    return np.mean(pnls) / np.std(pnls)


def mean(pnls: pd.Series):
    return np.mean(pnls)


def pl(pnls: pd.Series):
    return np.sum(pnls)


def cnt(pnls: pd.Series):
    return len(pnls)


def maxStat(pnls: pd.Series):
    return max(pnls)


def minStat(pnls: pd.Series):
    return min(pnls)


def medianStat(pnls: pd.Series):
    return np.median(pnls)


def pf(pnls: pd.Series):
    a = pnls[pnls > 0].sum()
    b = pnls[pnls < 0].sum()
    if abs(b) < 0.001:
        return None
    return a / float(abs(b))


metricsMap = {'sharpe': sharpe, 'mean': mean, 'pl': pl, 'pf': pf, 'cnt': cnt,
              'max': maxStat, 'min': minStat, 'median': medianStat}


def annRet(pnls: pd.Series, holdHours: float):
    return pnls.sum() / (holdHours / (24.0 * 365))


def statToHtml(tradesDF: pd.DataFrame):
    tradesDF = tradesDF.copy()

    tradesDF['HoldTimeHours'] = (tradesDF['ExitDate'] - tradesDF['EntryDate']).map(
        lambda x: x / np.timedelta64(1, 'h'))

    buysDF = tradesDF[tradesDF.BuySell > 0]
    sellsDF = tradesDF[tradesDF.BuySell < 0]

    buys = buysDF.apply(pctPnl, axis=1)
    sells = sellsDF.apply(pctPnl, axis=1)

    buyDict = dict((k, None if len(buys) == 0 else v(buys)) for k, v in metricsMap.items())
    sellDict = dict((k, None if len(sells) == 0 else v(sells)) for k, v in metricsMap.items())

    buyDict['HoldTimeMeanHours'] = None if len(buys) == 0 else buysDF['HoldTimeHours'].mean()
    buyDict['HoldTimeMedianHours'] = None if len(buys) == 0 else buysDF['HoldTimeHours'].median()
    buyDict['AnnRet'] = annRet(buys, buysDF.HoldTimeHours.sum())

    sellDict['HoldTimeMeanHours'] = None if len(sells) == 0 else sellsDF['HoldTimeHours'].mean()
    sellDict['HoldTimeMedianHours'] = None if len(sells) == 0 else sellsDF['HoldTimeHours'].median()
    sellDict['AnnRet'] = None if len(sells) == 0 else annRet(sells, sellsDF.HoldTimeHours.sum())

    return pd.DataFrame(
        {
            'buyStat': pd.Series(buyDict),
            'sellStat': pd.Series(sellDict)
        })


def load(filename) -> pd.DataFrame:
    cnx = sqlite3.connect(filename)
    trades = pd.read_sql_query(sql="SELECT * FROM trades",
                               con=cnx,
                               # 2013-04-08T10:00:00Z
                               parse_dates={'EntryDate': '%Y-%m-%dT%H:%M:%SZ',
                                            'ExitDate': '%Y-%m-%dT%H:%M:%SZ'})

    trades.sort_values(by='EntryDate', inplace=True)
    return trades


def loadOpts(filename) -> pd.DataFrame:
    cnx = sqlite3.connect(filename)
    try:
        opts = pd.read_sql_query(sql="SELECT * FROM opts",
                                 con=cnx)
        opts.fillna(0, inplace=True)
        opts.sort_values(by=[opts.columns[0]], inplace=True)
        return opts
    except:
        print('no opts')


def plotHeatMap(XC, YC, ZC, title, xlab, ylab):
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


def plot_equity_d2d_for_ticker(trades: pd.DataFrame, title, figsize=(18, 7)):
    ret = plt.figure()
    tr = trades.copy(True)
    tr.set_index(keys='EntryDate', inplace=True)
    sells = tr[tr.BuySell == -1]['Pnl'].dropna()
    buys = tr[tr.BuySell == 1]['Pnl'].dropna()
    if len(sells) > 0:
        sells.cumsum().plot(color='red', marker='o')
    if len(buys) > 0:
        buys.cumsum().plot(color='blue', marker='o')
    curr_figure = plt.gcf()
    curr_figure.set_size_inches(figsize)
    plt.title(title)
    return ret


lastStaticColumnInTrades = 'MFE'

def tickers(trades: pd.DataFrame):
    return trades['Ticker'].unique()


def getModels(trades: pd.DataFrame):
    return trades['ModelName'].unique()


def chunks(lst, n):
    return [lst[i:i + n] for i in range(0, len(lst), n)]


def renderTrades(trades: pd.DataFrame, title: str):
    out = widgets.Output()
    with out:
        displayTitle(title)
        display(HTML(statToHtml(trades).to_html()))
        plot_equity_d2d_for_ticker(trades, title)
        plt.show()
    return out


def subSub(trades: pd.DataFrame, tickers_in: List[str]):
    cont = [widgets.Output() for i in range(len(tickers_in))]
    tab = widgets.Tab(children=cont)

    for i in range(len(tickers_in)):
        tab.set_title(i, tickers_in[i])

    for idx, out in enumerate(cont):
        with out:
            ticker = tickers_in[idx]
            plot_equity_d2d_for_ticker(trades[trades.Ticker == ticker], ticker)
            plt.show()
            display(HTML(statToHtml(trades[trades.Ticker == ticker]).to_html()))
    return tab


def makeTickersTab(trades: pd.DataFrame, title: str):
    # ch = [subSub(trades, tickers) for tickers in chunks(tickers(trades), 10)]
    # ch.insert(0, renderTrades(trades, 'gen stat'))
    widgets_tab = widgets.Tab(children=[renderTrades(trades, 'gen stat')])
    widgets_tab.set_title(0, f'Overall stat for {title}')
    return widgets_tab


def makeTickersTabTop(trades: pd.DataFrame):
    models = getModels(trades)
    ch = [makeTickersTab(trades[trades.ModelName == modelName], modelName) for modelName in models]
    ch.insert(0, renderTrades(trades, 'gen stat'))
    ret = widgets.Tab(children=ch)

    ret.set_title(0, 'Overall stat')

    for idx, out in enumerate(models):
        ret.set_title(idx + 1, models[idx])

    return ret

nonFactorCols=['TradeId','Pnl']

def getFactorCols(trades: pd.DataFrame):
    return [k for k in trades.columns if not k in nonFactorCols]


def plotFactors(trades: pd.DataFrame):
    if(trades is None):
        return widgets.Tab(children=[])

    cols = getFactorCols(trades)
    if (len(cols) == 0):
        return
    cont = [widgets.Output() for i in range(len(cols))]
    tab = widgets.Tab(children=cont)

    for idx, out in enumerate(cont):
        tab.set_title(idx, cols[idx])
        if cols[idx].endswith('_int'):
            grp=trades.groupby(cols[idx])['Pnl']
        else:
            trd=trades[trades[cols[idx]] != -1.0]
            cat = pd.cut(trd[cols[idx]], 10, duplicates='drop')
            grp =  trd['Pnl'].groupby(cat)
        with out:
            grp.aggregate(len).plot(ax=plt.gca(), color='red', marker='o')
            plt.gca().set_ylabel('Count', color='red')
            grp.aggregate(pf).plot(ax=plt.gca().twinx(), color='green', marker='o')
            plt.gca().set_ylabel('Pf', color='green')
            #             plt.gca().get_yticklabels().set_color('green')
            plt.gcf().set_size_inches(15, 6)
            plt.show()

    return tab


def loadWithFactors(filename : str, modelName : str ) -> pd.DataFrame:
    cnx = sqlite3.connect(filename)
    try:
        trades = pd.read_sql_query(sql=f"SELECT t.Pnl, f.* FROM trades t, {modelName}_factors f where t.TradeId = f.TradeId and t.ModelName = '{modelName}'",
                                   con=cnx,
                                   # 2013-04-08T10:00:00Z
                                   parse_dates={'EntryDate': '%Y-%m-%dT%H:%M:%SZ',
                                                'ExitDate': '%Y-%m-%dT%H:%M:%SZ'})

        return trades
    except:
        return None


def noOpFilter(data : pd.DataFrame)->pd.DataFrame:
    return data

def displayFactors(filename : str, models : List[str], fltr : Callable[[pd.DataFrame],pd.DataFrame] = noOpFilter):
    ch = [plotFactors(fltr(loadWithFactors(filename, model))) for model in models]
    widgets_tab = widgets.Tab(children=ch)
    for idx, mm in enumerate(models):
        widgets_tab.set_title(idx, mm)
    return widgets_tab


def plotOptimization(opts: pd.DataFrame):
    optCols = [i for i in opts.columns if i.startswith('opt_')]

    if len(optCols) == 2:
        fig = plt.figure(figsize=plt.figaspect(0.5))
        fig.set_size_inches([10, 10])
        X = opts[optCols[0]]
        Y = opts[optCols[1]]
        Z = opts['Pf']
        plotHeatMap(X, Y, Z, 'pf', optCols[0], optCols[1])
    elif len(optCols) == 1:
        fig = plt.figure(figsize=plt.figaspect(0.3))
        fig.set_size_inches([20, 5])
        X = opts[optCols[0]]
        Y = opts['Pf']
        plt.plot(X, Y)
        fig = plt.figure(figsize=plt.figaspect(0.3))
        Y1 = opts['Pnl']
        fig.set_size_inches([20, 5])
        ax = plt.plot(X, Y1)


def test():
    import sys
    sys.path.append('/home/ivan/projects/chartpapa/market_research/report/')
    fileName = "/home/ivan/projects/chartpapa/market_research/report/report.db"
    trades = load(fileName)
    models = getModels(trades)
    print(models)
    displayFactors(fileName, models)
    return trades

df=test()


