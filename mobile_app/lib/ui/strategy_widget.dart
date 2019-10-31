import 'package:flutter/material.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:simple_material_app/ui/chart.dart';
import 'package:simple_material_app/ui/keyvalue_card.dart';

class StrategyWidget extends StatelessWidget{

  final Stream<StratDescription> stream;

  const StrategyWidget(this.stream);

  @override
  Widget build(BuildContext context) {
    return StreamBuilder(
      stream: stream,
      builder: (context, AsyncSnapshot<StratDescription> snapshot) {
        if (snapshot.hasData) {
          return ListView(children: <Widget>[
            PositionsList(snapshot.data.openPositions),
            StatsPage(snapshot.data.charts)
          ]);
        } else if (snapshot.hasError) {
          return Text(snapshot.error.toString());
        }
        return Center(child: Text("No positions"));
      },
    );


  }
}

class PositionsList extends StatelessWidget{

  final List<Position> positions;

  const PositionsList( this.positions) ;

  List<KeyValueStyled> mapPosition(Position position){
    var ret = List<KeyValueStyled>();
    ret.add(KeyValueStyled("", "${position.ticker}", TextStyle(fontSize: 15, fontWeight: FontWeight.bold),TextStyle(fontSize: 25, fontWeight: FontWeight.bold)));
    ret.add(KeyValueStyled("", "${formatPnl(position)} %", TextStyle(fontWeight: FontWeight.bold), TextStyle(fontSize: 25,  color: position.pnl > 0 ? Colors.green : Colors.red)));
    ret.add(KeyValueStyled("open ", position.openPrice.toStringAsFixed(2), TextStyle(fontWeight: FontWeight.bold),TextStyle()));
    return ret;
  }

  String formatPnl(Position pos){
    return ((pos.closePrice - pos.openPrice)/pos.openPrice*100.0).toStringAsFixed(1);
  }

  @override
  Widget build(BuildContext context) {
    return Column(children: positions.map((p){
      return KeyValuesCard(mapPosition(p));
    }).toList()
    );
  }
}


String fmtTs(int ts) {
  return new DateTime.fromMillisecondsSinceEpoch(ts).toIso8601String();
}




