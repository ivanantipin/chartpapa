import 'package:flutter/material.dart';
import 'package:flutter/material.dart' as prefix0;
import 'package:simple_material_app/bloc/levels_bloc.dart';
import 'package:simple_material_app/bloc/price_bloc.dart';
import 'package:simple_material_app/ui/levels/levels_chart.dart';

class AsyncLevelsList extends StatelessWidget {
  final PriceBloc priceBloc;
  final LevelsBloc levelsBloc;

  const AsyncLevelsList(
    this.priceBloc,
    this.levelsBloc,
  );

  @override
  Widget build(BuildContext context) {
    return StreamBuilder(
      stream: priceBloc.tickersStream.stream,
      builder: (context, AsyncSnapshot<List<String>> snapshot) {
        if (snapshot.hasData) {
          return buildList(snapshot, context);
        } else if (snapshot.hasError) {
          return Text(snapshot.error.toString());
        }
        return Center(child: Text("No positions"));
      },
    );
  }

  Widget buildList(AsyncSnapshot<List<String>> poses, BuildContext context) {
    var tickers = poses.data;
    return new ListView(
        shrinkWrap: true,
        padding: const EdgeInsets.all(20.0),
        itemExtent: 100,
        children: List.generate(tickers.length, (index) {
          return prefix0.ListTile(
            leading: Icon(Icons.multiline_chart),
            title: new Center(
                child: new Text(
              tickers[index],
              style: new TextStyle(fontWeight: FontWeight.w500, fontSize: 25.0),
            )),
            onTap: () {
              Navigator.push(
                  context,
                  MaterialPageRoute(
                      builder: (BuildContext context) =>
                          LevelsChartWidget(tickers[index], levelsBloc, priceBloc)));
            },
          );
        }));
  }
}
