#!/system/bin/sh

ip addr
echo "Please input 1808 network card name:"
read THE_1808_NET
echo "Please input local network card name:"
read  THE_LOCAL_NET

ifconfig $THE_1808_NET 192.168.180.1 netmask 255.255.255.0 up
ip rule add from all oif $THE_1808_NET table local_network
ip rule add from all iif $THE_1808_NET table $THE_LOCAL_NET
ip route add 192.168.180.0/24 dev $THE_1808_NET proto static table local_network
ip route flush cache

ndc tether interface add $THE_1808_NET
ndc tether dns set 8.8.4.4 8.8.8.8
ndc ipfwd enable tethering
ndc nat enable $THE_1808_NET $THE_LOCAL_NET 0

ip addr
