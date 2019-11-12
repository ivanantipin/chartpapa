import 'package:flutter/material.dart';
import 'package:syncfusion_flutter_charts/charts.dart';

SfCartesianChart getLegendCustomizedChart(bool isTileView) {
  return SfCartesianChart(
    plotAreaBorderWidth: 0,
    title:
    ChartTitle(text: isTileView ? '' : 'Automobile production by category'),
    legend: Legend(
      isVisible: true,
      overflowMode: LegendItemOverflowMode.wrap,
      legendItemBuilder:
          (String name, dynamic series, dynamic point, int index) {
        return Container(
            height: 30,
            width: 90,
            child: Row(children: <Widget>[
              Container(child: Text("${index}")),
              Container(child: Text(series.name)),
            ]));
      },
    ),
    primaryXAxis: NumericAxis(
        edgeLabelPlacement: EdgeLabelPlacement.shift,
        majorGridLines: MajorGridLines(width: 0)),
    primaryYAxis: NumericAxis(
        minimum: 0,
        maximum: 120,
        axisLine: AxisLine(width: 0),
        majorTickLines: MajorTickLines(color: Colors.transparent)),
    series: getLineSeries(isTileView),
    tooltipBehavior: TooltipBehavior(enable: true),
  );
}

List<ChartSeries<_ChartData, num>> getLineSeries(bool isTileView) {
  final List<_ChartData> chartData = <_ChartData>[
    _ChartData(2005, 38, 49, 56, 67),
    _ChartData(2006, 20, 40, 50, 60),
    _ChartData(2007, 60, 72, 84, 96),
    _ChartData(2008, 50, 65, 80, 90),
  ];
  return <ChartSeries<_ChartData, num>>[
    LineSeries<_ChartData, num>(
      width: 2,
      markerSettings: MarkerSettings(isVisible: true),
      dataSource: chartData,
      xValueMapper: (_ChartData sales, _) => sales.x,
      yValueMapper: (_ChartData sales, _) => sales.y,
      name: 'Truck',
    ),
    LineSeries<_ChartData, num>(
        markerSettings: MarkerSettings(isVisible: true),
        width: 2,
        dataSource: chartData,
        xValueMapper: (_ChartData sales, _) => sales.x,
        yValueMapper: (_ChartData sales, _) => sales.y2,
        name: 'Car'),
    LineSeries<_ChartData, num>(
        markerSettings: MarkerSettings(isVisible: true),
        width: 2,
        dataSource: chartData,
        xValueMapper: (_ChartData sales, _) => sales.x,
        yValueMapper: (_ChartData sales, _) => sales.y3,
        name: 'Bike'),
    LineSeries<_ChartData, num>(
        markerSettings: MarkerSettings(isVisible: true),
        width: 2,
        dataSource: chartData,
        xValueMapper: (_ChartData sales, _) => sales.x,
        yValueMapper: (_ChartData sales, _) => sales.y4,
        name: 'Bicycle')
  ];
}

class _ChartData {
  _ChartData(this.x, this.y, this.y2, this.y3, this.y4);
  final double x;
  final double y;
  final double y2;
  final double y3;
  final double y4;
}
