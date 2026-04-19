package com.security.demo.controller;

import com.security.demo.DBmodel.User;
import com.security.demo.model.UploadData;
import com.security.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class Usercontroller {

    @Autowired
    UserService userService;
    @GetMapping({"/profile/{id}", "/profile"})
    public User getuser(@AuthenticationPrincipal com.security.demo.config.User user,@PathVariable(value = "id" ,required = false) Integer acitivty){
        if(acitivty == null) {
            User user1=  userService.fetchuser(user.getUsername());
            user1.setLogs(null);
            return user1;
        }else {
            User user1 =  userService.fetchuser(user.getUsername());
            return user1;
        }


    }
    @PostMapping("/upload")
    public User upload(@AuthenticationPrincipal com.security.demo.config.User user , @RequestBody UploadData data){
      return userService.modifyUser(user.getUsername() , data);

    }
}
