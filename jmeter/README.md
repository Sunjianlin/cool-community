# JMeter 登录接口压测指南

## 一、准备工作

### 1. 安装JMeter
- 下载地址: https://jmeter.apache.org/download_jmeter.cgi
- 下载后解压，运行 `bin/jmeter.bat` (Windows) 或 `bin/jmeter.sh` (Linux/Mac)

### 2. 初始化测试用户数据

执行SQL脚本初始化测试用户：

```bash
mysql -u root -p cool_community < jmeter/init_test_users.sql
```

或者在MySQL客户端中执行：
```sql
source d:/Project/java/cool-community/jmeter/init_test_users.sql
```

## 二、压测文件说明

| 文件 | 说明 |
|-----|------|
| `login-stress-test.jmx` | JMeter测试计划文件 |
| `users.csv` | 测试用户数据文件 |
| `init_test_users.sql` | 初始化测试用户SQL脚本 |

## 三、运行压测

### 方式一：GUI模式（推荐用于调试）

1. 启动JMeter GUI
2. 打开测试计划：`文件 -> 打开 -> 选择 login-stress-test.jmx`
3. 修改CSV文件路径：
   - 找到 `用户数据CSV配置` 元素
   - 修改 `filename` 为 `users.csv` 的绝对路径
4. 点击 `启动` 按钮开始测试
5. 查看结果：
   - 察看结果树：查看每个请求详情
   - 聚合报告：查看统计汇总
   - 图形结果：查看响应时间曲线

### 方式二：命令行模式（推荐用于正式压测）

```bash
# 基本命令
jmeter -n -t login-stress-test.jmx -l result.jtl -e -o report

# 参数说明
# -n: 非GUI模式
# -t: 测试计划文件
# -l: 结果输出文件
# -e: 测试结束后生成HTML报告
# -o: HTML报告输出目录
```

示例：
```bash
cd d:/Project/java/cool-community/jmeter
jmeter -n -t login-stress-test.jmx -l result.jtl -e -o html-report
```

## 四、压测参数配置

### 当前配置
| 参数 | 值 | 说明 |
|-----|-----|------|
| 线程数 | 100 | 并发用户数 |
| Ramp-Up时间 | 10秒 | 启动所有线程的时间 |
| 循环次数 | 10 | 每个线程执行的次数 |
| 持续时间 | 60秒 | 测试持续时间 |

### 不同压测场景配置建议

#### 1. 低并发测试（功能验证）
```
线程数: 10
Ramp-Up: 5秒
循环次数: 5
```

#### 2. 中等并发测试
```
线程数: 100
Ramp-Up: 10秒
循环次数: 10
```

#### 3. 高并发测试
```
线程数: 500
Ramp-Up: 30秒
循环次数: 20
```

#### 4. 极限压力测试
```
线程数: 1000
Ramp-Up: 60秒
循环次数: 50
```

## 五、结果分析

### 关键指标

| 指标 | 说明 | 参考值 |
|-----|------|-------|
| Average | 平均响应时间 | < 500ms |
| 90% Line | 90%请求的响应时间 | < 1000ms |
| 95% Line | 95%请求的响应时间 | < 2000ms |
| Throughput | 吞吐量(QPS) | 越高越好 |
| Error % | 错误率 | < 1% |

### 聚合报告解读

```
Label        - 请求名称
Samples      - 总请求数
Average      - 平均响应时间(ms)
Median       - 中位数响应时间(ms)
90% Line     - 90%请求响应时间(ms)
95% Line     - 95%请求响应时间(ms)
99% Line     - 99%请求响应时间(ms)
Min          - 最小响应时间(ms)
Max          - 最大响应时间(ms)
Error %      - 错误率(%)
Throughput   - 吞吐量(请求/秒)
Received KB/sec - 接收数据量(KB/秒)
Sent KB/sec  - 发送数据量(KB/秒)
```

## 六、常见问题排查

### 1. 连接超时
- 检查服务是否启动
- 检查端口是否正确
- 检查防火墙设置

### 2. 高错误率
- 检查服务日志
- 检查数据库连接池配置
- 检查Redis连接配置

### 3. 响应时间过长
- 检查数据库慢查询
- 检查Redis缓存命中率
- 检查服务器CPU/内存使用率

## 七、性能优化建议

### 1. 服务端优化
- 增加数据库连接池大小
- 开启Redis缓存
- 优化SQL查询
- 增加JVM堆内存

### 2. JMeter优化
- 使用非GUI模式
- 禁用察看结果树（正式压测时）
- 使用CSV输出结果
- 增加JMeter内存

## 八、压测报告示例

```
======== 压测结果汇总 ========
测试时间: 2024-01-01 12:00:00
并发用户: 100
总请求数: 1000
成功率: 99.8%
平均响应时间: 156ms
最大响应时间: 892ms
吞吐量: 856.2 请求/秒
```

## 九、注意事项

1. 压测前确保服务器资源充足
2. 逐步增加并发，观察系统表现
3. 压测期间监控系统资源使用情况
4. 压测完成后清理测试数据
5. 生产环境压测需谨慎，建议在测试环境进行
