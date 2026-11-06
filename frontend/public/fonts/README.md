# PDF 中文字体

来源：[Google Fonts / Noto Sans SC](https://github.com/google/fonts/tree/main/ofl/notosanssc)，SIL OFL 1.1，见 `OFL.txt`。

本项目将可变 TTF 固定为 400/700 两种字重，分别设置唯一 PostScript 名称，避免 PDFKit 缓存混淆；为 U+200A / U+200B 增加空轮廓、零宽度字形。PDF 组件使用 U+200A 分词机会处理连续中文换行，避免插入可见的英文连字符。

静态文件直接随项目提供，运行和导出不需要 Python。需要重新生成时，安装 `fonttools`，下载上游可变 TTF，然后运行：

```text
python scripts/prepare-fonts.py path/to/NotoSansSC-variable.ttf
```
