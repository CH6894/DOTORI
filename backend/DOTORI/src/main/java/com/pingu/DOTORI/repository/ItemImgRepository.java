package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {

  /** 아이템의 모든 이미지: 대표(0) 먼저, 그 다음 id 오름차순 */
  List<ItemImg> findByItemDetails_ItemIdOrderByImgTypeAscIdAsc(Long itemId);

  /** 대표 이미지 한 장(있으면) */
  Optional<ItemImg> findFirstByItemDetails_ItemIdAndImgTypeOrderByIdAsc(Long itemId, Byte imgType);

  /** 이미지 총 개수 */
  long countByItemDetails_ItemId(Long itemId);

  /** 가장 이른 촬영 시각 */
  @Query("select min(i.filmingTime) from ItemImg i where i.itemDetails.itemId = :itemId")
  Optional<LocalDateTime> findEarliestFilmingTime(@Param("itemId") Long itemId);
}
