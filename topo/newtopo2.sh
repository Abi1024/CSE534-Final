R1 ip address add 1.1.2.1/24 dev R1-eth1
R1 ip address add 1.1.3.2/24 dev R1-eth2
R2 ip address add 1.1.4.1/24 dev R2-eth1

H1 ip route add 1.1.2.0/24 via 1.1.1.2 dev H1-eth0
H1 ip route add 1.1.3.0/24 via 1.1.1.2 dev H1-eth0
H1 ip route add 1.1.4.0/24 via 1.1.1.2 dev H1-eth0
R1 ip route add 1.1.4.0/24 via 1.1.2.2 dev R1-eth1
R2 ip route add 1.1.1.0/24 via 1.1.2.1 dev R2-eth0
R2 ip route add 1.1.3.0/24 via 1.1.2.1 dev R2-eth0
H2 ip route add 1.1.1.0/24 via 1.1.3.2 dev H2-eth0
H2 ip route add 1.1.2.0/24 via 1.1.3.2 dev H2-eth0
H2 ip route add 1.1.4.0/24 via 1.1.3.2 dev H2-eth0
H3 ip route add 1.1.1.0/24 via 1.1.4.1 dev H3-eth0
H3 ip route add 1.1.2.0/24 via 1.1.4.1 dev H3-eth0
H3 ip route add 1.1.3.0/24 via 1.1.4.1 dev H3-eth0

R1 iptables -t nat -A POSTROUTING -o R1-eth0 -j MASQUERADE
R1 iptables -t nat -A POSTROUTING -o R1-eth1 -j MASQUERADE
R1 iptables -t nat -A POSTROUTING -o R1-eth2 -j MASQUERADE
R2 iptables -t nat -A POSTROUTING -o R2-eth0 -j MASQUERADE
R2 iptables -t nat -A POSTROUTING -o R2-eth1 -j MASQUERADE

H1 echo 1 > /proc/sys/net/ipv4/ip_forward
R2 echo 1 > /proc/sys/net/ipv4/ip_forward
R1 echo 1 > /proc/sys/net/ipv4/ip_forward
H2 echo 1 > /proc/sys/net/ipv4/ip_forward
H3 echo 1 > /proc/sys/net/ipv4/ip_forward

H1 python ftable.py H1
R1 python ftable.py R1
R2 python ftable.py H2
H2 python ftable.py H3
H3 python ftable.py H4
