import 'package:flutter/material.dart';
import 'package:simple_material_app/domain/domain.dart';
import 'package:simple_material_app/gen/alfa.pb.dart';
import 'package:simple_material_app/ui/main_grid.dart';
import 'package:simple_material_app/ui/my_table.dart';

import 'bloc/bloc.dart';

void main() {
  subser.start();
  runApp(MaterialApp(
      title: "StratApp",
      initialRoute: '/',
      routes: {
        // When navigating to the "/second" route, build the SecondScreen widget.
        '/strat': (context) => StratPage(),
        '/positions': (context) => PositionsPage(),
        '/hist': (context) => PositionHist(),
      },

      // Home
      home: Scaffold(
        // Appbar
        appBar: AppBar(
          // Title
          title: Text("Straaat app"),
        ),
        // Body
        body: MainGrid(),
      )));
}

class PositionHist extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    var ret = Scaffold(
      appBar: AppBar(
        title: Text('hist'),
      ),
      body: StreamBuilder(
        stream: mainBloc.histPositionController.stream,
        builder: (context, AsyncSnapshot<List<Position>> snapshot) {
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

  Widget buildList(AsyncSnapshot<List<Position>> snapshot) {
    var headers = ["ticker", "pnl", "time"];
    List<List<String>> data = snapshot.data.map((f) => [f.ticker, f.pnl.toStringAsFixed(0), fmtTs(f.timestamp.toInt())]).toList();
    return MyTable(data, headers);
  }
}


String fmtTs(int ts){
  return new DateTime.fromMillisecondsSinceEpoch(ts)
      .toIso8601String();
}


class StratPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    var ret = Scaffold(
      appBar: AppBar(
        title: Text('StratCards'),
      ),
      body: StreamBuilder(
        stream: mainBloc.topController.stream,
        builder: (context, AsyncSnapshot<List<StratItem>> snapshot) {
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

  Widget buildList(AsyncSnapshot<List<StratItem>> snapshot) {
    var headers = ["ticker", "buySell", "time"];
    List<List<String>> data = snapshot.data.map((f) => [f.ticker, f.buySell, f.date]).toList();
    return MyTable(data, headers);
  }
}


class PositionsPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    var ret = Scaffold(
      appBar: AppBar(
        title: Text('Positions'),
      ),
      body: StreamBuilder(
        stream: mainBloc.positionsController.stream,
        builder: (context, AsyncSnapshot<Positions> snapshot) {
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

  Widget buildList(AsyncSnapshot<Positions> poses) {
    var headers = ["ticker", "pnl", "position", "time"];
    List<List<String>> data = poses.data.poses.map((f) => [f.ticker, f.pnl.toStringAsFixed(0), "${f.position}", fmtTs(f.timestamp.toInt())]).toList();
    return MyTable(data, headers);
  }


}

//MyTable(Iterable<int>.generate(20).map((f) {
//return SequentaItem("tex", "sber", f);
//}).toList()),

class MainApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('First app')),
        body: ListView(
          children: <Widget>[
            ListTile(
              title: Text('Seq'),
              trailing: Icon(Icons.keyboard_arrow_right),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => Text("")),
                );
              },
            )
          ],
        ),
      ),
    );
  }
}
