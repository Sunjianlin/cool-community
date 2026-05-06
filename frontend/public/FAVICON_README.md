# Favicon 图标说明

## 图标设计

### 设计理念
- **字母C**: 代表 "Cool Community"
- **渐变色**: 蓝紫渐变，现代科技感
- **圆点**: 代表社区用户，体现社交属性

### 图标预览

```
┌──────────────────────┐
│    ╭─────────────╮   │
│   ╭╯    ┌───┐    ╰╮  │
│   │     │ C │      │  │
│   │  ●  └───┘  ●   │  │
│   ╰╮            ╭─╯  │
│    ╰────────────╯    │
│         ●            │
└──────────────────────┘
```

## 文件说明

| 文件 | 尺寸 | 用途 |
|-----|------|------|
| `favicon.svg` | 64x64 | 浏览器标签页图标 |
| `apple-touch-icon.svg` | 180x180 | iOS设备桌面快捷方式 |

## 如何生成PNG图标（可选）

如果需要兼容旧浏览器，可以使用以下方法生成PNG：

### 方法1：在线转换
1. 访问 https://cloudconvert.com/svg-to-png
2. 上传 `favicon.svg`
3. 设置尺寸为 32x32, 16x16
4. 下载生成的PNG文件

### 方法2：使用ImageMagick
```bash
# 安装ImageMagick后执行
convert favicon.svg -resize 32x32 favicon-32x32.png
convert favicon.svg -resize 16x16 favicon-16x16.png
convert apple-touch-icon.svg -resize 180x180 apple-touch-icon.png
```

### 方法3：使用Node.js
```bash
npm install -g svgexport
svgexport favicon.svg favicon-32x32.png 32:32
svgexport favicon.svg favicon-16x16.png 16:16
```

## 使用方式

已在 `index.html` 中配置：
```html
<link rel="icon" type="image/svg+xml" href="/favicon.svg" />
<link rel="apple-touch-icon" href="/apple-touch-icon.svg" />
<meta name="theme-color" content="#3498db" />
```

## 颜色方案

| 颜色 | 十六进制 | 用途 |
|-----|---------|------|
| 主蓝色 | #3498db | 渐变起始色 |
| 主紫色 | #8e44ad | 渐变结束色 |
| 白色 | #ffffff | 图标内容 |

## 自定义修改

如需修改图标颜色，编辑 `favicon.svg` 中的渐变定义：

```svg
<linearGradient id="bgGradient" x1="0%" y1="0%" x2="100%" y2="100%">
  <stop offset="0%" style="stop-color:#YOUR_COLOR_1"/>
  <stop offset="100%" style="stop-color:#YOUR_COLOR_2"/>
</linearGradient>
```
