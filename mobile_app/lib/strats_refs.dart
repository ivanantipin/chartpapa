import 'package:flutter/material.dart';
import 'package:simple_material_app/bloc/bloc.dart';
import 'package:simple_material_app/ui/sequentas_list.dart';


class MainApp extends StatefulWidget {

  @override
  State<StatefulWidget> createState() {
    return MainAppState();
  }
}

class MainAppState extends State<StatefulWidget> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('First app')),
        body: ListView(
          children: <Widget>[
            ListTile(
              title: Text('Seq'),
              trailing: Icon(Icons.keyboard_arrow_right),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => StdLst(seqBloc,(seqItem)=>SeqCard(seqItem))),
                );
              },
            ),
            ListTile(
              title: Text('Strat'),
              trailing: Icon(Icons.keyboard_arrow_right),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => StdLst(stratBloc,(seqItem)=>StratCard(seqItem))),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}


