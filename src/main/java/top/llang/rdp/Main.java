package top.llang.rdp;

import top.llang.rdp.http.HttpRDP3;

public class Main {
    public static void main(String[] args) {
        String host = "localhost"; // 默认主机
        int port = 8080; // 默认端口
        float clarity = 0.5F; // 默认清晰度

        // 解析命令行参数
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    if (i + 1 < args.length) {
                        host = args[i + 1];
                        i++; // 跳过下一个参数，因为它已经被用作主机名
                    } else {
                        System.out.println("请提供主机名");
                        return;
                    }
                    break;
                case "-p":
                    if (i + 1 < args.length) {
                        try {
                            port = Integer.parseInt(args[i + 1]);
                            i++; // 跳过下一个参数，因为它已经被用作端口号
                        } catch (NumberFormatException e) {
                            System.out.println("端口号必须是整数");
                            return;
                        }
                    } else {
                        System.out.println("请提供端口号");
                        return;
                    }
                    break;
                case "-c":
                    if (i + 1 < args.length) {
                        try {
                            clarity = (float) Double.parseDouble(args[i + 1]);
                            if (clarity < 0.1 || clarity > 0.9) {
                                System.out.println("清晰度必须在0.1到0.9之间");
                                return;
                            }
                            i++; // 跳过下一个参数，因为它已经被用作清晰度
                        } catch (NumberFormatException e) {
                            System.out.println("清晰度必须是数字");
                            return;
                        }
                    } else {
                        System.out.println("请提供清晰度");
                        return;
                    }
                    break;
                default:
                    System.out.println("未知参数: " + args[i]);
                    return;
            }
        }

        System.out.println("主机(-h): " + host);
        System.out.println("端口(-p): " + port);
        System.out.println("清晰度(-c): " + clarity);

        // 调用HttpRDP3类
        new HttpRDP3(host, port, clarity);
    }
}

//class HttpRDP3 {
//    public HttpRDP3(String host, int port, double clarity) {
//        // 在这里使用主机、端口和清晰度
//        System.out.println("HttpRDP3 初始化: " + host + ":" + port + " 清晰度: " + clarity);
//    }
//}