package com.ahui.htmlanalysis;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Scanner;

/**
 * @Author: ahui
 * @Description: 下载工具类
 * @DateTime: 2022/3/22 - 10:54
 **/
public class GetUrlPicture {

    public static void main(String[] args) {
        // 未下载 https://asiansister.com/view_1108__Patron__Fantasy_Factory__34Pn
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入请求网址: ");
        String url =scanner.next();
        System.out.println("!!!注意!!! 路径必须以/结尾");
        System.out.print("请输入保存路径: ");
        String folderPath = scanner.next();
        downloadImage(url, folderPath);
    }


    /**
     * 下载图片
     * @param requestUrl 套图主页面的请求路径
     * @param writeFolderPath 要写入的文件夹路径 路径格式一定要以/结尾传入
     */
    @SneakyThrows
    public static void downloadImage(String requestUrl, String writeFolderPath) {
        //发起请求获取网站内容并解析HTML
        Document document = Jsoup.connect(requestUrl).get();
        //获取套图标题用来创建文件夹
        String title = document.title();
        //通过获取<div class="rootContant">的第二个元素来获取他的子标签
        Element rootContantElement = document.getElementsByClass("rootContant").get(1);
        //获取全部子标签
        Elements allElements = rootContantElement.getAllElements();
        //拼接 文件夹路径+套图名称 格式： /path/name/
        String splicingUrl = writeFolderPath+title+"/";
        //检查并创建文件夹
        FileUtil.mkdir(splicingUrl);
        //获取子标签内属性为dataurl的值
        for (Element element : allElements) {
            if(!"".equals(element.attr("data-src")) && !" ".equals(element.attr("data-src"))){
                //去除略缩图的_t
                String removeThumbnail = element.attr("data-src").replace("_t.jpg",".jpg");
                //拼接
                String url = StrUtil.format("https://asiansister.com/{}", removeThumbnail);
                //文件下载
                try {
                    HttpUtil.downloadFile(url, FileUtil.file(splicingUrl), new StreamProgress() {
                        @Override
                        public void start() {
                            Console.log("开始下载...");
                        }
                        @Override
                        public void progress(long progressSize) {
                            Console.log("已下载：{}", FileUtil.readableFileSize(progressSize));
                        }
                        @Override
                        public void finish() {
                            Console.log("下载结束...");
                        }
                    });
                }catch (HttpException httpException){
                    removeThumbnail = element.attr("data-src").replace("_t.jpg",".png");
                    url = StrUtil.format("https://asiansister.com/{}", removeThumbnail);
                    HttpUtil.downloadFile(url, FileUtil.file(splicingUrl));
                }
            }
        }
    }
}
