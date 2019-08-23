#部署步骤

##RK1808人工智能计算棒部署

1、RK1808人工智能计算棒网络配置

需提前在win、mac、linux等平台完成RK1808人工智能计算棒网络配置(RNDIS)、模型拷贝以及设置开机启动。



2、Android网络配置
Android进入shell后需要输入"su"命令切换到root用户，然后查看当前网卡的状态

```shell
E:\master_yolov3_demo>adb shell
rk3399:/ $ su
rk3399:/ #
rk3399:/ # ip addr
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 76:bb:8e:01:27:e9 brd ff:ff:ff:ff:ff:ff
    inet 172.16.9.113/24 brd 172.16.9.255 scope global eth0
       valid_lft forever preferred_lft forever
    inet6 fe80::1f1e:8e29:cfea:4f25/64 scope link flags 800
       valid_lft forever preferred_lft forever
3: sit0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN group default qlen 1
    link/sit 0.0.0.0 brd 0.0.0.0
4: wlan0: <NO-CARRIER,BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state DORMANT group default qlen 1000
    link/ether 04:e6:76:bc:83:1f brd ff:ff:ff:ff:ff:ff
5: usb0: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN group default qlen 1000
    link/ether 36:66:1a:ea:ab:59 brd ff:ff:ff:ff:ff:ff
rk3399:/ #
rk3399:/ # ip route
172.16.9.0/24 dev eth0  proto kernel  scope link  src 172.16.9.113
rk3399:/ #  ping 192.168.180.8
PING 192.168.180.8 (192.168.180.8) 56(84) bytes of data.
^C
--- 192.168.180.8 ping statistics ---
13 packets transmitted, 0 received, 100% packet loss, time 12012ms
```

可以看到计算棒的网卡为usb0目前未有任何配置，需要为usb0配置ip、子网掩码、路由表等

这里我们使用开发板于外网连接的以太网卡eth0为例，如使用其他网卡上网只需

```shell
rk3399:/ # ifconfig usb0 192.168.180.1 netmask 255.255.255.0 up
le add from all oif usb0 table local_network
ip rule add from all iif usb0 table eth0
ip route add 192.168.180.0/24 dev usb0 proto static table local_network
ip route flush cache

ndc tether interface add usb0
ndc tether dns set 8.8.4.4 8.8.8.rk3399:/ # ip rule add from all oif usb0 table local_network
8
ndc ipfwd enable tethering
ndc nat enable usb0 eth0 0
rk3399:/ # ip rule add from all iif usb0 table eth0
rk3399:/ # ip route add 192.168.180.0/24 dev usb0 proto static table local_network
rk3399:/ # ip route flush cache
rk3399:/ #
rk3399:/ # ndc tether interface add usb0
616 Route removed fe80::/64 dev usb0
614 Address removed fe80::3466:1aff:feea:ab59/64 usb0 196 253
616 Route updated fe80::/64 dev usb0
200 0 Tether operation succeeded
rk3399:/ # ndc tether dns set 8.8.4.4 8.8.8.8
200 0 Tether operation succeeded
rk3399:/ # ndc ipfwd enable tethering
200 0 ipfwd operation succeeded
rk3399:/ # ndc nat enable usb0 eth0 0
200 0 Nat operation succeeded
rk3399:/ #


```

再次查看

```shell
rk3399:/ # ip addr
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 76:bb:8e:01:27:e9 brd ff:ff:ff:ff:ff:ff
    inet 172.16.9.113/24 brd 172.16.9.255 scope global eth0
       valid_lft forever preferred_lft forever
    inet6 fe80::1f1e:8e29:cfea:4f25/64 scope link flags 800
       valid_lft forever preferred_lft forever
3: sit0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN group default qlen 1
    link/sit 0.0.0.0 brd 0.0.0.0
4: wlan0: <NO-CARRIER,BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state DORMANT group default qlen 1000
    link/ether 04:e6:76:bc:83:1f brd ff:ff:ff:ff:ff:ff
5: usb0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UNKNOWN group default qlen 1000
    link/ether 36:66:1a:ea:ab:59 brd ff:ff:ff:ff:ff:ff
    inet 192.168.180.1/24 brd 192.168.180.255 scope global usb0
       valid_lft forever preferred_lft forever
    inet6 fe80::3466:1aff:feea:ab59/64 scope link
       valid_lft forever preferred_lft forever
rk3399:/ #
rk3399:/ # ip route
172.16.9.0/24 dev eth0  proto kernel  scope link  src 172.16.9.113
192.168.180.0/24 dev usb0  proto kernel  scope link  src 192.168.180.1
rk3399:/ # ping 192.168.180.8
PING 192.168.180.8 (192.168.180.8) 56(84) bytes of data.
64 bytes from 192.168.180.8: icmp_seq=1 ttl=64 time=1.01 ms
64 bytes from 192.168.180.8: icmp_seq=2 ttl=64 time=1.28 ms
64 bytes from 192.168.180.8: icmp_seq=3 ttl=64 time=1.07 ms
^C
--- 192.168.180.8 ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 2003ms
rtt min/avg/max/mdev = 1.012/1.125/1.287/0.117 ms
rk3399:/ #

```

