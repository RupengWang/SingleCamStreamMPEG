<!-- TOC -->

- [介绍](#介绍)
    - [项目介绍](#项目介绍)
    - [第三方工具介绍](#第三方工具介绍)
- [准备工作](#准备工作)
    - [安装gradle](#安装gradle)
        - [类Unix系统(Ubuntu, CentOS, RedHat等)](#类unix系统ubuntu-centos-redhat等)
        - [Windows安装Gradle](#windows安装gradle)
    - [jsmpeg](#jsmpeg)
        - [依赖](#依赖)
- [打包](#打包)
    - [java](#java)
- [运行](#运行)

<!-- /TOC -->

# 介绍
## 项目介绍
类别 | 介绍
-- |-- 
开发语言 | java
开发工具 | Intellij IDEA
运行平台 | Windows, Linux, MAC OS

本项目中我们主要用来抓取网络摄像头的帧，并对其进行人脸检测加框，然后实时推流到前端页面中进行播放，其实这种场景的应用有很多，但是大部分是基于CS架构的，即针对的是桌面应用，这个直接使用Opencv就可以很快实现，但是针对前端网页，就会面对很多问题。目前主流的和直播有关的协议有RTMP,RTSP这些都是比较重量级的协议，面对一般的用户量较少的基于WEB的管理平台来说，太重量级。
所以本项目决定采用MPEG-TS流的方式，通过websockt实时将视频包传送到前端页面，直接通过浏览器进行解码。

## 第三方工具介绍
* JavaCV
Java interface to OpenCV, FFmpeg, and more。本项目主要使用JavaCV封装的FFmpeg接口进行抓图和推流的工作。
[官方地址](https://github.com/bytedeco/javacv)
* BoofCV
BoofCV is an open source real-time computer vision library written entirely in Java and released under the Apache License 2.0. Functionality includes low-level image processing, camera calibration, feature detection/tracking, structure-from-motion, classification, and recognition.
本项目主要使用boofcv中的追踪算法，进行人脸追踪。
[官方地址](https://github.com/lessthanoptimal/BoofCV)
* Gson
Google的json处理工具
* Jsmpeg
mpeg1视频的前端解码库
# 准备工作
## 安装gradle
### 类Unix系统(Ubuntu, CentOS, RedHat等)
* 安装sdkman(软件管理工具)

```bash
$ curl -s "https://get.sdkman.io" | bash
```
```bash
$ source "$HOME/.sdkman/bin/sdkman-init.sh"
```

* 检查是否安装成功

```bash
$ sdk version
```
* 安装gradle

```bash
$ sdk install gradle 4.7
```

### Windows安装Gradle

* 安装chocolatey(windows上的软件安装工具)

    * 使用cmd.exe 
    
    ```bash 
    @"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
    ```
    * 使用PowerShell.exe 
    
    首先运行
    ```bash
    Get-ExecutionPolic
    ```
    如果返回
    ```bash
    Restricted
    ```
    则运行
    ```bash
    Set-ExecutionPolicy AllSigned 或者 Set-ExecutionPolicy Bypass -Scope Process
    ```
    最后运行
    ```bash
    Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
    ```
* 安装Gradle
```bash
choco install gradle
```  

## jsmpeg
### 依赖
```shell
cd $PROJECT_DIR/src/main/resources/jsmpeg
npm install
```

# 打包
## java
```bash
$ gradle copyJar
```
在build/libs中会生成 Antaeus-1.0.jar

```bash
$ gradle build
```

# 运行
1. 启动jsmpeg服务
```bash
cd $PROJECT_DIR/src/main/resources/jsmpeg
node websocket-relay.js test
```

2. 启动java
```bash
$ java -cp build/libs/SingleCamStream-1.0-SNAPSHOT.jar  com.oceanai.StreamOriginMain $RTSP_URL http://localhost/test
```

3. 查看
```bash
cd $PROJECT_DIR/src/main/resources/jsmpeg
# copy the html file named view-stream.html to your web server directory, for example, apache2 would be '/var/www/html/'
# now you can view the stream on you brower by http://$SERVER_IP/view-stream.html
```