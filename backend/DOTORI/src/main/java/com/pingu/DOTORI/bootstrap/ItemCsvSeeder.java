package com.pingu.DOTORI.bootstrap;

// 데이터 넣기 위한 코드
import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ItemCsvSeeder implements CommandLineRunner {

    private final ItemRepository repo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        var resource = new ClassPathResource("items_from_text_v3.csv");

        try (InputStream in = resource.getInputStream();
                var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {

            Iterable<CSVRecord> rows = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withTrim()
                    .parse(reader);

            for (CSVRecord r : rows) {
                String itemCode = nullIfBlank(r.get("item_code"));
                String name = nullIfBlank(r.get("name"));
                String title = nullIfBlank(r.get("title"));
                String manu = nullIfBlank(r.get("manufacturer"));
                String material = nullIfBlank(r.get("material")); // = Texture
                String relMonth = nullIfBlank(r.get("release_month")); // YYYY-MM
                String size = nullIfBlank(r.get("size"));
                String info = nullIfBlank(r.get("information"));
                String imgUrl = nullIfBlank(r.get("img_url"));
                String costStr = nullIfBlank(r.get("cost"));
                String feesStr = nullIfBlank(r.get("storage_fees"));
                String genre = nullIfBlank(r.get("genre"));

                LocalDate releaseDate = (relMonth == null) ? null : LocalDate.parse(relMonth + "-01");
                Long storageFees = (feesStr == null) ? 3000L : Long.parseLong(feesStr);
                BigDecimal cost = (costStr == null) ? null : new BigDecimal(costStr);

                Item item = Item.builder()
                        // 👉 아직 엔티티 @Id가 ean이면 아래처럼:
                        .itemCode(itemCode)
                        // 👉 ean을 itemCode로 리팩터했으면 위 줄 대신 .itemCode(itemCode) 사용
                        .name(name)
                        .title(title)
                        .manufacturer(manu)
                        .material(material) // material -> texture 매핑
                        .releaseMonth(releaseDate)
                        .size(size) // size는 NULL 허용 권장
                        .information(info)
                        .imgUrl(imgUrl)
                        .storageFees(storageFees)
                        .genre(genre)
                        .build();

                // 선택: cost 필드가 엔티티에 있으면 세팅
                try {
                    var f = Item.class.getDeclaredField("cost");
                    f.setAccessible(true);
                    f.set(item, cost);
                } catch (NoSuchFieldException ignore) {
                }

                // 단순 삽입(동일 PK 존재 시 업데이트 하려면 exists 체크 후 merge 로직 추가)
                repo.save(item);
            }
        }
        // 한 번 적재 후 자동 재실행을 막고 싶다면, app.seed.enabled=false 로 변경해 주세요.
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
