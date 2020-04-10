import sqlite3
import sys
import time
import typing
from functools import reduce

# from fredapi import Fred
import numpy as np
import pandas as pd
import requests
from lxml import etree

from finam.export import Timeframe, Exporter, Market

julTabs = ['acc150592','acc150592i']
nadiaTabs = ['acc172926']
ivanTabs = ['acc34887','acc34887i', 'acc172209']


def getPrice(board, ticker):
    if ticker == 'RUB':
        return 1.0
    try:
        url = 'https://iss.moex.com/iss/engines/stock/markets/shares/securities/{}.xml'.format(ticker)
        content = requests.get(url).content
        tree = etree.XML(content)
        bid = float(tree.xpath('//data[@id="marketdata"]/rows/row[@BOARDID="{}"]/@BID'.format(board))[0])
        offer = float(tree.xpath('//data[@id="marketdata"]/rows/row[@BOARDID="{}"]/@OFFER'.format(board))[0])
        return (bid + offer)/2
    except:
        print ("exception {} retrieving {} ".format(sys.exc_info()[0], ticker))
        return 0.0



exporter = Exporter()



def calcVola(ticker):
    rub = exporter.lookup(code=ticker, market=Market.SHARES)
    print("calculating vola for ticker {}".format(ticker))
    df = exporter.download(rub.index[0], market=Market.SHARES, timeframe=Timeframe.WEEKLY)

    liq=round((df['<VOL>']*df['<CLOSE>']).mean()/1000000,1)

    closes = df['<CLOSE>']
    vola = closes.tail(
        40).pct_change().std() * np.sqrt(40)
    return (vola,closes.iat[-1],liq)

def_instr=[('BZ',Market.COMMODITIES),('RTSI',Market.INDEXES),('INX',Market.INDEXES),('USDRUB_SPT',Market.CURRENCIES)]

def getMd(instr, timeframe : Timeframe)->pd.DataFrame:

    def toDf(inTpl):
        rub = exporter.lookup(code=inTpl[0], market=inTpl[1])
        time.sleep(1)
        ret = exporter.download(rub.index[0], market=inTpl[1], timeframe=timeframe)
        return ret.rename(columns={'<CLOSE>': inTpl[0] + '_close'})[[inTpl[0] + '_close']]

    dfs=map(lambda x : toDf(x),instr)

    return reduce(lambda left,right: pd.merge(left,right,left_index=True, right_index=True), dfs)


def updateMd(dbpath : str):
    conn = sqlite3.connect(dbpath)
    cur = conn.cursor()
    cur.execute("CREATE TABLE IF NOT EXISTS md ( ticker TEXT primary key , vola number, price number)")

    df=pd.read_sql('select ticker from targets', conn)

    def updateVola(ticker: str):
        try:
            (vola,price,liq) = calcVola(ticker)
            time.sleep(1)
            print("updating vola {} {}".format(ticker, vola))
            cur.execute("delete from md where ticker=?", [ticker])
            cur.execute("insert into md (TICKER,VOLA,PRICE,liquidity) values (?,?,?,?)", [ticker, vola,price,liq])
            conn.commit()
        except:
            print('problem' + ticker)

    df['ticker'].apply(updateVola)





def getAnnualized():
    conn = sqlite3.connect('position.db')
    df = pd.read_sql(
        'select t.ticker, t.target, t.horizon, md.price, md.vola  from targets t, md where t.ticker = md.ticker', conn)

    # df['vola_koeff'] = 1 - (df.vola - df.vola.min())/(df.vola.max() - df.vola.min())

    df['upside'] = (df['target'] - df['price'])/df['price']

    df['annual_upside']=df.apply(lambda x : np.power(1 + x.upside, 1/x.horizon), axis=1)

    # df['t_weight'] = df.annual_upside/df.vola/df.annual_upside.sum()

    del df['upside']

    return df


def getPositions(tableNames,dbpath : str) -> pd.DataFrame:
    conn = sqlite3.connect(dbpath)
    func={'position' : sum, 'price' : 'mean'}
    dfs = map(lambda x : pd.read_sql('select * from {} '.format(x), conn), tableNames)
    return pd.concat(dfs).groupby(['ticker'] , as_index=False).agg(func)

def getPortfolio(tabs : typing.List[str], dbpath : str):
    pos = getPositions(tabs,dbpath)
    df = pos.drop('price', 1).merge(getAnnualized(), on=['ticker'])

    wgt=df.position*df.price

    df['wght']=wgt/wgt.sum()

    # print(df.sort_values(by='wght', ascending=False))
    # print('sum ' + str((df.position*df.price).sum()))
    df['money']= df.position*df.price
    return df.sort_values(by='money',ascending=True)


# fred = Fred(api_key='d9ab0788d00a3be2b4e1a552d57fc467')
#
# def getFredMd(name : str):
#     return fred.get_series(name)
