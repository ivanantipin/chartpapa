import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:simple_material_app/ui/strat_page.dart';

import 'bloc/bloc.dart';

void main() {
  subser.start();
  runApp(MaterialApp(
      title: "FireApp",
      initialRoute: '/',
      routes: {
        // When navigating to the "/second" route, build the SecondScreen widget.
        '/positions': (context) => AsyncStratList(),
      },

      // Home
      home: BottomBar()
  )
  );
}



class BottomBar extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return BottomBarState();
  }
}

class BottomBarState extends State {
  int _selectedIndex = 0;
  static const TextStyle optionStyle = TextStyle(fontSize: 30, fontWeight: FontWeight.bold);
  static List<Widget> _widgetOptions = <Widget>[AsyncStratList(), Text("nothing")];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Main app'),
      ),
      body: Center(
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.euro_symbol),
            title: Text('Strategies'),
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.functions),
            title: Text('Outliers'),
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.amber[800],
        onTap: _onItemTapped,
      ),
    );
  }
}

class StratsList extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return ListView(
      // Generate 100 widgets that display their index in the List.
      children: ["volabreak", "divgap"].map((name) {
        return GridTile(
          child: GestureDetector(
              onTap: () {
                Navigator.pushNamed(context, '/${name}');
              },
              child: Container(
                decoration: BoxDecoration(
                    color: Colors.blueAccent,
                    borderRadius: BorderRadius.all(Radius.circular(16.0))),
                alignment: Alignment.center,
                margin: EdgeInsets.all(15.0),
                child: Text(name, style: Theme.of(context).textTheme.headline),
              )),
        );
      }).toList(),
    );
  }
}
