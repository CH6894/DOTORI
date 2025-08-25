package com.pingu.DOTORI.dto;

import com.pingu.DOTORI.entity.Item;

public class ItemMapper {
	public static ItemDTO toDTO(Item i) {
		return ItemDTO.builder()
				.ean(i.getEan())
				.name(i.getName())
				.title(i.getTitle())
				.manufacturer(i.getManufacturer())
				.texture(i.getTexture())
				.size(i.getSize())
				.imgUrl(i.getImgUrl())
				.storageFees(i.getStorageFees())
				.genre(i.getGenre())
				.build();
	}
}