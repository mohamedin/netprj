
# Antenna specs
set chan       Channel/WirelessChannel
set prop       Propagation/TwoRayGround
set netif      Phy/WirelessPhy
set mac        Mac/802_11
set ifq        CMUPriQueue
set ll         LL
set ant        Antenna/OmniAntenna
# X dimension of the topography
set x              670
#Y dimension of the topography
set y              670
# max packet in ifq
set ifqlen         50
# routing protocol
set adhocRouting   DSR

Mac/802_11 set dataRate_   [expr $C_down_fac * $C_up]            ;# bps  
