import 'package:flutter/material.dart';
import '../models/item_model.dart';
import '../bloc/bloc.dart';

class StdLst<T> extends StatelessWidget {

  final SimpleBloc<T> bloc;
  final Widget Function(T) ff;

  StdLst(this.bloc, this.ff);
  
  @override
  Widget build(BuildContext context) {
    this.bloc.fetch();
    return Scaffold(
      appBar: AppBar(
        title: Text('StratCards'),
      ),
      body: StreamBuilder(
        stream: bloc.allMovies,
        builder: (context, AsyncSnapshot<List<T>> snapshot) {
          if (snapshot.hasData) {
            return buildList(snapshot);
          } else if (snapshot.hasError) {
            return Text(snapshot.error.toString());
          }
          return Center(child: CircularProgressIndicator());
        },
      ),
    );
  }

  Widget buildList(AsyncSnapshot<List<T>> snapshot) {
    return GridView.builder(
        itemCount: snapshot.data.length,
        gridDelegate:
            new SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 2),
        itemBuilder: (BuildContext context, int index) {
          return  this.ff(snapshot.data[index]);
        });
  }
}

class SeqCard extends StatelessWidget {
  SequentaItem strat;

  SeqCard(this.strat);

  @override
  Widget build(BuildContext context) {
    print("fetching");
    stratBloc.fetch();
    return Card(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          ListTile(
            leading: Icon(Icons.album),
            title: Text("${strat.name}"),
            subtitle: Text("ticker : ${strat.ticker}"),
          ),
        ],
      ),
    );
  }
}

class StratCard extends StatelessWidget {
  StratItem strat;

  StratCard(this.strat);

  @override
  Widget build(BuildContext context) {
    stratBloc.fetch();
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

