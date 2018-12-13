import gzip
import shutil
import urllib
import os.path


import sqlite3

import sys

import filingsExtractor as fe

import os


startYear = 2010
endYear = 2018

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

dataFolder = '/hdd2-archive/edgar'

con = sqlite3.connect('{}/edgar_insider_filings.db'.format(dataFolder))
cur = con.cursor()

defs = ','.join(map(lambda x: x + " TEXT", fe.fldsWithDate))

cur.execute("CREATE TABLE IF NOT EXISTS insider_filings ( {} )".format(defs))
cur.execute("CREATE TABLE IF NOT EXISTS processed_files ( file_name TEXT )")



def isProcessed(checkFileName):
    cur.execute('SELECT * FROM processed_files WHERE file_name=?', [checkFileName])
    return not cur.fetchone() is None

print (isProcessed("not existent"))


insstmt = "insert into insider_filings ( {} ) VALUES ( {} )".format(','.join(map(lambda x: x,fe.fldsWithDate)), ','.join(map(lambda x: "?",fe.fldsWithDate)))
print ("ins stmmtt" + insstmt)

for y in range(startYear,endYear):
    for q in range(1,5):
        for m in range(1,4):
            for d in range(1,32):
                mm = (q - 1) * 3 + m
                fname = '{}{}{}'.format(y, str(mm).zfill(2), str(d).zfill(2))
                try:
                    if isProcessed(fname):
                        print ('file alread processed {}, continue '.format(fname))
                        continue

                    outFilezip = '{}/{}.gz'.format(dataFolder,fname)
                    fileUnzipped = '{}/{}'.format(dataFolder,fname)

                    url = 'https://www.sec.gov/Archives/edgar/Oldloads/{}/QTR{}/{}.gz'.format(y, q, fname)
                    print('retrieving {}'.format(url))
                    urllib.urlretrieve(url, outFilezip)
                    fileSize = os.path.getsize(outFilezip)
                    if fileSize < 100000:
                        print ('file too small {}, romoving {} '.format(fileSize, outFilezip))
                        os.remove(outFilezip)
                        cur.execute("insert into processed_files (file_name) values (?)", [fname])
                        con.commit()
                    else:
                        print('processisg {} '.format(fileUnzipped))
                        with gzip.open(outFilezip, 'rb') as f_in, open(fileUnzipped, 'wb') as f_out:
                            shutil.copyfileobj(f_in, f_out)
                        for tpl in fe.extract4Filings(fileUnzipped):
                            print (tpl)
                            cur.execute(insstmt, tpl)
                        cur.execute("insert into processed_files (file_name) values (?)", [fname])
                        con.commit()
                        os.remove(outFilezip)
                        os.remove(fileUnzipped)
                except:
                    print ("exception {} faied to process file {} retriing ".format(sys.exc_info()[0], fname))
con.close()
# os.remove(outFilezip)


