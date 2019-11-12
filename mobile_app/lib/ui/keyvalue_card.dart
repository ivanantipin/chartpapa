import 'package:flutter/material.dart';
import 'package:quiver/iterables.dart';



class KeyValueStyled extends StatelessWidget{
  final String name;
  final String value;
  final TextStyle nameStyle;
  final TextStyle valueStyle;

  KeyValueStyled(this.name, this.value, this.nameStyle, this.valueStyle);

  @override
  Widget build(BuildContext context) {
    var l0 = <Widget>[
      Text(name, style: nameStyle, textAlign: TextAlign.center),
      Text(":", style: TextStyle(fontWeight: FontWeight.bold), textAlign: TextAlign.center),
    ];
    var l1 = <Widget>[Text(value, style: valueStyle, textAlign: TextAlign.center,)];
    return Container(

      margin: EdgeInsets.all(15.0),

      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: name.isEmpty ? l1 : concat([l0,l1]).toList(),
      ),
    );
  }
}


class KeyValuesCard extends StatelessWidget {

  final List<List<Widget>> cells;

  const KeyValuesCard(this.cells);

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(15.0),
      ),
      color: Colors.white,
      elevation: 20,
      child: Column(
        children: cells.map((lst){
          return Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: lst,
          );
        }).toList(),
      ),
    );
  }
}

String fmtTs(int ts) {
  return new DateTime.fromMillisecondsSinceEpoch(ts).toIso8601String();
}