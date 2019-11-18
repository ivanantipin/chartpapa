import 'package:charts_flutter/flutter.dart' as charts;
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:syncfusion_flutter_charts/charts.dart';


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

  static Widget buildChartSyncfusion(Chart chart){

    return Container(
        color: Colors.white,
        height: 300,
        padding: EdgeInsets.only(right: 10),
        child: SfCartesianChart(
          plotAreaBorderWidth: 0,
          title: ChartTitle(text: chart.name),
          legend: Legend(
              isVisible: true,
              overflowMode: LegendItemOverflowMode.wrap
          ),
          primaryXAxis: DateTimeAxis(
            majorGridLines: MajorGridLines(width: 0),
            dateFormat: DateFormat.yMd(),
            title: AxisTitle(text: 'Time'),
          ),
          primaryYAxis: NumericAxis(
              rangePadding: ChartRangePadding.round,
              decimalPlaces: 4,
              visibleMinimum: 0.0,
              axisLine: AxisLine(width: 0),
              majorTickLines: MajorTickLines(color: Colors.transparent)),
          series: getLineSeries(chart.points),
          tooltipBehavior: TooltipBehavior(enable: true),
        ));
  }

  static List<LineSeries<DatePoint, DateTime>> getLineSeries(List<DatePoint> seee) {
    return [
      LineSeries<DatePoint, DateTime>(
        name: 'Sales',
        color: Colors.blue,
        xValueMapper: (DatePoint sales, _) =>
            DateTime.fromMillisecondsSinceEpoch(sales.timestamp.toInt()),
        yValueMapper: (DatePoint sales, _) => sales.value,
        dataSource: seee,
      )
    ];
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