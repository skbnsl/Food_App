package com.tastenfood.FoodApp.menu.services;

import com.tastenfood.FoodApp.aws.AWSS3Service;
import com.tastenfood.FoodApp.category.entity.Category;
import com.tastenfood.FoodApp.category.repository.CategoryRepository;
import com.tastenfood.FoodApp.exceptions.BadRequestException;
import com.tastenfood.FoodApp.exceptions.NotFoundException;
import com.tastenfood.FoodApp.menu.dtos.MenuDTO;
import com.tastenfood.FoodApp.menu.entity.Menu;
import com.tastenfood.FoodApp.menu.repository.MenuRepository;
import com.tastenfood.FoodApp.response.Response;
import com.tastenfood.FoodApp.review.dtos.ReviewDTO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService{

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final AWSS3Service awss3Service;

    @Override
    public Response<MenuDTO> createMenu(MenuDTO menuDTO) {
        log.info("Inside createMenu(): {}",menuDTO.toString());
        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category Not Found with id:"+ menuDTO.getCategoryId()));
        String imageUrl = null;
        MultipartFile imageFile = menuDTO.getImageFile();
        if(imageFile==null || imageFile.isEmpty()){
            throw new BadRequestException("Menu image is required!");
        }
        String imageName = UUID.randomUUID() + "_" +imageFile.getOriginalFilename();
        URL s3Url = awss3Service.uploadFile("/menus"+imageName, imageFile);
        imageUrl = s3Url.toString();

        Menu menu = Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .price(menuDTO.getPrice())
                .imageUrl(imageUrl)
                .category(category)
                .build();

        Menu savedMenu = menuRepository.save(menu);

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu added successfully!")
                .data(modelMapper.map(savedMenu, MenuDTO.class))
                .build();
    }

    @Override
    public Response<MenuDTO> updateMenu(MenuDTO menuDTO) {
        log.info("Inside updateMenu(): {}",menuDTO.toString());
        Menu existingMenu = menuRepository.findById(menuDTO.getId())
                .orElseThrow(() -> new NotFoundException("Menu not found!"));

        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category Not Found with id:"+ menuDTO.getCategoryId()));

        String imageUrl = existingMenu.getImageUrl();
        MultipartFile imageFile = menuDTO.getImageFile();

        //check if new image file provided
        if(imageFile!=null && !imageFile.isEmpty()){
            //delete old image in cloud if exists
            if(imageUrl!=null && !imageUrl.isEmpty()){
                String keyName = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
                awss3Service.deleteFile("menus/"+keyName);
                log.info("Deleted old menu image from s3: {}", keyName);
            }
            //upload new image
            String imageName = UUID.randomUUID().toString() + "_" +imageFile.getOriginalFilename();
            URL newImageUrl = awss3Service.uploadFile("menus/"+imageName,imageFile);
            imageUrl = newImageUrl.toString();
        }

        if(menuDTO.getName() != null && !menuDTO.getName().isBlank()) existingMenu.setName(menuDTO.getName());
        if(menuDTO.getDescription() != null && !menuDTO.getDescription().isBlank()) existingMenu.setDescription(menuDTO.getDescription());
        if(menuDTO.getPrice() != null) existingMenu.setPrice(menuDTO.getPrice());

        existingMenu.setImageUrl(imageUrl);
        existingMenu.setCategory(category);

        Menu updatedMenu = menuRepository.save(existingMenu);

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu updated successfully!")
                .data(modelMapper.map(updatedMenu, MenuDTO.class))
                .build();
    }

    @Override
    public Response<MenuDTO> getMenuById(Long id) {
        log.info("Inside getMenuById(): {}",id.toString());
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found!"));

        MenuDTO menuDTO = modelMapper.map(existingMenu, MenuDTO.class);
        //sort the reviews in descending order
        if(menuDTO.getReviews() != null){
            menuDTO.getReviews().sort(Comparator.comparing(ReviewDTO::getId).reversed());
        }

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu Retrieved Successfully!")
                .data(menuDTO)
                .build();
    }

    @Override
    public Response<?> deleteMenu(Long id) {
        log.info("Inside deleteMenu(): {}",id.toString());
        Menu menuToDelete = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found!"));
        //Delete image from s3 if exist
        String imageUrl = menuToDelete.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()){
            String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            awss3Service.deleteFile("menus/"+keyName);
            log.info("Deleted Menu Image from aws: {}",keyName);
        }
        menuRepository.deleteById(id);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu deleted Successfully!")
                .build();
    }

    @Override
    public Response<List<MenuDTO>> getMenus(Long categoryId, String search) {
        log.info("Inside getMenus(): categoryId: {} ,search {}",categoryId.toString(), search);
        Specification<Menu> spec = buildSpecification(categoryId,search);

        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        List<Menu> menuList = menuRepository.findAll(spec, sort);
        List<MenuDTO> menuDTOS = menuList.stream()
                .map(menu -> modelMapper.map(menu, MenuDTO.class))
                .toList();

        return Response.<List<MenuDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menus Retrieved")
                .data(menuDTOS)
                .build();

    }

    private Specification<Menu> buildSpecification(Long categoryId, String search) {
        return (root, query, cb) -> {
            //list to accumulate all where conditions
            List<Predicate> predicates = new ArrayList<>();
            if(categoryId!=null){
                predicates.add(cb.equal(
                        root.get("category").get("id"),
                        categoryId
                ));
            }
            if(search != null && !search.isBlank()){
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(
                                cb.lower(root.get("name")),
                                searchTerm
                        ),
                        cb.like(
                                cb.lower(root.get("description")),
                                searchTerm
                        )
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}