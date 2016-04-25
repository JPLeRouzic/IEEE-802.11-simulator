This simulator integrates three amendments to the famous IEEE 802.11 standard: 
        * 802.11ac the current Wi-Fi, 
        * 802.11af a somewhat unknown protocol for using TV white spaces,
        * and the next Wi-Fi which will hit the market in 2018: 802.11ax. 

It uses the concepts and vocabulary of IEEE 802.11 study group. 

It is not a general network simulator as NS2, instead it concentrates on Wi-Fi. It is intented to be a quick tool, like for example Inssider.
There is no intention to simulate real physics, it uses heuristics to simulate physical phenomenas like noise, multipath or interferences, however this makes sense as physicals environments are not precisely simulated, in order to keep the user happy. Hence the compromise makes sense.

It derives from Jemula and Jemula802.11 which you can find at: https://github.com/schmist
However Jemula802.11 (not Jemula) has been heavily modified to make it much more modular, like for example having several PHY or several MAC protocols in the same simulation. 

If not enough has been done as I would like about MIMO, interferences, noise levels, coding schemes and modulations, there are much improvements here IMO that are unseen together in other simulators. 

Finally this is a work in progress, now what the simulation produces is only an XML file, more will come later.
I started this work four years ago, but there was a wide gap where I was busy doing other stuff mainly in open innovation in biology.

You can contact me at: https://padiracinnovation.org/feedback/

