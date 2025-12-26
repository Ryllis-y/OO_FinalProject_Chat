# 架构图生成程序使用说明

## 方案一：Python脚本生成（推荐）

### 安装依赖

```bash
pip install -r requirements.txt
```

### 运行脚本

```bash
python draw_architecture.py
```

运行后会生成 `architecture_diagram.png` 文件（高分辨率PNG格式，300 DPI）。

### 自定义选项

修改 `draw_architecture.py` 中的参数：

- **修改输出文件名**：
  ```python
  save_figure('my_architecture.png')
  ```

- **修改分辨率**：
  ```python
  save_figure('architecture_diagram.png', dpi=150)  # 降低分辨率，文件更小
  save_figure('architecture_diagram.png', dpi=600)  # 提高分辨率，更清晰
  ```

- **预览图片**（在脚本末尾取消注释）：
  ```python
  show_figure()  # 会弹出窗口显示图片
  ```

## 方案二：Mermaid图表（适合Markdown文档）

### 使用步骤

1. **在线渲染**（最简单）：
   - 访问 https://mermaid.live/
   - 打开 `architecture_mermaid.md` 文件
   - 复制Mermaid代码到在线编辑器
   - 导出为PNG或SVG

2. **在Markdown中使用**：
   - 如果使用Typora、GitLab、GitHub等支持Mermaid的编辑器
   - 直接粘贴Mermaid代码，会自动渲染

3. **在VS Code中使用**：
   - 安装 "Markdown Preview Mermaid Support" 插件
   - 预览Markdown文件时会自动渲染Mermaid图表

## 方案三：使用其他工具

### Draw.io / diagrams.net

1. 访问 https://app.diagrams.net/
2. 可以导入Mermaid代码（如果支持）
3. 或者手动绘制（参考 `项目结构设计图.md` 中的ASCII图）

### PlantUML

如果需要更专业的UML图，可以使用PlantUML格式（需要安装PlantUML）。

## 输出文件说明

- `architecture_diagram.png`: Python脚本生成的架构图（PNG格式）
- `architecture_mermaid.md`: Mermaid格式的架构图代码
- `draw_architecture.py`: Python绘图脚本

## 注意事项

1. **中文字体**：如果Python脚本显示中文为方块，需要安装中文字体：
   - Windows: 系统自带SimHei
   - macOS: 使用Arial Unicode MS或安装中文字体
   - Linux: 安装中文字体包

2. **分辨率**：默认300 DPI适合打印和PPT使用，如果只是网页展示可以用150 DPI

3. **颜色**：颜色可以在脚本中自定义，已经设置为适合打印的颜色


