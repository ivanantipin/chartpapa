import logging

from finam.export import Exporter, Market

# from finam.export import Exporter, Market, LookupComparator

"""
Full-on example displaying up-to-date values of some important indicators
"""

def main():
    exporter = Exporter()
    rub = exporter.lookup(code='MCFTR', market=Market.RUSSIAN_INDEXES)
    assert len(rub) == 1
    data = exporter.download(rub.index[0], market=Market.SHARES)
    print(data.tail(10))



if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    main()