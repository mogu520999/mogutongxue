package ai.xuexi.service;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Copyright © 2025 integration-projects-maven. All rights reserved.
 * ClassName KjjMcpService.java
 * author 舒一笑
 * version 1.0.0
 * Description TODO
 * createTime 2025年07月26日 09:27:13
 */
@Service
@Log4j2
public class KjjMcpService {
    //所有按键码表
    static final List<Integer> validKeyCodes = Arrays.asList(1, 2, 3, 4, 8, 9, 12, 13, 16, 17, 18, 19, 20, 27, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 144, 145, 186, 187, 188, 189, 190, 191, 192, 219, 220, 221, 222);

    //所有按键码
    //1-4,8,9,12,13,16-20,27,32-123,144,145,186-192,219-222

    //


    /*{
  "shortcuts": [
    {
      "id": "1",
      "keys": "ctrl+Right",
      "key_code": "17+39",
      "function": "切歌或下一首",
      "description": "切换到下一首歌曲",
      "enabled": true
    }
  ]
}*/


    /**
     * 读取config.json, 用读取到的值
     */
    @Tool(description = "读取所有可以的快捷操作,如果用户提到了'蘑菇同学'且没有提到'打开'时,可以先调用本方法,查询是否有符合用户要求的快捷操作")
    public String readAllShortcutOperations() {
        // 检查文件是否存在
        if (!FileUtil.exist("config.json")) {
            return "配置文件config.json不存在";
        }
        // 用hutool读取config.json
        String config = FileUtil.readUtf8String("config.json");
        // 检查配置内容是否为空
        if (config == null || config.trim().isEmpty()) {
            return "配置文件内容为空";
        }

        //把读取到的json字符串转换成json对象
        JSONObject jsonObject = JSON.parseObject(config);

        // 检查json对象是否为空
        if (jsonObject == null) {
            return "配置文件格式不正确，无法解析为JSON对象";
        }

        //获取json对象中的shortcuts
        JSONArray shortcutsArray = jsonObject.getJSONArray("shortcuts");

        StringBuffer sb = new StringBuffer();

        // 遍历数组中的每个元素
        for (int i = 0; i < shortcutsArray.size(); i++) {
            JSONObject shortcut = shortcutsArray.getJSONObject(i);
            if (shortcut == null) {
                continue;
            }

            // 获取各个属性，添加空值检查
            String id = shortcut.getString("id");
            String keys = shortcut.getString("keys");
            String key_code = shortcut.getString("key_code");
            String function = shortcut.getString("function");
            String description = shortcut.getString("description");

            // 对Boolean值进行特殊处理
            Boolean enabledObj = shortcut.getBoolean("enabled");
            boolean enabled = enabledObj != null ? enabledObj : false;

            // 构建返回字符串
            sb.append("id为").append(id != null ? id : "未知")
                    .append("的快捷键的作用是").append(description != null ? description : "无描述")
                    .append(",如果用户提到了'").append(function != null ? function : "未知功能")
                    .append("'，则需要执行的键码为").append(key_code != null ? key_code : "无键码");
        }

        if (sb.length() == 0) {
            return "未找到有效的快捷键配置";
        }

        return sb.toString();

    }

    /**
     * 用ai输入的 key_code 真正的执行快捷键
     */


    @Tool(description = "用 key_code 执行快捷键")
//    "17+39"
    public String executeShortcut(@ToolParam(description = "真正需要被执行的快捷键代码 key_code ,需要为'数字+数字' 这种格式的字符串 如 '17+39'") String key_code) throws AWTException {
        //解析key_code并执行快捷键
        String[] keys = key_code.split("\\+");
        Robot robot = new Robot();
        Integer[] keyCodes = new Integer[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String k = keys[i].trim();
            //判断k是否在validKeyCodes中
            Integer keyCode = Integer.parseInt(k);
            if (validKeyCodes.contains(keyCode)) {
                keyCodes[i] = keyCode;
            } else {
                return "您输入的快捷键代码有误" + key_code + "中的" + k + "不是有效的快捷键代码";
            }
        }
        for (Integer keyCode : keyCodes) {
            System.out.println("按下" + keyCode);
            robot.keyPress(keyCode);
        }
        // 逆序释放按键（对于组合键很重要）
        for (int i = keyCodes.length - 1; i >= 0; i--) {
            robot.keyRelease(keyCodes[i]);
        }
        String key_name = KeyEvent.getKeyText(keyCodes[0]);
        for (Integer keyCode : keyCodes) {
            //根据键码获取按键名
            String keyName = KeyEvent.getKeyText(keyCode);
            key_name += keyName;
        }

        return "已按下" + key_name + "键,完成指定操作";
    }




    static {
        log.info("OpenExeService 已加载，准备注册为工具");
    }

    @Tool(description = "获取指定文件夹下的所有文件，当用户提到'蘑菇同学'与'打开'时可以先调用本方法查询需要打开的文件的具体位置")
    public static List<String> listFilesInDirectory(
            @ToolParam(description = "要扫描的目录路径,默认为桌面'C:/Users/Administrator/Desktop',如果桌面上没有找到,可以直接询问用户,他的应用快捷方式都放在了那里") String dirPath,
            @ToolParam(description = "可选的文件名过滤器，为空则返回所有文件") String filter) throws IOException {
        log.info("Opening file: {}", dirPath);
        List<String> fileNames = new ArrayList<>();
        if (dirPath == null || dirPath.isEmpty()) {
            dirPath = "C:\\Users\\Administrator\\Desktop";
        }
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) {
            throw new IllegalArgumentException("目录不存在: " + dirPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    String fileName = path.getFileName().toString();
                    if (filter == null || filter.isEmpty() || fileName.toLowerCase().contains(filter.toLowerCase())) {
                        // 添加文件大小信息
                        long size = Files.size(path);
                        String sizeStr;
                        if (size > 1024 * 1024 * 1024) { // 大于1GB
                            sizeStr = String.format("%.2fGB", size / (1024.0 * 1024.0 * 1024.0));
                        } else if (size > 1024 * 1024) { // 大于1MB
                            sizeStr = (size / 1024 / 1024) + "MB";
                        } else {
                            sizeStr = (size / 1024) + "KB";
                        }
                        fileNames.add(fileName + " (" + sizeStr + ")");
                    }
                }
            }
            // 按文件名排序
            fileNames.sort(Comparator.naturalOrder());
        }
        return fileNames;
    }


    @Tool(description = "打开指定文件，支持中文路径和多种打开方式,在调用本方法之前最好先用其他方法确定要打开的文件是否存在")
    public static String openFileWithDefaultApp(
            @ToolParam(description = "要打开的文件路径,必须指定完整路径") String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return "文件不存在: " + filePath;
        }

        try {
            // 添加文件类型检查
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".txt") || fileName.endsWith(".doc") ||
                    fileName.endsWith(".xls") || fileName.endsWith(".ppt") ||
                    fileName.endsWith(".pdf") || fileName.endsWith(".jpg") ||
                    fileName.endsWith(".png") || fileName.endsWith(".mp4")) {

                // 添加文件锁定检查
                if (isFileLocked(file)) {
                    return "文件正在被其他程序使用，无法打开: " + filePath;
                }

                Desktop.getDesktop().open(file);
            } else if (fileName.endsWith(".lnk")) {
                // 使用 cmd 命令打开 .lnk 文件
//                new ProcessBuilder("explorer.exe", filePath).start();
                new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"" + filePath + "\"").start();
            } else {
                // 对于可执行文件，使用ProcessBuilder打开
                ProcessBuilder pb = new ProcessBuilder(filePath);
                pb.start();
            }
        } catch (IOException e) {
            return "无法打开文件: " + e.getMessage();
        }
        return "文件已打开: " + filePath;
    }

    /**
     * 检查文件是否被锁定
     */
    private static boolean isFileLocked(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder("handle.exe", file.getAbsolutePath());
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

}
