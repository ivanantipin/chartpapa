import finam.export as fe
import utils as u

df=u.exporter.getMeta()

md=u.getMd([('ROSN', fe.Market.SHARES)], fe.Timeframe.DAILY)

