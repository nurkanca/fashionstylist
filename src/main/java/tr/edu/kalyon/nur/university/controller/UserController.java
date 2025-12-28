package tr.edu.kalyon.nur.university.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.service.UserService;

@RestController
@RequestMapping("/api/user")

public class UserController {
    private UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/create")
    public void post(@RequestParam String name, String gender) {
        userService.addUser(name,gender);
        System.out.println("User is added");
    }
}
