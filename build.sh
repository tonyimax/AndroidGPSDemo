#!/bin/bash

# Android GPS Demo 手动构建脚本

echo "开始构建Android GPS APK..."

# 设置环境变量
export ANDROID_HOME=~/Android/Sdk
export BUILD_TOOLS=$ANDROID_HOME/build-tools/34.0.0
export PLATFORM=$ANDROID_HOME/platforms/android-35

# 创建构建目录
mkdir -p build/gen build/obj build/apk

echo "1. 生成R.java..."
$BUILD_TOOLS/aapt package -f -m \
    -J build/gen \
    -M app/src/main/AndroidManifest.xml \
    -S app/src/main/res \
    -I $PLATFORM/android.jar

if [ $? -ne 0 ]; then
    echo "生成R.java失败"
    exit 1
fi

echo "2. 编译Java源文件..."
javac -d build/obj \
    -classpath $PLATFORM/android.jar \
    -sourcepath app/src/main/java:build/gen \
    app/src/main/java/com/example/gps/*.java \
    build/gen/com/example/gps/R.java

if [ $? -ne 0 ]; then
    echo "编译Java失败"
    exit 1
fi

echo "3. 转换为DEX..."
$BUILD_TOOLS/d8 --lib $PLATFORM/android.jar \
    --output build/apk \
    build/obj/com/example/gps/*.class

if [ $? -ne 0 ]; then
    echo "DEX转换失败"
    exit 1
fi

echo "4. 打包APK..."
$BUILD_TOOLS/aapt package -f \
    -M app/src/main/AndroidManifest.xml \
    -S app/src/main/res \
    -I $PLATFORM/android.jar \
    -F build/gps-unsigned.apk \
    build/apk

if [ $? -ne 0 ]; then
    echo "打包APK失败"
    exit 1
fi

# 添加DEX文件到APK
cd build/apk
zip -j ../gps-unsigned.apk classes.dex
cd ../..

echo "5. 签名APK..."
# 使用debug密钥签名
if [ ! -f ~/.android/debug.keystore ]; then
    echo "创建debug密钥..."
    keytool -genkey -v -keystore ~/.android/debug.keystore \
        -storepass android -alias androiddebugkey \
        -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
fi

$BUILD_TOOLS/apksigner sign --ks ~/.android/debug.keystore \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out build/gps-debug.apk \
    build/gps-unsigned.apk

if [ $? -ne 0 ]; then
    echo "签名失败"
    exit 1
fi

echo "✅ APK构建成功: build/gps-debug.apk"

# 检查是否有连接的设备
if command -v adb &> /dev/null; then
    DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l)
    if [ $DEVICES -gt 0 ]; then
        echo ""
        read -p "检测到已连接设备,是否安装APK? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "安装APK到设备..."
            adb install -r build/gps-debug.apk
            echo "✅ 安装完成!"
        fi
    else
        echo "未检测到已连接的Android设备"
        echo "你可以手动安装: adb install -r build/gps-debug.apk"
    fi
else
    echo "未安装adb工具"
    echo "安装命令: sudo apt install adb"
    echo "APK已生成,可以手动传输到设备安装"
fi
