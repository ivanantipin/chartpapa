import 'package:rxdart/rxdart.dart';
import 'package:simple_material_app/models/item_model.dart';
import 'package:simple_material_app/resources/movie_api.dart';

class SimpleBloc<T> {

  final Future<List<T>> Function() provider;

  final _moviesFetcher = PublishSubject<List<T>>();

  SimpleBloc(this.provider);

  Observable<List<T>> get allMovies => _moviesFetcher.stream;

  fetch() async {
    List<T> itemModel = await provider();
    _moviesFetcher.sink.add(itemModel);
  }

  dispose() {
    _moviesFetcher.close();
  }
}

SimpleBloc<SequentaItem> seqBloc = SimpleBloc(()=>SequentaApiProvider.fetchSignals());
SimpleBloc<StratItem> stratBloc = SimpleBloc(()=>StratApiProvider.fetchStrats());




