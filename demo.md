

1、下载附件中master_yolov3.zip和master_yolov3_demo.apk(如需查看源码可下载master_yolov3_Android_demo.zip查看)。

解压master_yolov3.zip文件夹，里面的1808文件夹就是在RK1808人工智能计算棒的示例demo。master_yolov3_demo.apk是宿主安卓机器运行的程序。

2、将RK1808人工智能计算棒插入pc机，然后参考wiki页面`配置计算棒网络共享->配置NAT功能`，完成RK1808人工智能计算棒访问NAT。

3、在pc拷贝解压目录中的1808目录至计算棒：

scp  -r  1808/  toybrick@192.168.180.8:/home/toybrick/

4、进入计算棒：

ssh  toybrick@192.168.180.8

5、安装依赖包：

sudo dnf  install  python3-opencv  -y

6、进入刚才拷贝的1808目录，运行1808端程序。

![1.png](http://t.rock-chips.com/data/attachment/portal/201907/11/1562832613162900.png)

7、可以运行后按照如下方式参考设置RK1808计算棒中程序开机启动，或走标准fedora开启启动流程自行配置。

![1.png](http://t.rock-chips.com/data/attachment/portal/201907/23/1563870824891141.png)



8、把RK1808计算棒从pc机移除，插入宿主安卓机器，然后参照`配置计算棒网络共享->Android网络配置`配置安卓与RK1808计算棒的网络（此配置操作在安卓重启之后将会丢失，顾每次都需重新配置，如果需要开机配置网络要走安卓的开机启动流程开机运行网络配置脚本）

9、安装master_yolov3_demo.apk。

10、在安卓运行master_yolov3_demo.apk即可，如果画面一直卡住一般是网络没有配置成功（可进入adb shell执行 ping 192.168.180.8验证）或者

RK1808计算棒没有设置开机启动