---
layout: post
title:  "Dependency injection framework? - no thanks!"
date:   2018-04-20 10:18:00
categories: dependency injection
---

Dependency injection is an old popular technique to glue your application components together.
Sometime struggling with DI frameworks ( spring mainly) I asked myself if it is possible to make this easier and 
eventually I found solution that is good for me.

Lets review few popular frameworks in java world try to understand what is wrong with them and think if we can do better without.

### Spring Xml DI

This one is a pioneer among DI frameworks and probably most hated framework because of its verbosity inherited from xml.

-   Pros
    -   no magic, injection based on object instances rather than classes which eliminates [robot leg problems](https://github.com/google/guice/wiki/FrequentlyAskedQuestions)
    -   does not pollute classes with annotations like
        @Inject,@Name etc.
-   Cons
    - verbosity, xml is unnecessary heavy for this task
    - non homogeneous, i.e. injection logic is in XML, application is in java. Any non trivial configuration become bulky and cumbersome in XML (a.k.a. XML programming)
    - you need IDE to refactor and manipulate xml contexts and, still, IDE easy get confused by quite trivial cases.
    - xml contexts tend to grow in size as they hard to split and combine.

### Spring Annotation based DI

-   Pros
    -   homogeneity i.e. DI logic expressed in java
-   Cons
    -   a lot of magic happens during runtime which cause non trivial puzzles for trivial applications. Weak control over application initialization order.
    -   DI based on classes rather than instances so you have [robot leg problems](https://github.com/google/guice/wiki/FrequentlyAskedQuestions), you need to use qualifiers to distinct instances.
    -   pollute classes with annotations related to DI

### Google Guice
-   Pros
    -   homogeneity i.e. DI logic expressed in java.
    -   keeps DI logic in one place - guice module which is quite convenient 
    -   does not have magic problems with classpath resolution comparing to spring.
-   Cons
    -   DI based on classes rather than instances so you have [robot leg problems](https://github.com/google/guice/wiki/FrequentlyAskedQuestions), you need to use qualifiers to distinct instances.
    -   pollute classes with annotations related to DI

### Alternative approach

There are few problems we need to address to beat above frameworks:

1. Having gluing logic in plain java code not in any xml/json/whatever ( homogeneity  )
2. No runtime magic, proxy generation etc.
3. No annotations

Lets demonstrate approach on simple artificial example:

Imagine that we have class Server that does something with string:

{% highlight java %}
public interface Server{
    String process(String str);
}
{% endhighlight %}

Implementation delegates processing to service provided in constructor as a function :

{% highlight java %}
public class ServerImpl implements Server{
    public ServerImpl(Function<String,String> processor){
        this.processor = processor;
    }
    public String process(String inStr) {
        return processor.apply(inStr);
    }
}
{% endhighlight %}


lets inject this service using standard java :

{% highlight java %}

public class DiModule {
    public Function<String, String> getProcessor() {
        return (s) -> "processed(" + s + ")";
    }

    public Server getServer(){
        return new ServerImpl(getProcessor());
    }
}
{% endhighlight %}

and for sure we need to be able to mock injected service for testing purposes, and this is simply done by 
overriding *getProcessor()* method in module:

{% highlight java %}

public void testDiMethod(){
    DiModule testModule = new DiModule() {
        @Override
        public Function<String, String> getProcessor() {
            return (s)->"processed_by_mock(" + s + ")";
        }
    };
    Assert.assertEquals("processed_by_mock(in_str)",
        mockedModule.getServer().process("in_str"));
}
{% endhighlight %}


As you may notice we are missing **singletons** functionality here , that could be useful when dealing with heavy stateful services.<br/>You can use *ConcurrentHashMap.computeIfAbsent* with factory method: 

{% highlight java %}
public class DiModule implements AutoCloseable{

    private final ConcurrentHashMap singletones = new ConcurrentHashMap();

    public Function<String, String> getProcessor() {
        return (s) -> s + "processed";
    }

    public Server getServer(){
        return singletons.computeIfAbsent("server", ()->{
            return new ServerImpl(getProcessor());
        });
    }

    //this is trivial implementation of cleaning up singleton resources
    public void close(){
        singletones.forEach((name, service)->{
            if(service instanceof AutoCloseable){
                ((AutoCloseable)service).close();
            }
        });
    }
}
{% endhighlight %}

I use this approach at work for more than 2 years and it works quite well for me, though my collegues keep trying to convince me that I just do not know how to properly "cook" spring.  
<br/> Hopefully this post will be useful for lost souls in spring mess.