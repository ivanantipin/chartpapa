import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:simple_material_app/bloc/levels_bloc.dart';
import 'package:simple_material_app/bloc/price_bloc.dart';
import 'package:simple_material_app/server/client.dart';
import 'package:simple_material_app/ui/levels/async_levels_list.dart';
import 'package:simple_material_app/ui/levels/chart_test.dart';
import 'package:syncfusion_flutter_core/core.dart';

void simpleRun(Widget widget) {
  runApp(MaterialApp(
      title: "FireApp",
      initialRoute: '/',
// Home
      home: Scaffold(
// Appbar
        appBar: AppBar(
// Title
          title: Text("Fire App"),
        ),
// Body
        body: widget,
      )));
}

void main(){

  Client client = new Client();


  SyncfusionLicense.registerLicense("NT8mJyc2IWhiZH1gfWN9YmdoYmF8YGJ8ampqanNiYmlmamlmanMDHmg6JTI9fTI9JzojOj0TND4yOj99MDw+");

  //simpleRun(getLegendCustomizedChart(false));
  simpleRun(AsyncLevelsList(new PriceBloc(client).start(),LevelsBloc(client).start()));
}

