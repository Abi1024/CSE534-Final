R1 ip address add 1.1.2.2/24 dev R1-eth1
R1 ip address add 1.1.3.2/24 dev R1-eth2
R1 ip address add 1.1.4.2/24 dev R1-eth3

H1 ip route add 1.1.2.0/24 via 1.1.1.2 dev H1-eth0
H1 ip route add 1.1.3.0/24 via 1.1.1.2 dev H1-eth0
H1 ip route add 1.1.4.0/24 via 1.1.1.2 dev H1-eth0
H2 ip route add 1.1.1.0/24 via 1.1.2.2 dev H2-eth0
H2 ip route add 1.1.3.0/24 via 1.1.2.2 dev H2-eth0
H2 ip route add 1.1.4.0/24 via 1.1.2.2 dev H2-eth0
H3 ip route add 1.1.1.0/24 via 1.1.3.2 dev H3-eth0
H3 ip route add 1.1.2.0/24 via 1.1.3.2 dev H3-eth0
H3 ip route add 1.1.4.0/24 via 1.1.3.2 dev H3-eth0
H4 ip route add 1.1.1.0/24 via 1.1.4.2 dev H4-eth0
H4 ip route add 1.2.1.0/24 via 1.1.4.2 dev H4-eth0
H4 ip route add 1.3.1.0/24 via 1.1.4.2 dev H4-eth0

R1 iptables -t nat -A POSTROUTING -o R1-eth0 -j MASQUERADE
R1 iptables -A FORWARD -i R1-eth0 -o R1-eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT
R1 iptables -A FORWARD -i R1-eth0 -o R1-eth1 -j ACCEPT

H1 echo 1 > /proc/sys/net/ipv4/ip_forward
H2 echo 1 > /proc/sys/net/ipv4/ip_forward
R1 echo 1 > /proc/sys/net/ipv4/ip_forward
H3 echo 1 > /proc/sys/net/ipv4/ip_forward
H4 echo 1 > /proc/sys/net/ipv4/ip_forward

H1 python ftable.py H1
R1 python ftable.py R1
H2 python ftable.py H2
H3 python ftable.py H3
H4 python ftable.py H4
