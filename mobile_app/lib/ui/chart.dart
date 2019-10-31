import 'package:charts_flutter/flutter.dart' as charts;
import 'package:flutter/material.dart';
import 'package:simple_material_app/gen/domain.pb.dart';


class StatsPage extends StatelessWidget {

  final List<Chart> chartsList;

  const StatsPage(this.chartsList);

  @override
  Widget build(BuildContext context) {
    return buildList(chartsList);
  }

  Widget buildList(List<Chart> charts) {
    return Container(
      height: (charts.length*300).toDouble(),
      child: Column(
        children: charts.map((chart) {
          return Expanded(
              child: buildChart(chart)
          );
        }).toList(),
      ),
    );
  }

  static Widget buildChart(Chart chart) {
    if (chart.chartType == Chart_ChartType.Bar) {
      return new charts.TimeSeriesChart(
        _createSampleData(chart.points),
        animate: true,
        defaultRenderer: new charts.BarRendererConfig<DateTime>(),
        defaultInteractions: false,
        behaviors: [new charts.SelectNearest(), new charts.DomainHighlighter()],
      );
    }
    if (chart.chartType == Chart_ChartType.Line) {
      return charts.TimeSeriesChart(
        _createSampleData(chart.points),
        animate: true,
        defaultInteractions: false,
        domainAxis: new charts.EndPointsTimeAxisSpec(),
      );
    }
  }


  /// Create one series with sample hard coded data.
  static List<charts.Series<DatePoint, DateTime>> _createSampleData(List<DatePoint> seee) {
    return [
      new charts.Series<DatePoint, DateTime>(
        id: 'Sales',
        colorFn: (_, __) => charts.MaterialPalette.blue.shadeDefault,
        domainFn: (DatePoint sales, _) =>
            DateTime.fromMillisecondsSinceEpoch(sales.timestamp.toInt()),
        measureFn: (DatePoint sales, _) => sales.value,
        data: seee,
      )
    ];
  }

}