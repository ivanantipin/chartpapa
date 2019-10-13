import 'package:flutter/material.dart';

import '../bloc/bloc.dart';
import '../domain/domain.dart';


class StdList extends StatelessWidget {



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
    return GridView.builder(
        itemCount: snapshot.data.length,
        gridDelegate:
        new SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 2),
        itemBuilder: (BuildContext context, int index) {
          return StratCard(snapshot.data[index]);
        });
  }

  StdList(){
    print("some");
  }
}


class StratCard extends StatelessWidget {
  StratItem strat;

  StratCard(this.strat);

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          ListTile(
            leading: Icon(Icons.album),
            title: Text("${strat.ticker}"),
            subtitle: Text("date : ${strat.date}"),
          ),
        ],
      ),
    );
  }
}


