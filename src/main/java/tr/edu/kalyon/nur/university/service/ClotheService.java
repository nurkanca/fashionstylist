package tr.edu.kalyon.nur.university.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tr.edu.kalyon.nur.university.model.ClipResponse;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;
import tr.edu.kalyon.nur.university.model.ImagePart;
import tr.edu.kalyon.nur.university.model.User;
import tr.edu.kalyon.nur.university.storage.ClotheRepository;

import java.util.List;

@Service
public class ClotheService {
    private final ClotheRepository clotheRepository;
    private final UserService userService;
    private final AiService aiService;
    private final OutfitService outfitService;

    public ClotheService(ClotheRepository clotheRepository, UserService userService, AiService aiService, OutfitService outfitService){
        this.clotheRepository=clotheRepository;
        this.userService=userService;
        this.aiService = aiService;
        this.outfitService = outfitService;
    }

    @Transactional
    public void addClothe(MultipartFile file, Long userId) throws Exception {
        User user = userService.getUserById(userId);

        Clothe newClothe = new Clothe();
        newClothe.setUser(user);

        newClothe.setImageData(file.getBytes());
        newClothe.setImageFilename(file.getOriginalFilename());
        newClothe.setImageContentType(file.getContentType());

        MediaType mt = (file.getContentType() != null)
                ? MediaType.valueOf(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        ImagePart part = new ImagePart(file.getBytes(), file.getOriginalFilename(), mt);

        ClipResponse attribute = aiService.extractAttributes(part);

        newClothe.setStyle(attribute.getItemType());
        newClothe.setColor(attribute.getColor());
        newClothe.setCategory(attribute.getCategory());
        newClothe.setSeasons(attribute.getSeasons());

        clotheRepository.save(newClothe);
        outfitService.createPossibleOutfit(user, newClothe);
    }


    public List<Clothe> getALlClothes(){
        return clotheRepository.findAll();
    }


}
