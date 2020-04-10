import os
import sqlite3

from lxml import etree

flds = [
    'ticker TEXT',
    'position INT',
    'price NUMBER'
]

def getTickers(tree):
    items = tree.xpath('//broker_report/spot_portfolio_security_params/item')
    return dict(map(lambda x : [x.attrib['security_name'],x.attrib['ticker']] , items))

def importFile(path,dbpath):
    conn = sqlite3.connect(dbpath)
    cur = conn.cursor()
    tableName = os.path.basename(path).replace('.xml', '')
    cur.execute("DROP TABLE IF EXISTS {}".format(tableName))
    cur.execute("CREATE TABLE IF NOT EXISTS {} ( ticker TEXT, position INT, price NUMBER )".format(tableName))
    with open(path, 'r', encoding='windows-1251') as myfile:
        data = myfile.read().replace(' encoding="windows-1251"','')
        tree = etree.XML(data)
        items = tree.xpath('//broker_report/spot_assets/item')
        tickers = getTickers(tree)
        for i in items:
            ticker = tickers.get(i.attrib['asset_name'],i.attrib['asset_code'])
            vals = [ticker, i.attrib['closing_position_plan'], i.attrib['settlement_price']]
            print(vals)
            cur.execute("insert into {} (ticker,position,price) values (?,?,?)".format(tableName), vals)
        conn.commit()
        print(getTickers(tree))
    conn.close()


def updatePos(path : str, dbpath : str):
    for filename in os.listdir(path):
        importFile(path + '/' + filename, dbpath)
        print(filename)