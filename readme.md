## Android网络配置
### 方法一： 运行配置脚本

1、复制脚本

将下载的脚本通过adb推送到安卓设备的data目录

```shell
adb root
adb remount
adb push configAndroidNet.sh /data
```

2、进入/data目录执行`sh configAndroidNet.sh`运行脚本，根据提示输入RK1808人工智能计算棒虚拟网卡（本例为“usb0”）和本地与外网相连接的网卡（本例为”eth0“），输入正确网卡名称即可自动配置，可以根据输出看到RK1808人工智能计算棒虚拟网卡在执行脚本后被设置了相关的信息

```shell
E:\origin_yolov3_demo adb shell
remount succeeded
rk3399:/ # cd data/
rk3399:/data # sh configAndroidNet.sh
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
    link/ether 66:56:56:3d:5b:11 brd ff:ff:ff:ff:ff:ff
Please input 1808 network card name:
usb0
Please input local network card name:
eth0
616 Route removed fe80::/64 dev usb0
614 Address removed fe80::6456:56ff:fe3d:5b11/64 usb0 196 253
616 Route updated fe80::/64 dev usb0
200 0 Tether operation succeeded
200 0 Tether operation succeeded
200 0 ipfwd operation succeeded
200 0 Nat operation succeeded
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
    link/ether 66:56:56:3d:5b:11 brd ff:ff:ff:ff:ff:ff
    inet 192.168.180.1/24 brd 192.168.180.255 scope global usb0
       valid_lft forever preferred_lft forever
    inet6 fe80::6456:56ff:fe3d:5b11/64 scope link
       valid_lft forever preferred_lft forever
rk3399:/data #
```





###方法二： 手敲命令行

1、查看网卡名称

Android进入shell后需要输入"su"命令切换到root用户，然后运行ip addr查看当前网卡的状态，可以看到eth0为本地网卡用于访问外网，usb0为usb网卡（RK1808人工智能计算棒虚拟网卡），在不同环境下网卡名称可能不一样，不同上网环境下外网网卡也可能不一样，以实际使用的为准。

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

2、运行一下命令配置usb0网卡IP掩码以及路由（如实际外网网卡为其他网卡需修改eth0为实际外网的网卡名称）

```shell
ifconfig usb0 192.168.180.1 netmask 255.255.255.0 up
ip rule add from all oif usb0 table local_network
ip rule add from all iif usb0 table eth0
ip route add 192.168.180.0/24 dev usb0 proto static table local_network
ip route flush cache
```

3、配置网络共享

```shell
ndc tether interface add usb0
ndc tether dns set 8.8.4.4 8.8.8.8
ndc ipfwd enable tethering
ndc nat enable usb0 eth0 0
```

运行流程如下：

```shell

rk3399:/ # ifconfig usb0 192.168.180.1 netmask 255.255.255.0 up
rk3399:/ # ip rule add from all oif usb0 table local_network
rk3399:/ # ip rule add from all iif usb0 table eth0
rk3399:/ # ip route add 192.168.180.0/24 dev usb0 proto static table local_network
rk3399:/ # ip route flush cache
rk3399:/ # ndc tether interface add usb0
616 Route removed fe80::/64 dev usb0
614 Address removed fe80::ac40:92ff:fe3c:e344/64 usb0 128 253
200 0 Tether operation succeeded
rk3399:/ # ndc tether dns set 8.8.4.4 8.8.8.8
200 0 Tether operation succeeded
rk3399:/ # ndc ipfwd enable tethering
200 0 ipfwd operation succeeded
rk3399:/ # ndc nat enable usb0 eth0 0
200 0 Nat operation succeeded
rk3399:/ #
```

4、再次查看再次运行`ip addr`可看到usb0已有配置ip地址，运行` ip route`可看到180网段的路由也以添加

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

