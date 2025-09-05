package com.pingu.DOTORI.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pingu.DOTORI.dto.PriceDTO;
import com.pingu.DOTORI.entity.Orders;

@Repository
public interface PriceRepository extends JpaRepository<Orders, Long>{
	@Query("""
			select new com.pingu.DOTORI.dto.PriceDTO(
				id.id,
				o.payTime,
				id.cost
			)
			from Orders o
			join o.itemDetails id
			where id.id = :itemId
			  and (:from is null or o.payTime >= :from)
			  and (:to is null or o.payTime < : to)
			order by o.payTime asc
			""")
	List<PriceDTO> findPriceHistoryByItemCode(
			@Param("itemCode") String itemCode,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to
			);
}
