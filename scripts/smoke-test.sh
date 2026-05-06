#!/bin/bash

# Cool Community 冒烟测试脚本 (Linux/Mac)

BASE_URL="http://localhost:8082/api"
PASS_COUNT=0
FAIL_COUNT=0

echo "========================================"
echo "  Cool Community 冒烟测试脚本"
echo "========================================"
echo ""

# 测试函数
test_api() {
    local name=$1
    local url=$2
    local method=$3
    local data=$4
    local expect_success=$5
    
    echo "测试: $name"
    
    if [ "$method" = "GET" ]; then
        status=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    else
        status=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    if [ "$expect_success" = "true" ]; then
        if [ "$status" = "200" ]; then
            echo "    ✓ 通过 (状态码: $status)"
            ((PASS_COUNT++))
        else
            echo "    ✗ 失败 (状态码: $status)"
            ((FAIL_COUNT++))
        fi
    else
        if [ "$status" != "200" ]; then
            echo "    ✓ 通过 (状态码: $status)"
            ((PASS_COUNT++))
        else
            echo "    ✗ 失败 (状态码: $status)"
            ((FAIL_COUNT++))
        fi
    fi
    echo ""
}

# 执行测试
test_api "服务健康检查" "${BASE_URL}/post/list?page=1&pageSize=1" "GET" "" "true"
test_api "帖子列表接口" "${BASE_URL}/post/list?page=1&pageSize=10" "GET" "" "true"
test_api "话题列表接口" "${BASE_URL}/topic/list?page=1&pageSize=10" "GET" "" "true"
test_api "产品列表接口" "${BASE_URL}/product/list?page=1&pageSize=10" "GET" "" "true"
test_api "分类列表接口" "${BASE_URL}/category/list" "GET" "" "true"
test_api "搜索接口" "${BASE_URL}/search/all?keyword=测试" "GET" "" "true"
test_api "用户注册参数校验" "${BASE_URL}/user/register" "POST" "{}" "false"
test_api "用户登录错误处理" "${BASE_URL}/user/login" "POST" '{"username":"test","password":"wrong"}' "false"
test_api "帖子不存在处理" "${BASE_URL}/post/detail/999999999" "GET" "" "false"
test_api "搜索空关键词处理" "${BASE_URL}/search/all?keyword=" "GET" "" "false"

# 输出结果
echo "========================================"
echo "  测试结果汇总"
echo "========================================"
echo "  通过: $PASS_COUNT"
echo "  失败: $FAIL_COUNT"
echo "  总计: $((PASS_COUNT + FAIL_COUNT))"
echo "========================================"

if [ $FAIL_COUNT -eq 0 ]; then
    echo "  状态: ✓ 冒烟测试通过"
    exit 0
else
    echo "  状态: ✗ 冒烟测试失败"
    exit 1
fi
