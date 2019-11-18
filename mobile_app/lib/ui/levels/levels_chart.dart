import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/material.dart' as prefix0;
import 'package:intl/intl.dart';
import 'package:quiver/iterables.dart' as prefix2;
import 'package:rxdart/rxdart.dart';
import 'package:simple_material_app/bloc/levels_bloc.dart';
import 'package:simple_material_app/bloc/price_bloc.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:syncfusion_flutter_charts/charts.dart';
import 'package:tuple/tuple.dart';

class LSnap {
  final List<Level> levels;
  final List<OhlcTO> history;
  final OhlcTO currentPrice;
  final Tuple2<double, double> range;

  LSnap(this.levels, this.history, this.currentPrice, this.range);
}

class LevelsChartWidget extends StatelessWidget {
  final String ticker;
  final LevelsBloc levelsBloc;
  final PriceBloc priceBloc;

  Observable<LSnap> lsnap;

  LevelsChartWidget(this.ticker, this.levelsBloc, this.priceBloc) {
    var latestStream = priceBloc.getLatestPrice(ticker);

    var histPrice = priceBloc.getHistPrice(ticker).map((List<OhlcTO> hist) {
      var start = DateTime.now().millisecondsSinceEpoch;
      var compare = (OhlcTO a, OhlcTO b) => a.close.compareTo(b.close);
      var minOh = prefix2.min(hist, compare);
      var maxOh = prefix2.max(hist, compare);
      print("hist took ${(DateTime.now().millisecondsSinceEpoch - start)/1000.0}");
      return Tuple3(hist, minOh, maxOh);
    });

    Observable<List<Level>> processedLevels = levelsBloc.getLevelsForTickers(ticker).map((levels) {

      var start = DateTime.now().millisecondsSinceEpoch;

      var compare = (Level a, Level b) {
        return a.level.value.compareTo(b.level.value);
      };

      var minLevel = prefix2.min(levels.levels, compare);
      var maxLevel = prefix2.max(levels.levels, compare);

      var diff = maxLevel.level.value - minLevel.level.value;

      var nl = [...levels.levels];
      nl.sort(compare);

      var divider = 50;

      var last = minLevel.level.value - diff / (divider - 1);

      nl = nl.where((f) {
        var ret = f.level.value > last + diff / divider;
        if (ret) {
          last = f.level.value;
        }
        return ret;
      }).toList();

      print("heavy took ${(DateTime.now().millisecondsSinceEpoch - start)/1000.0}");
      return nl;
    });

    this.lsnap = Observable.combineLatest3(processedLevels, latestStream, histPrice,
        (List<Level> levels, OhlcTO latest, Tuple3<List<OhlcTO>, OhlcTO, OhlcTO> hist) {
      List<OhlcTO> histPrices = hist.item1;

      var rangedLevels = levels.where((l) {
        return l.level.value >= hist.item2.close * 0.8 && l.level.value <= hist.item3.close * 1.2;
      }).map((l) {
        return l.copyWith((inl) {
          inl.level = inl.level.copyWith((point) {
            point.timestamp = prefix2.max([histPrices.first.timestamp, point.timestamp]);
          });
          return inl;
        });
      }).toList();

      return LSnap(rangedLevels, histPrices, latest, Tuple2(hist.item2.low, hist.item3.high));
    });
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder(
      stream: lsnap,
      builder: (context, AsyncSnapshot<LSnap> snapshot) {
        if (snapshot.hasData) {
          return Container(
              color: Colors.white,
              height: 300,
              padding: prefix0.EdgeInsets.only(right: 10),
              child: SfCartesianChart(
                plotAreaBorderWidth: 0,
                title: ChartTitle(text: ticker.toUpperCase()),
                legend: Legend(isVisible: true, overflowMode: LegendItemOverflowMode.wrap),
                primaryXAxis: DateTimeAxis(
                  majorGridLines: MajorGridLines(width: 0),
                  dateFormat: DateFormat.yMd(),
                  title: AxisTitle(text: 'Time'),
                ),
                primaryYAxis: NumericAxis(
                    rangePadding: ChartRangePadding.round,
                    decimalPlaces: 4,
                    minimum: snapshot.data.range.item1 * 0.8,
                    maximum: snapshot.data.range.item2 * 1.2,
                    axisLine: AxisLine(width: 0),
                    majorTickLines: MajorTickLines(color: Colors.transparent)),
                series: getLineSeries(snapshot.data),
                tooltipBehavior: TooltipBehavior(enable: true),
              ));
        } else if (snapshot.hasError) {
          return Text(snapshot.error.toString());
        }
        return Center(child: Text("Nothing"));
      },
    );
  }

  static DateTime epochToDt(int epoch) {
    return DateTime.fromMillisecondsSinceEpoch(epoch);
  }

  List<LineSeries<dynamic, dynamic>> getLineSeries(LSnap snap) {
    var start = DateTime.now().millisecondsSinceEpoch;
    var priceSer = LineSeries<OhlcTO, dynamic>(
      name: 'history',
      color: Colors.black,
      isVisibleInLegend: false,
      xValueMapper: (OhlcTO oh, _) => epochToDt(oh.timestamp.toInt()),
      yValueMapper: (OhlcTO oh, _) => oh.close,
      dataSource: snap.history,
    );

    var levelSer = <LineSeries<DatePoint, DateTime>>[];

    bool weekLegendSet = false;
    bool dayLegendSet = false;

    var visibleLegends = ["Day level", "Week level"];

    for (int i = 0; i < snap.levels.length; i++) {
      var l = snap.levels[i];

      Color color = null;

      var name = "Level ${i}";

      if (l.levelType == OhlcPeriod.Day) {
        color = Colors.green;
        if (!dayLegendSet) {
          name = visibleLegends[0];
          dayLegendSet = true;
        }
      } else {
        color = Colors.blue;
        if (!weekLegendSet) {
          name = visibleLegends[1];
          weekLegendSet = true;
        }
      }

      var ds = [
        l.level,
        l.level.copyWith((l) {
          l.timestamp = snap.history.last.timestamp;
          return l;
        })
      ];

      levelSer.add(LineSeries<DatePoint, DateTime>(
        name: name,
        isVisibleInLegend: visibleLegends.contains(name),
        color: color,
        xValueMapper: (oh, _) => epochToDt(oh.timestamp.toInt()),
        yValueMapper: (oh, _) => oh.value,
        dataSource: ds,
      ));
    }

    var curDs = [
      snap.currentPrice.copyWith((oh) {
        oh.timestamp = snap.history.first.timestamp;
        return oh;
      }),
      snap.currentPrice,
    ];

    var curPriceSer = LineSeries<OhlcTO, DateTime>(
      name: 'current price',
      isVisibleInLegend: true,
      dashArray: <double>[5, 5],
      color: Colors.redAccent,
      xValueMapper: (oh, _) => epochToDt(oh.timestamp.toInt()),
      yValueMapper: (oh, _) => oh.close,
      dataSource: curDs,
    );

    print("line series took ${(DateTime.now().millisecondsSinceEpoch - start)/1000.0}");

    return [priceSer, ...levelSer, curPriceSer];
  }
}
