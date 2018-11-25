package firelib.parser;


@FunctionalInterface
interface IModifier<T,V>{
    void apply(T md, V val);
}
