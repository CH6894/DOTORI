package com.pingu.DOTORI.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.ZoneId;
import java.util.*;

@Service
public class FileStorageService {

  @Value("${app.upload-dir:uploads}")
  private String uploadRoot;

  /** 이미지 실제 저장 + 메타 제거(재인코딩), 저장된 URL(/static/...) 리스트 반환 */
  public List<String> saveItemImages(Long itemId, List<MultipartFile> files) throws IOException {
    if (files == null || files.isEmpty()) return List.of();

    Path dir = Path.of(uploadRoot, "items", String.valueOf(itemId)).toAbsolutePath().normalize();
    Files.createDirectories(dir);

    List<String> urls = new ArrayList<>();
    for (int i = 0; i < files.size(); i++) {
      MultipartFile f = files.get(i);

      // 간단한 시그니처 검사 (jpeg/png/webp… 필요시 보강)
      String ct = f.getContentType() == null ? "" : f.getContentType().toLowerCase();
      if (!ct.startsWith("image/")) throw new IOException("이미지 파일만 허용됩니다: " + ct);

      String filename = "p_" + i + ".jpg"; // 모두 jpeg로 재인코딩
      Path out = dir.resolve(filename);

      try (InputStream in = f.getInputStream(); OutputStream os = Files.newOutputStream(out)) {
        // 메타 제거 + 리사이즈(긴 변 2048px, 원본 작으면 그대로)
        Thumbnails.of(in)
            .size(2048, 2048)
            .outputFormat("jpg")
            .outputQuality(0.92)
            .toOutputStream(os);
      }

      // 브라우저 접근 URL
      String url = "/static/items/" + itemId + "/" + filename;
      urls.add(url);
    }
    return urls;
  }

  /** EXIF 촬영시각(가장 대표 컷 기준) 추출 → 없으면 null */
  public LocalDateTime readExifDateTime(MultipartFile file) {
    try (InputStream in = file.getInputStream()) {
      Metadata metadata = ImageMetadataReader.readMetadata(in);
      ExifSubIFDDirectory dir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
      if (dir != null) {
        Date dt = dir.getDateOriginal();
        if (dt != null) {
          return LocalDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault());
        }
      }
    } catch (Exception ignore) {}
    return null;
  }
}
