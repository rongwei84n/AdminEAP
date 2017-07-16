package com.cnpc.framework.base.controller;


import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.pojo.AvatarResult;
import com.cnpc.framework.base.service.UploaderService;
import com.cnpc.framework.utils.DateUtil;
import com.cnpc.framework.utils.FileUtil;
import com.cnpc.framework.utils.PropertiesUtil;
import com.cnpc.framework.base.pojo.MarkDownResult;
import org.apache.commons.fileupload.util.Streams;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/file")
public class UploaderController {

    @Resource
    private UploaderService uploaderService;


    @RequestMapping("/avatarUpload")
    @ResponseBody
    public AvatarResult avatarUpload(String userId, HttpServletRequest httpRequest, HttpSession session) throws Exception {

        MultipartHttpServletRequest request = (MultipartHttpServletRequest) httpRequest;
        Map<String, MultipartFile> fileMap = request.getFileMap();
        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") >= 0) {
            AvatarResult result = new AvatarResult();
            result.setAvatarUrls(new ArrayList());
            result.setSuccess(false);
            result.setMsg("Failure!");

            // 定义一个变量用以储存当前头像的序号
            int avatarNumber = 1;
            User user = uploaderService.get(User.class, userId);
            if (user == null) {
                user = new User();
                user.setName("new");
            }
            // 文件名
            String fileName = user.getName() + "_" + (new Date()).getTime() + ".jpg";
            String relPath = PropertiesUtil.getValue("avatarPath");
            String dirPath = request.getRealPath("/");

            String initParams = "";

            BufferedInputStream inputStream;
            BufferedOutputStream outputStream;
            for (Iterator<Map.Entry<String, MultipartFile>> it = fileMap.entrySet().iterator(); it.hasNext(); avatarNumber++) {
                File filePath = new File(dirPath + relPath);
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }
                Map.Entry<String, MultipartFile> entry = it.next();
                MultipartFile mFile = entry.getValue();
                String fieldName = entry.getKey();
                Boolean isSourcePic = fieldName.equals("__source"); // 是否是原始图片域名称
                // 文件名，如果是本地或网络图片为原始文件名（不含扩展名）、如果是摄像头拍照则为 *FromWebcam
                // String name = fileItem.getName();
                // 当前头像基于原图的初始化参数（即只有上传原图时才会发送该数据），用于修改头像时保证界面的视图跟保存头像时一致，提升用户体验度。
                // 修改头像时设置默认加载的原图url为当前原图url+该参数即可，可直接附加到原图url中储存，不影响图片呈现。
                if (fieldName.equals("__initParams")) {
                    inputStream = new BufferedInputStream(mFile.getInputStream());
                    byte[] bytes = new byte[mFile.getInputStream().available()];
                    inputStream.read(bytes);
                    initParams = new String(bytes, "UTF-8");
                    inputStream.close();
                } else if (isSourcePic || fieldName.startsWith("__avatar")) {
                    String virtualPath = dirPath + relPath + "\\" + fileName;
                    if (avatarNumber > 1) {
                        fileName = avatarNumber + fileName;
                        virtualPath = dirPath + relPath + "\\" + fileName;
                    }
                    // 原始图片(file 域的名称：__source，如果客户端定义可以上传的话，可在此处理）。
                    if (isSourcePic) {
                        fileName = "source" + fileName;
                        virtualPath = dirPath + relPath + "\\" + fileName;
                        result.setSourceUrl(relPath + "/" + fileName);
                    }
                    // 头像图片(file 域的名称：__avatar1,2,3...)。
                    else {
                        result.getAvatarUrls().add(relPath + "/" + fileName);
                    }

                    inputStream = new BufferedInputStream(mFile.getInputStream());
                    outputStream = new BufferedOutputStream(new FileOutputStream(virtualPath.replace("/", "\\")));
                    Streams.copy(inputStream, outputStream, true);
                    inputStream.close();
                    outputStream.flush();
                    outputStream.close();
                    // 保存图片信息
                    result.setMsg(uploaderService.saveAvatar(userId, fileName, relPath + File.separator + fileName, dirPath));
                }

            }
            if (result.getSourceUrl() != null) {
                result.setSourceUrl(result.getSourceUrl() + initParams);
            }
            result.setSuccess(true);
            return result;
        }
        return null;
    }

    /**
     * markdown组件上传图片
     */
    @RequestMapping(value = "/markdownUpload", method = RequestMethod.POST)
    @ResponseBody
    public MarkDownResult markdownUpload(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "editormd-image-file", required = false) MultipartFile attach) {
        try {
            String relPath = PropertiesUtil.getValue("markdownPath");
            String dirPath = request.getRealPath("/");
            //不存在目录 则创建
            File filePath = new File(dirPath + relPath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            File realFile = new File(dirPath + relPath + File.separator + attach.getOriginalFilename());
            //上传的文件已存在 则对新上传的文件重命名
            if (realFile.exists()) {
                String fileName = DateUtil.format(new Date(), "yyyyMMddHHmmss") + "_" + attach.getOriginalFilename();
                realFile = new File(dirPath+relPath+File.separator+fileName);
            }
            boolean iscreate = FileUtil.copyInputStreamToFile(attach.getInputStream(), realFile);
            if (iscreate) {
                String url=relPath + File.separator + realFile.getName();
                url=request.getAttribute("basePath").toString()+url.replaceAll("\\\\","/");
                return new MarkDownResult(1, "上传成功", url);
            }
            else {
                return new MarkDownResult(0, "上传失败", null);
            }
        } catch (IOException ex) {
            return new MarkDownResult(0, "上传失败:原因" + ex.getMessage().toString(), null);
        }
    }


}
