from bs4 import BeautifulSoup
import requests
import re

from datetime import  datetime

def extr(row):
    dt=[i for i in list(row.children) if not isinstance(i, str)][0]
    div=[i for i in list(row.children) if not isinstance(i, str)][2]
    return (dt.get_text(),div.get_text())


def getDivs(ticker : str):
    r=requests.get('https://www.dohod.ru/ik/analytics/dividend/' + ticker, verify=False)
    soup = BeautifulSoup(r.text)
    aa=soup.find_all(text=re.compile('Дата закрытия реестра'))
    rows=list(aa[0].find_parent().find_parent().find_parent().children)
    rows=list(filter(lambda x : not isinstance(x,str),rows))
    return [extr(x) for x in rows]

epochStart=datetime(1970,1,1)

def parseDiv(row):
    try:
        return ((datetime.strptime(row[0],'%d.%m.%Y') - epochStart).days, float(row[1]))
    except:
        print('problem parsing ' + str(sys.exc_info()))
        return None



tickers=[
    'sber',
'lkoh',
'gazp',
'alrs',
'moex',
'gmkn',
'mgnt',
'rosn',
'tatn',
'sngs',
'chmf',
'aflt',
'sberp',
'nvtk',
'vtbr',
'hydr',
'nlmk',
'mfon',
'irao',
'mtss',
'magn',
'sngs',
'plzl',
'five',
'trnfp',
'mtlr',
'yndx',
'rual',
'fees',
'tatn',
'sibn',
'poly',
'mrkp',
'rtkm',
'enpl',
'afks']


import sqlite3
conn = sqlite3.connect('/ddisk/globaldatabase/md/meta.db')
c = conn.cursor()

c.execute("drop table if exists dividends")

c.execute("create table if not exists dividends (ticker varchar not null, dt integer not null, div double precision  not null, primary key (ticker,dt))")

stmt='insert or replace into dividends (ticker,dt,div) values (?,?,?)'

import sys

for t in tickers:
    try:
        rowRows = [parseDiv(r) for r in getDivs(t)]
        rowRows = [(t,r[0],r[1]) for r in rowRows if r]
        c.executemany(stmt, rowRows)

    except:
        print('problem with ticker ' + str(t) + str(sys.exc_info()))

conn.commit()
c.close()