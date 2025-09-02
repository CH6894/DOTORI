package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.CollectionMapping;
import com.pingu.DOTORI.entity.CollectionMapping.Pk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.pingu.DOTORI.repository.DexItemProjection;

import java.util.List;
import java.util.Optional;

public interface CollectionMappingRepository extends JpaRepository<CollectionMapping, Pk> {
    List<CollectionMapping> findByCollectionId(Long collectionId);
    Optional<CollectionMapping> findByCollectionIdAndItemCode(Long collectionId, String itemCode);

    /**
     * dex-state 전용 최소 필드 조회(ENUM 파싱 회피)
     */
    @Query(value = "SELECT Item_Code AS itemCode, Is_Own AS isOwn, Is_Certified AS isCertified FROM Collection_Mapping WHERE Collection_ID = :collectionId", nativeQuery = true)
    List<DexItemProjection> findDexItemsByCollectionId(@Param("collectionId") Long collectionId);
}
