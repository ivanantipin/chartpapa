import 'dart:async';
import 'dart:collection';
import 'dart:ffi';
import 'dart:io';

import 'package:fixnum/fixnum.dart' as prefix0;
import 'package:rxdart/rxdart.dart';
import 'package:simple_material_app/client.dart';
import 'package:simple_material_app/gen/alfa.pb.dart';

import 'package:simple_material_app/domain/domain.dart';

ApplicationBloc mainBloc = ApplicationBloc();

Subser subser = Subser();

class ApplicationBloc {
  StreamController<List<StratItem>> topController = BehaviorSubject();
  StreamController<Positions> positionsController = BehaviorSubject();
  StreamController<List<Position>> histPositionController = BehaviorSubject();

  StreamController<ModelStat> statController = BehaviorSubject();



  Queue<Signal> lastTrades = Queue<Signal>();
  Queue<Position> history = Queue();

  StratItem mapSignal(Signal it){
    return StratItem(it.ticker, epochDateToStr(it), it.buySell.name);
  }

  String epochDateToStr(Signal it) =>
      new DateTime.fromMillisecondsSinceEpoch(it.timestamp.toInt()).toIso8601String();

  void dispose() {
    topController.close();
    positionsController.close();
    histPositionController.close();
  }

  void addPositions(Positions pos) {
    positionsController.add(pos);
  }

  void addSignal(Signal signal){
    lastTrades.add(signal);

    if(lastTrades.length > 30){
      lastTrades.removeFirst();
    }

    topController.add(lastTrades.map((f)=>mapSignal(f)).toList().reversed.toList());
  }

  void addClosedPos(Position pos) {
    history.add(pos);

    if (history.length > 30) {
      history.removeFirst();
    }
    histPositionController.add(history.toList().reversed.toList());
  }
}

class Subser {
  var client = Client();

  Subser start() {
    print("starting");
    sub();
    posSub();
    closedPosSub();
    subStat();
    return this;
  }

  Future sub() async {
    Tickers tickers = await client.stub.getTickers(Empty.create());
    client.stub
        .subscribe(tickers)
        .listen((sig) {
          mainBloc.addSignal(sig);
        })
        .asFuture()
        .catchError((e) async {
          await Future.delayed(Duration(seconds: 5));
          print("error ${e}");
          sub();
        });
  }

  Future subStat() async {

    client.stub
        .getModelStat(Empty.create())
        .listen((sig) {
      mainBloc.statController.add(sig);
    })
        .asFuture()
        .catchError((e) async {
      await Future.delayed(Duration(seconds: 5));
      print("error ${e}");
      subStat();
    });
  }


  Future posSub() async {
    client.stub
        .positionSubscribe(Empty.create())
        .listen((pos) {
          print("received poses ${pos}");
          if (pos.poses.isNotEmpty) {
            mainBloc.addPositions(pos);
          }
        })
        .asFuture()
        .catchError((e) async {
          await Future.delayed(Duration(seconds: 5));
          print("error ${e}");
          posSub();
        });
  }

  Future closedPosSub() async {
    client.stub
        .posHistorySubscribe(Empty.create())
        .listen((pos) {
          print("received closed pose ${pos}");
          mainBloc.addClosedPos(pos);
        })
        .asFuture()
        .catchError((e) async {
          await Future.delayed(Duration(seconds: 5));
          print("error ${e}");
          closedPosSub();
        });
  }
}
