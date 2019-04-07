Sometimes when I do code review I look into the code of particular system and say that this is shit in 20 mins.
 
There are no particular arguments about OOP, just my story. After I realized that I developed my home project in scala. 

And was amuzed how much promlems has gone, though in my opinion scala is a bit overdesigned.


 And often my arguments sound like : I spent 20 mins looking into the code , I do not get it, system concept is damn simple, why code have 
 to be so sophisticated.
 
 system is damn simple like load data make a bit of trasform save
 
 then why on low level we see some tricky code.

 
 
why spring annotation is cursed
 
complexity:



complexity, similarity with life
non linearity, dimensinal course, oop


 
fun - case from life ?



hello
this post will be about programming in particular and in life in general.

tags: complexity, dimensional curse, programming

at the institute I found that we understand things much better by simple analogy.

For example at first glance functional analise looked like damn complex but if you understand
linear algebra you surprisingly will find a lot of anologies there. 
Linear algebra is very good for that purpose because it is simple for humanbean understanding 
because we operate in 3d reality - so our brain is optimized for different geometric things.
once you get familiar with linear algebra you can start generalise things and you get to functional analise.

Complexity - imagine you have a system and you add some functionality into the flow wich contains some state.
often when developer does not understand system it tries to make some non invasive change but complexity of 
ony flow will be complexity of system C(S) x C(new functionality) and that way you get exponential increase and 
system become unmaintainable. Because in not properly managed dev process people tend to put new functionality 
and increase dimensioning.

In most of my programming task i am trying to understand high level picture of system and try to get as less 
and realize how many dimensions of state I need. So once i understand i merecelesly get rid of any non 
necessary state and dimensions of complexity.

I often witnessed when one person worked more effectively than whole team out of 5-10 persons
They spend so much time for communication agreement synchronization that it killed performance of whole team and thing 
stagnated. This particular true for cache locality as one person has a context of very fast memory of task he is working 
 on. 



best known anty-pattern copy paste is good example of redundant dimension - when you copy-paste some 
  usually it is assumed that you have only one dimension but you create additional dimension by copying you
  code because it is not flexible enough to generalize.

Non linearity , ifs etc
as cpu works slow on if-s in cycle same people does - for example i have 5 databases non production
and my pattern calling them is:
name: uat uat_read/uat_read
name dev dev_read/dev_read

i know that pattern and always use it but one too smart ops guy said that it is not good not secure - though nobody going to
crack non prod envs and it creates something like :

preprod user0/SuperDuper1275Password

and every time I need to access this database i go to some passwords.txt file and retrieve it - this is damn slow
i calculated that i lost half an hour just for this simple operation and other team members as well ... People does not get 

so if you write somethig like cycle with if you will find out how much faster it would be without if( example here)

so magor takepoint from here : make things predictable and linear - this save much more than people tend to think about.






I found that people management is very similar to concurrent programming - if team have just a few contention 
things then they perform very well. To reduce contention you need to have immutable contract ( API, protocol etc)
then they do not need to sync on that contract as it is immutable.

