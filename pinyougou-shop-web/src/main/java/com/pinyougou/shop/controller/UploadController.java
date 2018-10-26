package com.pinyougou.shop.controller;


import com.pinyougou.common.util.FastDFSClient;
import com.pinyougou.vo.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @PostMapping
    public Result upload(MultipartFile file){
        Result result = Result.fail("上传图片失败");

        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastdfs/tracker.conf");

            //文件后缀
            String file_ext_name = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);

            String url = fastDFSClient.uploadFile(file.getBytes(), file_ext_name);
            if(!StringUtils.isEmpty(url)){
                result = Result.ok(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
