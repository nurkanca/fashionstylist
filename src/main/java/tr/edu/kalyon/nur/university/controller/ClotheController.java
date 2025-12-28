package tr.edu.kalyon.nur.university.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;
import tr.edu.kalyon.nur.university.service.ClotheService;

import java.util.List;

@RestController
@RequestMapping("/api/clothe")
public class ClotheController {
    private ClotheService clotheService;

    public ClotheController(ClotheService clotheService){
        this.clotheService=clotheService;
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addClothe(@RequestPart("file") MultipartFile file,
                          @RequestParam Long userId) throws Exception {
        clotheService.addClothe(file, userId);
    }

    @GetMapping
    public List<Clothe> getAllCLothes(){
        return clotheService.getALlClothes();
    }
}
