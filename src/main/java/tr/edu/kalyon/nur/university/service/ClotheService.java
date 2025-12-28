package tr.edu.kalyon.nur.university.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tr.edu.kalyon.nur.university.controller.dto.ClotheCandidateDto;
import tr.edu.kalyon.nur.university.model.ClipResponse;
import tr.edu.kalyon.nur.university.service.ai.AiService;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;
import tr.edu.kalyon.nur.university.model.ImagePart;
import tr.edu.kalyon.nur.university.model.User;
import tr.edu.kalyon.nur.university.storage.ClotheRepository;

import java.util.List;

@Service
public class ClotheService {
    private final ClotheRepository clotheRepository;
    private final AiService aiService;
    private final OutfitService outfitService;

    public ClotheService(ClotheRepository clotheRepository, AiService aiService, OutfitService outfitService){
        this.clotheRepository=clotheRepository;
        this.aiService = aiService;
        this.outfitService = outfitService;
    }


    public List<ClotheCandidateDto> listMyClothes(Long userId) {

        List<Clothe> clothes = clotheRepository.findByUserId(userId);

        return clothes.stream()
                .map(c -> new ClotheCandidateDto(
                        c.getId(),
                        c.getCategory(),
                        c.getColor(),
                        c.getStyle(),
                        c.getSeasons() == null ? List.of() : c.getSeasons()
                ))
                .toList();
    }


    @Transactional
    public void addClothe(MultipartFile file, Long userId) throws Exception {
        User user = new User();
        user.setId(userId);

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

    @Transactional(readOnly = true)
    public List<Clothe> getOwnedClothesByIds(Long userId, List<Long> ids) {
        return clotheRepository.findByUserIdAndIdIn(userId, ids);
    }

    public List<ClotheCandidateDto> listMyClothesByType(Long userId, String type) {
        List<Clothe> clothes = clotheRepository.findByUserIdAndCategoryIgnoreCase(userId, type);

        return clothes.stream()
                .map(c -> new ClotheCandidateDto(
                        c.getId(),
                        c.getCategory(),
                        c.getColor(),
                        c.getStyle(),
                        c.getSeasons() == null ? List.of() : c.getSeasons()
                ))
                .toList();
    }
}
