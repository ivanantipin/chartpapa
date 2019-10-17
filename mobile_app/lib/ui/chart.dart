import 'package:charts_flutter/flutter.dart' as charts;
import 'package:flutter/material.dart';
import 'package:simple_material_app/bloc/bloc.dart';
import 'package:simple_material_app/gen/alfa.pb.dart';




class StatsPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    var ret = Scaffold(
      appBar: AppBar(
        title: Text('Positions'),
      ),
      body: StreamBuilder(
        stream: mainBloc.statController.stream,
        builder: (context, AsyncSnapshot<ModelStat> snapshot) {
          if (snapshot.hasData) {
            return buildList(snapshot);
          } else if (snapshot.hasError) {
            return Text(snapshot.error.toString());
          }
          return Center(child: CircularProgressIndicator());
        },
      ),
    );
    return ret;
  }

  Widget buildList(AsyncSnapshot<ModelStat> poses) {
    return GridView.count(
      crossAxisCount: 1,
      // Generate 100 widgets that display their index in the List.
      children: poses.data.charts.map((chart) {
        return GridTile(
          child: GestureDetector(
              onTap: (){
//                Navigator.pushNamed(context, '/${name}');
              },
              child: Container(
                child: buildChart(chart),
              )
          ),
        );
      }).toList(),
    );
  }

  Widget buildChart(Chart chart){
    if(chart.chartType == Chart_ChartType.Bar){
      return new charts.TimeSeriesChart(
        _createSampleData(chart.points),
        animate: true,
        // Set the default renderer to a bar renderer.
        // This can also be one of the custom renderers of the time series chart.
        defaultRenderer: new charts.BarRendererConfig<DateTime>(),
        // It is recommended that default interactions be turned off if using bar
        // renderer, because the line point highlighter is the default for time
        // series chart.
        defaultInteractions: false,
        // If default interactions were removed, optionally add select nearest
        // and the domain highlighter that are typical for bar charts.
        behaviors: [new charts.SelectNearest(), new charts.DomainHighlighter()],
      );

    }
    if(chart.chartType == Chart_ChartType.Line){
      return new charts.TimeSeriesChart(
        _createSampleData(chart.points),
        animate: true,
        // Configures an axis spec that is configured to render one tick at each
        // end of the axis range, anchored "inside" the axis. The start tick label
        // will be left-aligned with its tick mark, and the end tick label will be
        // right-aligned with its tick mark.
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
        domainFn: (DatePoint sales, _) => DateTime.fromMillisecondsSinceEpoch(sales.timestamp.toInt()),
        measureFn: (DatePoint sales, _) => sales.value,
        data: seee,
      )
    ];
  }



}