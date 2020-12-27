package com.kubernetes;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by mengxiangli on 2020-12-26.
 */
@RequestMapping("file")
@RestController
public class FileController {

    private static Map<String, Integer> container = new HashMap<>();
    private static String basePath = "D:/filedemo";

    static {
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/apic29767.jpg", 1);
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/hpic3382.jpg", 1);
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/apic29769.jpg", 2);
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/apic29770.jpg", 2);
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/apic29771.jpg", 2);
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/apic29768.jpg", 3);
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/apic29766.jpg", 3);
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/apic29767.jpg", 4);
        container.put("https://scpic.chinaz.net/files/pic/pic9/202012/apic29764.jpg", 5);
    }


    @GetMapping("/download")
    public void download(HttpServletResponse response) {
        Set<Map.Entry<String, Integer>> entries = container.entrySet();
        for (Map.Entry<String, Integer> entry : entries
                ) {
            if (entry.getValue() == 1) {
                localDownload(entry.getKey(), basePath + "/目录root");
            } else if (entry.getValue() == 2) {
                localDownload(entry.getKey(), basePath + "/目录root/目录1");
            } else if (entry.getValue() == 3) {
                localDownload(entry.getKey(), basePath + "/目录root/目录1/目录2");
            } else if (entry.getValue() == 4) {
                localDownload(entry.getKey(), basePath + "/目录root/目录1/目录2/目录3");
            } else {
                localDownload(entry.getKey(), basePath + "/目录root/目录1/目录2/目录3/目录4");
            }
        }

        //压缩输出
        zip(basePath + "/目录root", response);

    }

    public static void zip(String sourceFileName, HttpServletResponse response) {
        ZipOutputStream out = null;
        BufferedOutputStream bos = null;
        try {
            //将zip以流的形式输出到前台
            response.setHeader("content-type", "application/octet-stream");
            response.setCharacterEncoding("utf-8");
            // 设置浏览器响应头对应的Content-disposition
            //参数中 testZip 为压缩包文件名，尾部的.zip 为文件后缀
            response.setHeader("Content-disposition",
                    "attachment;filename=" + new String("testZip".getBytes("gbk"), "iso8859-1") + ".zip");
            //创建zip输出流
            out = new ZipOutputStream(response.getOutputStream());
            //创建缓冲输出流
            bos = new BufferedOutputStream(out);
            File sourceFile = new File(sourceFileName);
            //调用压缩函数
            compress(out, bos, sourceFile, sourceFile.getName());
            out.flush();
            System.out.println("压缩完成");
        } catch (Exception e) {
            System.out.println("ZIP压缩异常：" + e.getMessage());
        } finally {
            try {
                bos.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void compress(ZipOutputStream out, BufferedOutputStream bos, File sourceFile, String base) {
        FileInputStream fos = null;
        BufferedInputStream bis = null;
        try {
            //如果路径为目录（文件夹）
            if (sourceFile.isDirectory()) {
                //取出文件夹中的文件（或子文件夹）
                File[] flist = sourceFile.listFiles();
                if (flist.length == 0) {//如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
                    out.putNextEntry(new ZipEntry(base + "/"));
                } else {//如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
                    for (int i = 0; i < flist.length; i++) {
                        compress(out, bos, flist[i], base + "/" + flist[i].getName());
                    }
                }
            } else {//如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
                out.putNextEntry(new ZipEntry(base));
                fos = new FileInputStream(sourceFile);
                bis = new BufferedInputStream(fos);

                int tag;
                //将源文件写入到zip文件中
                while ((tag = bis.read()) != -1) {
                    out.write(tag);
                }

                bis.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null)
                    bis.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static File localDownload(String url, String filePath) {
        String fileName = url.substring(url.lastIndexOf("/"));
        FileOutputStream fileOut = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        BufferedOutputStream bos = null;
        try {
            // 建立链接
            URL httpUrl = new URL(url);
            conn = (HttpURLConnection) httpUrl.openConnection();
            //连接指定的资源
            conn.connect();
            //获取网络输入流
            inputStream = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            //判断文件的保存路径后面是否以/结尾
            if (!filePath.endsWith("/")) {
                filePath += "/";
            }
            //如果目录不存在则直接创建
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            //写入到文件（注意文件保存路径的后面一定要加上文件的名称）
            fileOut = new FileOutputStream(filePath + fileName);
            bos = new BufferedOutputStream(fileOut);

            byte[] buf = new byte[4096];
            int length = bis.read(buf);
            //保存文件
            while (length != -1) {
                bos.write(buf, 0, length);
                length = bis.read(buf);
            }
            bos.close();
            bis.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("抛出异常！！");
        } finally {
            try {
                fileOut.close();
                inputStream.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new File(filePath + fileName);
    }

    public static void main(String[] args) {
        Set<Map.Entry<String, Integer>> entries = container.entrySet();
        for (Map.Entry<String, Integer> entry : entries
                ) {
            if (entry.getValue() == 1) {
                localDownload(entry.getKey(), basePath + "/目录root");
            } else if (entry.getValue() == 2) {
                localDownload(entry.getKey(), basePath + "/目录root/目录1");
            } else if (entry.getValue() == 3) {
                localDownload(entry.getKey(), basePath + "/目录root/目录1/目录2");
            } else if (entry.getValue() == 4) {
                localDownload(entry.getKey(), basePath + "/目录root/目录1/目录2/目录3");
            } else {
                localDownload(entry.getKey(), basePath + "/目录root/目录1/目录2/目录3/目录4");
            }
        }
    }
}
