package top.llang.rdp.http;

import spark.Request;
import spark.Response;
import spark.Spark;
import spark.utils.IOUtils;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static spark.Spark.*;

public class HttpRDP3 {
    private final ExecutorService compressionPool = Executors.newFixedThreadPool(1);
    private Future<?> currentTask; // 新增成员变量
    private final AtomicReference<BufferedImage> screenImageRef = new AtomicReference<>();
    private final Dimension screenSize;
//    private final ExecutorService compressionPool = Executors.newFixedThreadPool(2);
    private volatile byte[] lastFrame;
    private BufferedImage previousFrame;
private float Myclarity;
    public HttpRDP3(String host, int port, float clarity) {
        System.out.println("HttpRDP3 初始化: " + host + ":" + port + " 清晰度: " + clarity);
        this.Myclarity=clarity;
//        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenSize = new Dimension(2560, 1600);
        Timer captureTimer = new Timer(50, e -> captureScreen());
        captureTimer.start();
        port(port);
        after((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            //res.type(getContentType(req)); // 动态设置Content-Type
        });
        get("/", this::html);
        get("/screenshot", this::getScreenshot);



    }
    private String readHtmlFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading HTML file";
        }
    }




    private void captureScreen() {
        try {
            Robot robot = new Robot();
            BufferedImage newImage = robot.createScreenCapture(new Rectangle(screenSize));
            screenImageRef.set(newImage);

            // 取消之前的任务
            if (currentTask != null && !currentTask.isDone()) {
                currentTask.cancel(true); // 中断正在执行的任务
            }
            currentTask = compressionPool.submit(() -> processFrame(newImage));
        } catch (AWTException ex) {
            System.err.println("Capture error: " + ex.getMessage());
        }
    }

    private synchronized void processFrame(BufferedImage newFrame) {
        try {
            // 帧差异检测（简单像素比对）
//            if (previousFrame != null && framesIdentical(previousFrame, newFrame)) {
//                return; // 跳过相同帧
//            }

            // 智能压缩决策
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 根据内容类型选择压缩方式
            if (isTextDominant(newFrame)) {
                // 文本内容使用PNG
                ImageIO.write(scaleImage(newFrame, Myclarity), "PNG", baos);
            } else {
                ImageIO.write(scaleImage(newFrame, Myclarity), "PNG", baos);
//                // 图像内容使用WEBP
//                ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
//                WebPWriteParam writeParam = new WebPWriteParam(Locale.getDefault());
//                writeParam.setCompressionType(WebPWriteParam.LOSSY_COMPRESSION);
//                writeParam.setCompressionQuality(0.75f);
//
//                try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
//                    writer.setOutput(ios);
//                    writer.write(null, new IIOImage(scaleImage(newFrame, 0.7f), null, null), writeParam);
//                }
            }

            lastFrame = baos.toByteArray();
            previousFrame = newFrame;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object getScreenshot(Request request, Response response) {
        response.type("image/png");
        if (lastFrame == null) {
            response.status(503);
            return "Not ready";
        }
        return lastFrame;
    }
    private String html(Request request, Response response) {
        response.type("text/html");
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Screen Monitor</title>\n" +
                "    <style>\n" +
                "        * {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "\n" +
                "        body {\n" +
                "            background-color: #1a1a1a;\n" +
                "            height: 100vh;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "\n" +
                "        .container {\n" +
                "            position: relative;\n" +
                "            width: 95vw;\n" +
                "            height: 95vh;\n" +
                "            background-color: #000;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);\n" +
                "        }\n" +
                "\n" +
                "        #screenView {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            object-fit: contain;\n" +
                "            image-rendering: crisp-edges;\n" +
                "        }\n" +
                "\n" +
                "        .status-bar {\n" +
                "            position: absolute;\n" +
                "            bottom: 0;\n" +
                "            left: 0;\n" +
                "            right: 0;\n" +
                "            background: rgba(0, 0, 0, 0.7);\n" +
                "            color: #fff;\n" +
                "            padding: 8px 16px;\n" +
                "            display: flex;\n" +
                "            justify-content: space-between;\n" +
                "            font-family: monospace;\n" +
                "        }\n" +
                "\n" +
                "        .loading {\n" +
                "            position: absolute;\n" +
                "            top: 50%;\n" +
                "            left: 50%;\n" +
                "            transform: translate(-50%, -50%);\n" +
                "            color: #fff;\n" +
                "            font-size: 1.5em;\n" +
                "            display: none;\n" +
                "        }\n" +
                "\n" +
                "        @media (max-width: 768px) {\n" +
                "            .container {\n" +
                "                width: 100vw;\n" +
                "                height: 100vh;\n" +
                "                border-radius: 0;\n" +
                "            }\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "    <div class=\"loading\" id=\"loading\"></div>\n" +
                "    <img id=\"screenView\" alt=\"Remote Screen\">\n" +
                "    <div class=\"status-bar\">\n" +
                "        <span id=\"timestamp\">Connected: -</span>\n" +
                "        <span id=\"resolution\">Resolution: -</span>\n" +
                "        <span id=\"fps\">FPS: -</span>\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                "<script>\n" +
                "    const screenView = document.getElementById('screenView');\n" +
                "    const loading = document.getElementById('loading');\n" +
                "    const timestamp = document.getElementById('timestamp');\n" +
                "    const resolution = document.getElementById('resolution');\n" +
                "    const fps = document.getElementById('fps');\n" +
                "\n" +
                "    let frameCount = 0;\n" +
                "    let lastUpdate = Date.now();\n" +
                "    let isFetching = false;\n" +
                "\n" +
                "    // 动态更新统计信息\n" +
                "    function updateStats() {\n" +
                "        const now = Date.now();\n" +
                "        const elapsed = (now - lastUpdate) / 1000;\n" +
                "        const currentFPS = Math.round(frameCount / elapsed);\n" +
                "\n" +
                "        fps.textContent = `FPS: ${currentFPS}`;\n" +
                "        frameCount = 0;\n" +
                "        lastUpdate = now;\n" +
                "    }\n" +
                "\n" +
                "    // 获取屏幕截图\n" +
                "    async function fetchScreen() {\n" +
                "        if (isFetching) return;\n" +
                "\n" +
                "        try {\n" +
                "            isFetching = true;\n" +
                "            loading.style.display = 'block';\n" +
                "\n" +
                "            const response = await fetch('http://192.168.0.209:8080/screenshot?' + Date.now());\n" +
                "            if (!response.ok) throw new Error(response.statusText);\n" +
                "\n" +
                "            const blob = await response.blob();\n" +
                "            const url = URL.createObjectURL(blob);\n" +
                "\n" +
                "            screenView.onload = () => {\n" +
                "                URL.revokeObjectURL(url);\n" +
                "                loading.style.display = 'none';\n" +
                "                frameCount++;\n" +
                "                resolution.textContent = `Resolution: ${screenView.naturalWidth}x${screenView.naturalHeight}`;\n" +
                "                timestamp.textContent = `Connected: ${new Date().toLocaleTimeString()}`;\n" +
                "            };\n" +
                "\n" +
                "            screenView.src = url;\n" +
                "        } catch (error) {\n" +
                "            console.error('Fetch error:', error);\n" +
                "            loading.textContent = 'Connection Error';\n" +
                "        } finally {\n" +
                "            isFetching = false;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    // 自动调整请求频率\n" +
                "    let interval = 50;\n" +
                "    function adjustInterval() {\n" +
                "        const now = Date.now();\n" +
                "        const actualInterval = now - lastUpdate;\n" +
                "        interval = Math.min(2000, Math.max(500, actualInterval * 0.8));\n" +
                "    }\n" +
                "\n" +
                "    // 修改请求函数，移除请求间隔调整\n" +
                "    function startMonitoring() {\n" +
                "        // 设置请求间隔为 50 毫秒（每秒 20 次请求）\n" +
                "        const requestInterval = 50;\n" +
                "        // 启动定时器，每秒请求 20 次\n" +
                "        setInterval(fetchScreen, requestInterval);\n" +
                "        // 使用 requestAnimationFrame 连续请求\n" +
                "        // function fetchLoop() {\n" +
                "        //     fetchScreen();\n" +
                "        //     requestAnimationFrame(fetchLoop);\n" +
                "        // }\n" +
                "        // fetchLoop();\n" +
                "\n" +
                "        // 统计信息更新保持不变\n" +
                "        setInterval(updateStats, 1000);\n" +
                "    }\n" +
                "\n" +
                "    // 页面加载完成后启动\n" +
                "    window.addEventListener('load', startMonitoring);\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }

    // 辅助方法
    private BufferedImage scaleImage(BufferedImage original, float scale) {
        int newWidth = (int)(original.getWidth() * scale);
        int newHeight = (int)(original.getHeight() * scale);
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaled;
    }

    private boolean framesIdentical(BufferedImage img1, BufferedImage img2) {
        // 简单差异检测（可优化为区域检测）
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
            return false;

        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y))
                    return false;
            }
        }
        return true;
    }

    private boolean isTextDominant(BufferedImage image) {
        // 简单文本检测（基于颜色变化频率）
        // 实际可替换为更复杂的图像分析
        return image.getWidth() > 1280; // 示例条件
    }

    private String getContentType(Request request) {
        String format = request.queryParams("format");
        if ("webp".equalsIgnoreCase(format)) return "image/webp";
        if ("jpg".equalsIgnoreCase(format)) return "image/jpeg";
        return "image/png";
    }

//    public static void main(String[] args) {
//        new HttpRDP3();
//    }
}