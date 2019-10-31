import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:simple_material_app/bloc/bloc.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:simple_material_app/ui/chart.dart';
import 'package:simple_material_app/ui/strategy_widget.dart';

class AsyncStratList extends StatelessWidget {



  @override
  Widget build(BuildContext context) {
    return StreamBuilder(
      stream: mainBloc.stratController.stream,
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

  Widget buildList(AsyncSnapshot<List<StratDescription>> poses, BuildContext context ) {
    return new ListView(
        shrinkWrap: true,
        padding: const EdgeInsets.all(20.0),
        children: List.generate(poses.data.length, (index) {
          return GestureDetector(
            onTap: (){
              Navigator.push(context, MaterialPageRoute(builder: (BuildContext context) => StrategyWidget(mainBloc.getStratStream(poses.data[index].name))));
            },
            child:  Center(
              child: ChoiceCard(poses.data[index]),
          )
          );

        }));
  }
}

class ChoiceCard extends StatelessWidget {

  final StratDescription description;

  const ChoiceCard(this.description);

  @override
  Widget build(BuildContext context) {
    TextStyle textStyle = Theme.of(context).textTheme.display1;
//    if (selected) textStyle = textStyle.copyWith(color: Colors.lightGreenAccent[400]);

    return Card(
        color: Colors.white,
        child: Column(
          children: <Widget>[
            new Container(padding: const EdgeInsets.all(8.0), height: 300, child: StatsPage.buildChart(description.benchmark)),
            new Container(
              padding: const EdgeInsets.all(10.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(description.name, style: Theme.of(context).textTheme.title),
//                  Text(choice.date, style: TextStyle(color: Colors.black.withOpacity(0.5))),
                  Text(description.description),
                ],
              ),
            )
          ],
          crossAxisAlignment: CrossAxisAlignment.start,
        ));
  }
}
