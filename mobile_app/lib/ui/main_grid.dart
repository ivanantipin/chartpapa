import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class MainGrid extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return GridView.count(
      // Create a grid with 2 columns. If you change the scrollDirection to
      // horizontal, this produces 2 rows.
      crossAxisCount: 2,
      // Generate 100 widgets that display their index in the List.
      children: ["strat","positions","hist"].map((name) {
        return GridTile(
          child: GestureDetector(
            onTap: (){
              Navigator.pushNamed(context, '/${name}');
            },
            child: Container(
              decoration: BoxDecoration(
                  color: Colors.blueAccent,
                  borderRadius: BorderRadius.all(Radius.circular(16.0))
              ),
              alignment: Alignment.center,
              margin: EdgeInsets.all(15.0),
              child: Text(name, style: Theme.of(context).textTheme.headline),
            )
          ),
        );
      }).toList(),
    );
  }
}
