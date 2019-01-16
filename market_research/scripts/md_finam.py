import finam.export as fe
import utils as u

df=u.exporter.getMeta()

md=u.getMd([('ALRS', fe.Market.SHARES)], fe.Timeframe.MINUTES10)

print(md)