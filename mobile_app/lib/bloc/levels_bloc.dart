import 'dart:async';

import 'package:rxdart/rxdart.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:simple_material_app/server/client.dart';

class LevelChart {
  final String ticker;
  final List<double> points;
  LevelChart(this.ticker, this.points);
}

class LevelsBloc {
  final Client client;

  Map<String, BehaviorSubject<Levels>> levelsStreams = Map();

  LevelsBloc(this.client);

  LevelsBloc start(){
    levelsSub();
    return this;
  }


  Future levelsSub() async {
    client.stub
        .getLevels(Empty.create())
        .listen((pos) {
          updateLevels(pos);
        })
        .asFuture()
        .catchError((e) async {
          await Future.delayed(Duration(seconds: 5));
          print("error ${e}");
          levelsSub();
        });
  }

  void dispose() {
    levelsStreams.values.forEach((f) => f.close());
  }

  BehaviorSubject<Levels> getLevelsForTickers(String ticker) {
    levelsStreams.putIfAbsent(ticker, () => BehaviorSubject());
    return levelsStreams[ticker].stream;
  }

  void updateLevels(Levels levels) {
    levelsStreams.putIfAbsent(levels.ticker, () => BehaviorSubject()).add(levels);
  }
}
