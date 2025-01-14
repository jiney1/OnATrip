package com.naver.OnATrip.service;

import com.naver.OnATrip.entity.Location;
import com.naver.OnATrip.repository.admin.LocationRepository;
import com.naver.OnATrip.web.dto.location.LocationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;
    private static final String UPLOAD_DIR = "src/main/resources/static/images/location";

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional
    public void addLocation(LocationDTO locationDTO) throws IOException {
        // 파일 유효성 검사
        MultipartFile file = locationDTO.getFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 업로드해야 합니다.");
        }

        // 이미지 저장 경로 설정
        String imagePath = saveFile(file); // 이미지 파일 저장 메서드 호출

        // DTO를 엔티티로 변환
        Location location = new Location();
        location.setCountryName(locationDTO.getCountryName());
        location.setCountryCode(locationDTO.getCountryCode());
        location.setCity(locationDTO.getCity());
        location.setDescription(locationDTO.getDescription());
        location.setLocationType(locationDTO.getLocationType());
        location.setImage(imagePath);

        // 여행지 저장
        locationRepository.save(location);
    }


    // 도시 중복 검사
    public boolean existsCity(String cityName) {
        return locationRepository.existsByCity(cityName);
    }

    // 국가 중복 검사
    public boolean existsbyName(String countryName) {
        return locationRepository.existsByCountryName(countryName);
    }

    // 여행지 목록
    public List<LocationDTO> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        List<LocationDTO> locationDTOs = new ArrayList<>();

        for (Location location : locations) {

            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setId(location.getId());
            locationDTO.setCountryName(location.getCountryName());
            locationDTO.setCountryCode(location.getCountryCode());
            locationDTO.setCreatedDate(location.getCreatedDate());
            locationDTO.setEndDate(location.getEndDate());
            locationDTO.setCity(location.getCity());
            locationDTO.setDescription(location.getDescription());
            locationDTO.setLocationType(location.getLocationType());
            locationDTO.setImagePath(location.getImage());
            locationDTOs.add(locationDTO);

/*
            logger.info("LocationDTO: {}==================", locationDTO); // 로그로 데이터 확인
*/

        }


        return locationDTOs;
    }

    // 여행지 수정
    @Transactional
    public void updateLocationWithImage(LocationDTO locationDTO) throws IOException {
        MultipartFile file = locationDTO.getFile();
        if (file != null && !file.isEmpty()) {
            String imagePath = saveFile(file);
            updateLocationWithNewImage(locationDTO, imagePath);
        }
    }

    // 여행지 수정 - 이미지가 없는 경우
    @Transactional
    public void updateLocationWithoutImage(LocationDTO locationDTO) {
        Location existingLocation = locationRepository.findById(locationDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 여행지가 존재하지 않습니다."));

        existingLocation.setCountryName(locationDTO.getCountryName());
        existingLocation.setCountryCode(locationDTO.getCountryCode());
        existingLocation.setCity(locationDTO.getCity());
        existingLocation.setDescription(locationDTO.getDescription());
        existingLocation.setLocationType(locationDTO.getLocationType());
        // 날짜는 엔티티의 @PrePersist 메서드에서 설정되므로, 여기서는 설정하지 않음

        locationRepository.save(existingLocation);
    }

    private void updateLocationWithNewImage(LocationDTO locationDTO, String imagePath) {
        Location location = locationRepository.findById(locationDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 여행지가 존재하지 않습니다."));

        location.setCountryName(locationDTO.getCountryName());
        location.setCountryCode(locationDTO.getCountryCode());
        location.setCity(locationDTO.getCity());
        location.setDescription(locationDTO.getDescription());
        location.setLocationType(locationDTO.getLocationType());

        // 이미지 경로 설정
        if (imagePath != null) {
            location.setImage(imagePath);
        } else {
            // 이미지가 업로드되지 않은 경우 - 기존 이미지 경로를 유지
            location.setImage(locationDTO.getImagePath());
        }

        locationRepository.save(location);
    }


    // 이미지 파일 저장
    private String saveFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        return "/images/location/" + fileName;
    }

    // 여행지 ID로 조회
    public LocationDTO getLocationById(long id) {
        Optional<Location> locationOptional = locationRepository.findById(id);
        if (locationOptional.isPresent()) {
            Location location = locationOptional.get();
            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setId(location.getId());
            locationDTO.setCreatedDate(location.getCreatedDate());
            locationDTO.setEndDate(location.getEndDate());
            locationDTO.setCountryName(location.getCountryName());
            locationDTO.setCountryCode(location.getCountryCode());
            locationDTO.setCity(location.getCity());
            locationDTO.setDescription(location.getDescription());
            locationDTO.setLocationType(location.getLocationType());
            locationDTO.setImagePath(location.getImage());
            return locationDTO;
        } else {
            throw new IllegalArgumentException("해당 ID의 여행지가 존재하지 않습니다.");
        }
    }

    public boolean deleteLocation(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            return true;
        }
        return false;
    }


    // 이미지 파일 삭제
    public boolean deleteImage(String imagePath) {
        File imageFile = new File("src/main/resources/static" + imagePath);
        if (imageFile.exists()) {
            try {
                boolean deleted = imageFile.delete();
                if (deleted) {
                    return true;
                } else {
                    // 실패한 경우에 대한 처리 로직 추가
                    return false;
                }
            } catch (SecurityException e) {
                // 예외 발생 시 처리 로직 추가
                return false;
            }
        } else {
            // 파일이 존재하지 않는 경우 처리 로직 추가
            return false;
        }
    }

    // 국내 여행지 목록 조회
    public List<LocationDTO> getDomesticLocations() {
        List<Location> domesticLocations = locationRepository.findByLocationType("domestic");
        return domesticLocations.stream().map(LocationDTO::new).collect(Collectors.toList());
    }

    // 해외 여행지 목록 조회
    public List<LocationDTO> getOverseasLocations() {
        List<Location> overseasLocations = locationRepository.findByLocationType("overseas");
        return overseasLocations.stream().map(LocationDTO::new).collect(Collectors.toList());
    }


}