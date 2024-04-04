import win32gui
import win32con
import win32api
import subprocess
import platform
import threading
import time
from pynput import keyboard

def move_window(window_title, x, y):
    # 获取窗口句柄
    hwnd = win32gui.FindWindow(None, window_title)
    if hwnd == 0:
        print("Window not found.")
        return
    _, _, w, h = win32gui.GetWindowRect(hwnd)
    # 移动窗口
    # 1280, 764
    # 853, 509
    win32gui.MoveWindow(hwnd, x, y, 853, 509, True)

def is_focus():
    current_window = win32gui.GetForegroundWindow()
    # 获取当前焦点窗口的标题
    current_window_title = win32gui.GetWindowText(current_window)
    return current_window_title == "ブルアカ"

def adb_command(command):
    """
    执行adb命令的函数
    """
    print("send")
    adb_path = "adb"  # 如果adb在环境变量中，可以直接使用adb，否则需要指定adb的路径
    if platform.system() == "Windows":
        adb_path += ".exe"
    
    full_command = [adb_path] + command
    subprocess.run(full_command)

def on_press(key):
    if not is_focus():
        return
    try:
        if key == keyboard.Key.space: 
            adb_command(["shell", "input", "tap", "1719", "394"])
        elif key == keyboard.Key.esc:
            adb_command(["shell", "input", "tap", "537", "394"])
    except Exception:
        pass
    try:
        key_char = key.char
        # 这里可以根据按键定义点击位置
        if key_char == "1":
            adb_command(["shell", "input", "tap", "1387", "960"])
        elif key_char == "2":
            adb_command(["shell", "input", "tap", "1488", "960"])
        elif key_char == "3":
            adb_command(["shell", "input", "tap", "1585", "960"])
        elif key_char == "a":
            adb_command(["shell", "input", "tap", "1700", "1020"])
        elif key_char == "s":
            adb_command(["shell", "input", "tap", "1700", "960"])
    except AttributeError:
        pass

def on_release(key):
    if key == keyboard.Key.delete:
        # 如果按下delete键，则停止监听
        return False

def listen_keyboard():
    with keyboard.Listener(
        on_press=on_press,
        on_release=on_release) as listener:
        listener.join()

if __name__ == "__main__":
    # 调用函数移动指定窗口
    move_window("ブルアカ", 500, 300)
    # 连接wsa
    adb_command(["connect", "127.0.0.1:58526"])
    listen_keyboard()