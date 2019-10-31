import 'dart:async';
import 'dart:collection';

import 'package:rxdart/rxdart.dart';
import 'package:simple_material_app/gen/alfa.pb.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:simple_material_app/server/client.dart';

ApplicationBloc mainBloc = ApplicationBloc();

Subser subser = Subser();

class ApplicationBloc {
  BehaviorSubject<List<StratDescription>> stratController = BehaviorSubject();

  Map<String,BehaviorSubject<StratDescription>> strats = Map();

  void dispose() {
    stratController.close();
  }
  
  Stream<StratDescription> getStratStream(String name){
    ensureExists(name);
    return strats[name].stream;
  }

  void addStrat(StratDescription strat) {

    ensureExists(strat.name);

    strats[strat.name].add(strat);

    stratController.add(strats.values.map((f)=>f.value).where((f)=>f!=null).toList());

    print(strats.values.map((f)=>f.value));

  }

  void ensureExists(String name) {
    strats.putIfAbsent(name, ()=>BehaviorSubject());
  }
}

class Subser {
  var client = Client();

  Subser start() {
    print("starting");
    stratSub();
    return this;
  }

  Future stratSub() async {
    client.stub
        .getStrats(Empty.create())
        .listen((pos) {
      mainBloc.addStrat(pos);
    })
        .asFuture()
        .catchError((e) async {
      await Future.delayed(Duration(seconds: 5));
      print("error ${e}");
      stratSub();
    });
  }
}
