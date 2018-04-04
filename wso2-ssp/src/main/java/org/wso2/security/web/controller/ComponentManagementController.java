/*
 *
 *  *  Copyright (c) ${YEAR} WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *  WSO2 Inc. licenses this file to you under the Apache License,
 *  *  Version 2.0 (the "License"); you may not use this file except
 *  *  in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.security.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.web.entity.Component;
import org.wso2.security.web.entity.User;
import org.wso2.security.web.model.UploadedFile;
import org.wso2.security.web.service.ComponentService;
import org.wso2.security.web.service.FileValidator;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/*
* Component Management class for the WSO2 SSP
* This class carries all the operations related to the Components
*
**/

@Controller
@EnableAutoConfiguration
@RequestMapping("component/")
public class ComponentManagementController {

    @Autowired
    private ComponentService componentService;

    @Autowired
    private FileValidator fileValidator;

    private static Log log = LogFactory.getLog(ComponentManagementController.class);


    private int fileCount = 1;

    @RequestMapping(value = "add-component-details/",method = RequestMethod.POST)
    @ResponseBody
    public String addComponentDetails(Model model, @RequestParam String username
            , @RequestParam String email, @RequestParam String team, @RequestParam String componentname) {


        User user = new User();
        user.setTeam(team);


        Component component = new Component();
        component.setDeveloper(username);
        component.setDeveloperEmail(email);
        component.setComponantName(componentname);

        componentService.save(component);

        return "success";
    }

    @RequestMapping("upload-binaryfile")
    public String fileUploaded(
            @ModelAttribute("uploadedFile") UploadedFile uploadedFile,
            BindingResult result, Model model, HttpServletRequest request) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        MultipartFile file = uploadedFile.getFile();
        fileValidator.validate(uploadedFile, result);

        String fileName = file.getOriginalFilename();

        if (result.hasErrors()) {
            model.addAttribute("status", "failed");
            return "marketing/AddToMarketing";
        }

        try {
            inputStream = file.getInputStream();

            fileName = String.valueOf(fileCount++) + ":" + fileName;
            //String realPath=request.getSession().getServletContext().getRealPath("/");

            File newFile = new File(fileName);

            //set application user permissions to 455
            newFile.setExecutable(false);
            newFile.setReadable(true);
            newFile.setWritable(true);

            //change permission to 777 for all the users
            //no option for group and others
            newFile.setExecutable(true, false);
            newFile.setReadable(true, false);
            newFile.setWritable(true, false);

            //using PosixFilePermission to set file permissions 777
            Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
            //add owners permission
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            //add group permissions
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            //add others permissions
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

            System.out.println("ssssssss " + newFile.getAbsoluteFile().getParent().toString());

            Files.setPosixFilePermissions(Paths.get(newFile.getAbsoluteFile().getParent().toString()), perms);

            if (!newFile.exists()) {

                if (!newFile.createNewFile()) {
                    System.out.println("failed");
                    model.addAttribute("status", "failed");
                    return "";

                }
            }
            outputStream = new FileOutputStream(newFile);
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        model.addAttribute("status", "success");

        return "done";
    }
}
