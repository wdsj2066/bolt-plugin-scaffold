#!/bin/bash

# Bolt 插件脚手架 - 构建脚本

set -e

echo "============================================"
echo "   Bolt 插件脚手架 - 构建脚本"
echo "============================================"
echo ""

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "[错误] 未找到 Maven，请确保 Maven 已安装"
    exit 1
fi

echo "[1/3] 清理旧构建..."
mvn clean -q

echo "[2/3] 编译代码..."
mvn compile -q

echo "[3/3] 打包插件..."
mvn package -DskipTests -q

echo ""
echo "============================================"
echo "   构建成功！"
echo "============================================"
echo ""
echo "插件包位置: target/bolt-plugin-scaffold-1.0.0.jar"
echo ""
echo "使用说明:"
echo "  1. 开发你的插件（参考 src/main/java/com/example/bolt/plugin/examples/）"
echo "  2. 在 META-INF/services 中注册你的插件类"
echo "  3. 运行 ./build.sh 重新打包"
echo "  4. 将 JAR 上传到 Bolt 系统"
