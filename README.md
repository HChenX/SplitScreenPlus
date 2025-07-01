### SplitScreenPlus

#### 简介：
- 强制小米 pad 支持上下分屏与左右分屏动态切换。
- 理论只支持，也只会支持 HyperOS2。

#### 使用：
- 安装模块并激活后，重启一次系统界面；
- 执行命令：
```text
// 关闭上下分屏模式
su -c settings put system sothx_project_treble_vertical_screen_split_enable 0
// 开启上下分屏模式
su -c settings put system sothx_project_treble_vertical_screen_split_enable 1
```
- 即可使用！

#### 鸣谢：
- [Sothx](https://github.com/sothx)

#### 免责声明：
- 模块修改了系统逻辑，可能存在不稳定等情况，如果使用则代表您愿意承担一切后果！

#### 下载建议：
- 正常使用下载 release 版本。
- 反馈 BUG 请使用 debug 版本。
