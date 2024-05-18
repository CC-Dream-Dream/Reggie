package com.cc.controller;


import com.cc.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    //拿到yml里保存的文件路径
    @Value("${takeOutFile.fileLocaltion}")
    private String fileLoaction;


    /**
     * 上传图片
     * @param file 文件名称，和前台传来的文件名称是一致的
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upLoadFile(MultipartFile file){
        //这里的file只是一个临时的文件存储，临时存储到某一个位置，然后待接收完毕后再转存到目标位置上，然后再把这个临时文件删除
        //截取文件后缀
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        //生成UUID
        String randomUUID = UUID.randomUUID().toString();
        //拼接文件最后名称，结果为文件本体名字+UUID+后缀
        String fileName = file.getOriginalFilename() + randomUUID + suffix;

        //保证存储的位置有这个文件夹
        File dir = new File(fileLoaction);
        if (!dir.exists()) {
            //目标存储位置不存在，就创建一个文件夹
            dir.mkdirs();
        }

        try {
            //转存文件到指定位置+文件的名称全拼
            file.transferTo(new File(fileLoaction+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //把文件的名字上传回去，方便后续回显读取路径
        return Result.success(fileName);
    }


    /**
     * 文件回显接口
     * @param httpServletResponse 响应对象
     * @param name 上传的文件名称
     * @throws IOException IO异常
     */
    @GetMapping("/download")
    public void fileDownload(HttpServletResponse httpServletResponse,String name) throws IOException {
        //把刚刚存的文件读取到内存中，准备回显
        FileInputStream fileInputStream = new FileInputStream(new File(fileLoaction+name));

        //把读取到内存中的图片用输出流写入Servlet响应对象里
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();

        //可选项，选择响应类型
        httpServletResponse.setContentType("image/jpeg");

        //用byte数组写入，注意是小写b，不是大写，大写就是包装类了
        byte[] fileArray = new byte[1024];
        int length=0;
        try {
            //只要没读到数组的尾部就一直读下去，这部分是IO的内容
            while ((length=fileInputStream.read(fileArray))!=-1) {
                //写入响应流，从0开始，写入到数组末尾长度
                servletOutputStream.write(fileArray, 0, length);
                //把流里的东西挤出来
                servletOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关闭流
            fileInputStream.close();
            servletOutputStream.close();
        }
        return;
    }


}
