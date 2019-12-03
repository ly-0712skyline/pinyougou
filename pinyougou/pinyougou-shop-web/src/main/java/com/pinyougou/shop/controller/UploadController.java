package com.pinyougou.shop.controller;

import com.pinyougou.common.util.FastDFSClient;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/upload")
@RestController
public class UploadController {

    /**
     * 接收图片文件并保存返回操作结果
     * @param file 图片文件
     * @return 操作结果（成功则返回地址，失败则提示信息）
     */
    @PostMapping
    public Result uploadFile(MultipartFile file){
        Result result = Result.fail("上传文件失败");

        try {
            //上传图片文件
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastdfs/tracker.conf");

            //文件扩展名（后缀）
            String file_ext_name = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);

            String url = fastDFSClient.uploadFile(file.getBytes(), file_ext_name);

            result = Result.ok(url);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
