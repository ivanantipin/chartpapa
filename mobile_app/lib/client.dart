// Copyright (c) 2017, the gRPC project authors. Please see the AUTHORS file
// for details. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import 'dart:async';
import 'dart:core';
import 'dart:math' show Random;

import 'package:grpc/grpc.dart';
import 'package:simple_material_app/gen/services.pbgrpc.dart';

import 'gen/services.pb.dart';
import 'gen/services.pbgrpc.dart';

class Client {
  ClientChannel channel;
  StratServiceClient stub;

  Client(){
    channel = ClientChannel('127.0.0.1',
        port: 50051,
        options:
        const ChannelOptions(credentials: ChannelCredentials.insecure()));
    stub = StratServiceClient(channel,
        options: CallOptions(timeout: Duration(seconds: 30)));
//    await channel.shutdown();

  }


  void doSome(){
    var tickers = stub.getTickers(Empty.create());
  }
}

main()  async {

  var client = Client();

  var tickers = await client.stub.getTickers(Empty.create());

  print(tickers);

  client.stub. subscribe(tickers).forEach((a){
    print(a);
  });

//  await for (var feature in client.stub.getTickers(Empty.create()).asStream()) {
//    print(feature);
//  }


}
