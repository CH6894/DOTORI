package com.pingu.DOTORI.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pingu.DOTORI.dto.PriceDTO;
import com.pingu.DOTORI.repository.PriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository priceRepository;

    public List<PriceDTO> getPrice(Long itemId, LocalDateTime from, LocalDateTime to) {
        return priceRepository.findPriceHistoryByItemId(itemId, from, to);
    }
    
    public List<PriceDTO> getPriceByCode(String itemCode, LocalDateTime from, LocalDateTime to) {
    	return priceRepository.findPriceHistoryByItemCode(itemCode, from, to);
    }

    // 기간별 시세 조회 (1개월, 3개월, 6개월, 1년)
    public List<PriceDTO> getPriceHistoryByPeriod(Long itemId, String period) {
        LocalDateTime from = switch (period) {
            case "1M" -> LocalDateTime.now().minusMonths(1);
            case "3M" -> LocalDateTime.now().minusMonths(3);
            case "6M" -> LocalDateTime.now().minusMonths(6);
            case "1Y" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusMonths(1); // 기본값
        };

        return priceRepository.findPriceHistoryByItemId(itemId, from, null);
    }
    
    public List<PriceDTO> getPriceByCode(String itemCode, String period) {
    	LocalDateTime from = switch (period) {
    	case "1M" -> LocalDateTime.now().minusMonths(1);
    	case "3M" -> LocalDateTime.now().minusMonths(3);
    	case "6M" -> LocalDateTime.now().minusMonths(6);
    	case "1Y" -> LocalDateTime.now().minusYears(1);
    	default -> LocalDateTime.now().minusMonths(1); // 기본값
    	};
    	
    	return priceRepository.findPriceHistoryByItemCode(itemCode, from, null);
    }
    
   

}
