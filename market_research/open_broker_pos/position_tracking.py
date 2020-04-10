import sqlite3

import pandas as pd
import requests
from lxml import etree

dataFolder = '/hdd2-archive/edgar/'

con = sqlite3.connect('{}/positions.sqlite'.format(dataFolder))
cur = con.cursor()

cur.execute('create table if not EXISTS positions(ticker TEXT,board TEXT, position int,price_open decimal,target decimal)')


pd.read_sql_query('SELECT * FROM positions')


cur.execute()
for r in cur.fetchall():
    url = 'https://iss.moex.com/iss/engines/stock/markets/shares/securities/{}.xml'.format(r[0])
    content = requests.get(url).content
    tree = etree.XML(content)
    tree.xpath('//data[@id="marketdata"]/rows/row[@BOARDID="TQBR"]/@LAST')[0]
    print (tree)



