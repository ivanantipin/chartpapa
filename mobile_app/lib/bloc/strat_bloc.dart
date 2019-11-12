import 'dart:async';

import 'package:rxdart/rxdart.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:simple_material_app/server/client.dart';

class StratBloc {
  BehaviorSubject<List<StratDescription>> stratController = BehaviorSubject();

  Map<String, BehaviorSubject<StratDescription>> strats = Map();

  final Client client;

  StratBloc(this.client) {}

  Future stratSub() async {
    client.stub
        .getStrats(Empty.create())
        .listen((pos) {
          addStrat(pos);
        })
        .asFuture()
        .catchError((e) async {
          await Future.delayed(Duration(seconds: 5));
          print("error ${e}");
          stratSub();
        });
  }

  void dispose() {
    stratController.close();
  }

  BehaviorSubject<StratDescription> getStratStream(String name) {
    return strats.putIfAbsent(name, ()=>BehaviorSubject()).stream;
  }

  void addStrat(StratDescription strat) {
    getStratStream(strat.name).add(strat);
    stratController.add(strats.values.map((f) => f.value).where((f) => f != null).toList());
  }
}
