import 'package:flutter/material.dart';

class MyTable<T> extends StatelessWidget {
  List<List<String>> data;
  List<String> headers;

  MyTable(this.data, this.headers);

  @override
  Widget build(BuildContext context) {
    return Column(children: [
      new ListTile(
        onTap: null,
        title: Row(children: <Widget>[
          ...headers.map((f) => Expanded(child: new Text(f)))
        ]),
      ),
      new Expanded(
        child: Container(
          child: ListView.builder(
            itemCount: data == null ? 0 : data.length,
            itemBuilder: (BuildContext context, int index) {
              return new InkWell(
                onTap: () {
                  Navigator.push(
                    context,
                    new MaterialPageRoute(),
                  );
                },
                child: new ListTile(
                    //return new ListTile(
                    onTap: null,
                    title: new Row(children: <Widget>[
                      ...data[index].map((f) => Expanded(child: new Text(f)))
                    ])),
              );
            }, //itemBuilder
          ),
        ),
      ),
    ]);
  }
}
