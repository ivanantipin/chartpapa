# Generate the list of index files archived in EDGAR since start_year (earliest: 1993) until the most recent quarter
from bs4 import BeautifulSoup

flds = [
    'issuercik',
    'issuername',
    'issuertradingsymbol',
    'securitytitle',
    'transactionformtype',
    'transactioncode',
    'transactionshares',
    'transactionacquireddisposedcode',
    'directorindirectownership'
]


def extr(beausoup, tag):
    find = beausoup.find(tag)
    return "error" if find is None else find.text

def fetchNameValue(txt):
    soup = BeautifulSoup(txt)
    return map(lambda path: extr(soup, path), flds)

