
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:simple_material_app/bloc/bloc.dart';
import 'package:simple_material_app/ui/strat_page.dart';

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
  subser.start();
//  simpleRun(AsyncStratList(mainBloc.stratController.stream));
}
