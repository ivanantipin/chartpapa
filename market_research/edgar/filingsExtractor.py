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

fldsWithDate = flds + ['filing_date','xml']


def extr(beausoup, tag):
    find = beausoup.find(tag)
    return "error" if find is None else find.text

def fetchNameValue(txt):
    soup = BeautifulSoup(txt)
    return map(lambda path: extr(soup, path), flds)

def extract4Filings(inFile):
    chunk = []
    filingDate = ''
    form4 = False
    for l in open(inFile):
        if '<SUBMISSION>' in l:
            chunkStr = ''.join(chunk)
            if form4:
                submission = fetchNameValue(chunkStr)
                submission.append(filingDate)
                submission.append(chunkStr)
                yield submission
            chunk = [l]
            form4 = False
        else:
            if l.startswith('<FILING-DATE>'):
                filingDate = l.replace('<FILING-DATE>', '').strip()
            if l.strip() == '<FORM-TYPE>4':
                form4 = True
            chunk.append(l)
