package top.llang.rdp.http;//package top.llang.rdp.http;
//
//import spark.Request;
//import spark.Response;
//import spark.Spark;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static spark.Spark.*;
//
//public class HttpRDP {
//    private final AtomicReference<BufferedImage> screenImageRef = new AtomicReference<>();
//    private final Dimension screenSize;
//
//    public HttpRDP() {
////        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        screenSize = new Dimension(2560, 1600);
////        Dimension screenSize = new Dimension(2560, 1600);
//
//        Timer timer = new Timer(50, e -> captureScreen());
//        timer.start();
//
//        port(8080);
//        after((req, res) -> {
//            res.header("Access-Control-Allow-Origin", "*");
//            res.header("Content-Type", "image/png");
//        });
//        get("/screenshot", this::getScreenshot);
//    }
//
//    private void captureScreen() {
//        try {
//            Robot robot = new Robot();
//
//            Rectangle screenRect = new Rectangle(screenSize);
//            BufferedImage image = robot.createScreenCapture(screenRect);
//            screenImageRef.set(image);
//        } catch (AWTException ex) {
//            System.err.println("Error capturing screen: " + ex.getMessage());
//        }
//    }
//
//    private Object getScreenshot(Request request, Response response) {
//        BufferedImage image = screenImageRef.get();
//        if (image == null) {
//            response.status(503); // Service Unavailable
//            return "Screenshot not available";
//        }
//
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            ImageIO.write(image, "png", baos);
//            return baos.toByteArray();
//        } catch (IOException e) {
//            response.status(500);
//            return "Error generating image";
//        }
//    }
//
//    public static void main(String[] args) {
//        new HttpRDP();
//    }
//}