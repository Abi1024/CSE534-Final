R1 ip address add 1.1.2.1/24 dev R1-eth1

H1 ip route add 1.1.2.0/24 via 1.1.1.2 dev H1-eth0
H2 ip route add 1.1.1.0/24 via 1.1.2.1 dev H2-eth0

R1 iptables -t nat -A POSTROUTING -o R1-eth0 -j MASQUERADE
R1 iptables -A FORWARD -i R1-eth0 -o R1-eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT
R1 iptables -A FORWARD -i R1-eth0 -o R1-eth1 -j ACCEPT

H1 echo 1 > /proc/sys/net/ipv4/ip_forward
H2 echo 1 > /proc/sys/net/ipv4/ip_forward
R1 echo 1 > /proc/sys/net/ipv4/ip_forward

H1 python ftable.py H1
R1 python ftable.py R1
H2 python ftable.py H2

#Usage: H1 java Host <download_rate (packets per second)> {<destination> <upload_rate (packets per second)> <number of transmitted packets>}
#Usage R1 java Router <link_rate> {<measure queue size/packet loss interval (ms)> <max queue size>}
H1 javac *.java
H1 java Host 10 1.1.2.2 10 10000 &
R1 java Router 200 1000 10 &
H2 java Host 10