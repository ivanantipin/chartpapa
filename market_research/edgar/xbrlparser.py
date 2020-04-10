from xbrl import XBRLParser

xbrl_parser = XBRLParser()
xbrl = xbrl_parser.parse(open("data/aapl-20170701.xml"))

gaap_obj = xbrl_parser.parseGAAP(xbrl, doc_date="20170701", ignore_errors=0)

print (gaap_obj)