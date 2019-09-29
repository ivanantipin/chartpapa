import 'dart:async';

import '../models/item_model.dart';


class SequentaApiProvider {
  static Future<List<SequentaItem>> fetchSignals() async {
    List<SequentaItem> ret = List(10);

    for(int i = 0; i < 10; i++){
      ret[i] = SequentaItem("test","sber", 13);
    }

    return Future.sync(()=>ret);
  }
}

class StratApiProvider {
  static Future<List<StratItem>> fetchStrats() async {
    
    print("strat fetch");

    List<StratItem> ret = List(10);

    for(int i = 0; i < 10; i++){
      ret[i] = StratItem("sber", "2015-02-20");
    }

    return Future.sync(()=>ret);
  }
}

