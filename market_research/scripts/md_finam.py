import market_research.finam.export as fe
import market_research.utils as u

df=u.exporter.getMeta()

md=u.getMd([('ALRS', fe.Market.SHARES)], fe.Timeframe.MINUTES10)

print(md)