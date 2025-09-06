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
public interface PriceRepository extends JpaRepository<Orders, Long> {
	@Query("""
			select new com.pingu.DOTORI.dto.PriceDTO(
				d.itemId,
				o.payTime,
				d.cost
			)
			from Orders o
			join o.itemDetails d
			where d.id = :itemId
			  and (:from is null or o.payTime >= :from)
			  and (:to is null or o.payTime < :to)
			order by o.payTime asc
			""")
	List<PriceDTO> findPriceHistoryByItemId(
			@Param("itemId") Long itemId,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	@Query("""
			select new com.pingu.DOTORI.dto.PriceDTO(
			d.itemId,
			o.payTime,
			d.cost
			)
			from Orders o
			join o.itemDetails d
			join d.item i
			where i.itemCode = :itemCode
			  and (:from is null or o.payTime >= :from)
			  and (:to is null or o.payTime < :to)
			  order by o.payTime asc
			""")
	List<PriceDTO> findPriceHistoryByItemCode(
			@Param("itemCode") String itemCode,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);
}
