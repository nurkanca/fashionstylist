package tr.edu.kalyon.nur.university.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tr.edu.kalyon.nur.university.model.User;
import tr.edu.kalyon.nur.university.service.OutfitService;
import tr.edu.kalyon.nur.university.service.UserService;

@RestController
@RequestMapping("/api/outfit")

public class OutfitController {
    private UserService userService;
    private OutfitService outfitService;

    public OutfitController(UserService userService, OutfitService outfitService){
        this.userService=userService;
        this.outfitService=outfitService;
    }

}
