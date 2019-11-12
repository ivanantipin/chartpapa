import 'dart:async';
import 'dart:collection';

import 'package:rxdart/rxdart.dart';
import 'package:simple_material_app/gen/domain.pb.dart';
import 'package:simple_material_app/server/client.dart';

class PriceBloc {
  final Client client;
  final Set<String> tickers = new Set();
  final Map<String, SplayTreeMap<int, OhlcTO>> history = new Map();

  BehaviorSubject<List<String>> tickersStream = BehaviorSubject<List<String>>();
  Map<String, BehaviorSubject<List<OhlcTO>>> histPriceStreams = Map();
  Map<String, BehaviorSubject<OhlcTO>> latestPriceStreams = Map();

  BehaviorSubject<List<OhlcTO>> getHistPrice(String ticker) {
    return histPriceStreams.putIfAbsent(ticker, () => BehaviorSubject(seedValue: []));
  }

  BehaviorSubject<OhlcTO> getLatestPrice(String ticker) {
    return latestPriceStreams.putIfAbsent(ticker, () => BehaviorSubject());
  }


  PriceBloc(this.client);

  PriceBloc start(){
    priceSub();
    histSub();
    return this;
  }

  Future priceSub() async {
    client.stub
        .intradaySubscribe(Empty.create())
        .listen((ohlc) {
          latestPriceStreams.putIfAbsent(ohlc.ticker, ()=>BehaviorSubject()).add(ohlc);
        })
        .asFuture()
        .catchError((e) async {
          await Future.delayed(Duration(seconds: 5));
          print("error ${e}");
          priceSub();
        });
  }

  Future histSub() async {
    client.stub
        .priceSubscribe(HistoryRequest.create())
        .listen((ohlc) {
          var updateTickets = !history.containsKey(ohlc.ticker);
          var treeMap = history.putIfAbsent(ohlc.ticker, () => SplayTreeMap());
          treeMap[ohlc.timestamp.toInt()] = ohlc;

          if(updateTickets){
            var lst = history.keys.toList();
            lst.sort();
            tickersStream.add(lst);
          }


          if (treeMap.length > 600) {
            treeMap.remove(treeMap.firstKey());
          }
          getHistPrice(ohlc.ticker).add(treeMap.values.toList());
        })
        .asFuture()
        .catchError((e) async {
          await Future.delayed(Duration(seconds: 5));
          print("error ${e}");
          histSub();
        });
  }

  void dispose() {
    latestPriceStreams.values.forEach((f)=>f.close());
  }
}
