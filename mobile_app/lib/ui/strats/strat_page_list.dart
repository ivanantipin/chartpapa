import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:simple_material_app/bloc/strat_bloc.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:simple_material_app/ui/chart.dart';
import 'package:simple_material_app/ui/strats/strategy_widget.dart';

class AsyncStratList extends StatelessWidget {

  final StratBloc stratBloc;

  const AsyncStratList(this.stratBloc);


  @override
  Widget build(BuildContext context) {
    return StreamBuilder(
      stream: stratBloc.stratController.stream,
      builder: (context, AsyncSnapshot<List<StratDescription>> snapshot) {
        if (snapshot.hasData) {
          return buildList(snapshot, context);
        } else if (snapshot.hasError) {
          return Text(snapshot.error.toString());
        }
        return Center(child: Text("No positions"));
      },
    );
  }

  Widget buildList(AsyncSnapshot<List<StratDescription>> poses, BuildContext context) {
    return new ListView(
        shrinkWrap: true,
        padding: const EdgeInsets.all(20.0),
        children: List.generate(poses.data.length, (index) {
          return GestureDetector(
              onTap: () {
                Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (BuildContext context) =>
                            StrategyWidget(stratBloc.getStratStream(poses.data[index].name))));
              },
              child: Center(
                child: ChoiceCard(poses.data[index]),
              ));
        }));
  }
}

class ChoiceCard extends StatelessWidget {
  final StratDescription description;

  const ChoiceCard(this.description);

  @override
  Widget build(BuildContext context) {
    return Card(
        color: Colors.white,
        child: Column(
          children: <Widget>[
            new Container(
                padding: const EdgeInsets.all(8.0),
                height: 200,
                child: StatsPage.buildChartSyncfusion(description.benchmark)),
            new Container(
              height: 300,
              padding: const EdgeInsets.all(10.0),
                child: Markdown(
                  data: description.description,
                  imageDirectory: 'https://raw.githubusercontent.com',
                ),
            )
          ],
          crossAxisAlignment: CrossAxisAlignment.start,
        ));
  }
}
