package org.copy.common.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.copy.common.config.BootdoConfig;
import org.copy.common.domain.FileDO;
import org.copy.common.service.FileService;
import org.copy.common.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wykj-4
 * 文件管理
 */
@Controller
@RequestMapping("/common/sysFile")
public class FileController extends BaseController {

    @Autowired
    private FileService fileService;

    @Autowired
    private BootdoConfig bootdoConfig;

    @GetMapping()
    @RequiresPermissions("common:sysFile:sysFile")
    public String sysFile(Model model){
        Map<String, Object> params = new HashMap<>(16);
        return "common/file/file";
    }

    @GetMapping("/add")
    public String add(){
        return "common/sysFile/add";
    }

    @GetMapping("/edit")
    public String edit(Long id,Model model){
        FileDO fileDO = fileService.get(id);
        model.addAttribute("sysFile",fileDO);
        return "common/sysFile/edit";
    }

    /**
     * 信息
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/info/{id}")
    @RequiresPermissions("common:info")
    public R info(@PathVariable("id") Long id) {
        FileDO sysFile = fileService.get(id);
        return R.ok().put("sysFile", sysFile);
    }

    /**
     * 保存
     * @param fileDO
     * @return
     */
    @ResponseBody
    @PostMapping("/save")
    @RequiresPermissions("common:save")
    public R save(FileDO fileDO){
        if (fileService.save(fileDO) > 0){
            return R.ok();
        }
        return R.error();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("common:update")
    public R update(@RequestBody FileDO sysFile) {
        fileService.update(sysFile);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/remove")
    @ResponseBody
    public R remove(Long id, HttpServletRequest request){
        if ("test".equals(getUsername())){
            return R.error(1, "演示系统不允许修改,完整体验请部署程序");
        }
        String fileName = bootdoConfig.getUploadPath() + fileService.get(id).getUrl().replace("/files/", "");
        if (fileService.remove(id) > 0) {
            boolean b = FileUtil.deleteFile(fileName);
            if (!b) {
                return R.error("数据库记录删除成功，文件删除失败");
            }
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 批量删除
     */
    @PostMapping("/batchRemove")
    @ResponseBody
    @RequiresPermissions("common:remove")
    public R remove(@RequestParam("ids[]") Long[] ids) {
        if ("test".equals(getUsername())) {
            return R.error(1, "演示系统不允许修改,完整体验请部署程序");
        }
        fileService.batchRemove(ids);
        return R.ok();
    }

    /**
     * 更新
     */
    @ResponseBody
    @PostMapping("/upload")
    public R upload(@RequestParam("file")MultipartFile file){
        if ("test".equals(getUsername())) {
            return R.error(1, "演示系统不允许修改,完整体验请部署程序");
        }
        String fileName = file.getOriginalFilename();
        fileName = FileUtil.renameToUUID(fileName);
        FileDO sysFile = new FileDO(FileType.fileType(fileName), "/files/" + fileName, new Date());
        try {
            FileUtil.uploadFile(file.getBytes(),bootdoConfig.getUploadPath(),fileName);
        } catch (Exception e){
            e.printStackTrace();
            return R.error();
        }

        if (fileService.save(sysFile) > 0){
            return R.ok().put("fileName",sysFile.getUrl());
        }
        return R.error();
    }

    @ResponseBody
    @GetMapping("/list")
    @RequiresPermissions("common:sysFile:sysFile")
    public PageUtils list(@RequestParam Map<String, Object> params) {
        // 查询列表数据
        Query query = new Query(params);
        List<FileDO> sysFileList = fileService.list(query);
        int total = fileService.count(query);
        PageUtils pageUtils = new PageUtils(sysFileList, total);
        return pageUtils;
    }

}