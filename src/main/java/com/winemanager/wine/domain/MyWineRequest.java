package com.winemanager.wine.domain;

import com.winemanager.wine.util.Pagination;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class MyWineRequest {
	private String keyword; // 와인 이름 ((추가 예정))
	
	private String userId;
	
	private String type; // 와인 타입 all, red, white, sparkling, rose
	private String sortBy; // reg_, name_, price_, count_  +  asc or desc
	private boolean hideSoldOut;
	
	private int page;             // 현재 페이지 번호
    private int recordSize;       // 페이지당 출력할 데이터 개수
    private int pageSize;         // 화면 하단에 출력할 페이지 사이즈
    
    private Pagination pagination;
    
    // 생성자에서 기본값 설정
    public MyWineRequest() {
        this.page = 1;
        this.recordSize = 20;
        this.pageSize = 5;
        this.sortBy = "price_asc";
    }
    
}
