
# Antenna specs
set chan       Channel/WirelessChannel
#set prop       Propagation/TwoRayGround
set prop       Propagation/Shadowing
set netif      Phy/WirelessPhy
set mac        Mac/802_11
set ifq        CMUPriQueue
set ll         LL
set ant        Antenna/OmniAntenna
# X dimension of the topography
set x              1000
#Y dimension of the topography
set y              670
# max packet in ifq
set ifqlen         50
# routing protocol
set adhocRouting   DSR

Mac/802_11 set dataRate_   [expr $C_down_fac * $C_up]            ;# bps  

Phy/WirelessPhy set RXThresh_ 1.92278e-06

Propagation/Shadowing set pathlossExp_ 2.0  ;# path loss exponent
Propagation/Shadowing set std_db_ 4.0       ;# shadowing deviation (dB)
Propagation/Shadowing set dist0_ 1.0        ;# reference distance (m)
Propagation/Shadowing set seed_ 0           ;# seed for RNG