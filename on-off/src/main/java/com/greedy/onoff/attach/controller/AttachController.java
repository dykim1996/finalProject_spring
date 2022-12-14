package com.greedy.onoff.attach.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.greedy.onoff.append.dto.AppendDto;
import com.greedy.onoff.attach.dto.AttachDto;
import com.greedy.onoff.attach.service.AttachService;
import com.greedy.onoff.classes.dto.OpenClassesDto;
import com.greedy.onoff.common.ResponseDto;
import com.greedy.onoff.common.paging.Pagenation;
import com.greedy.onoff.common.paging.PagingButtonInfo;
import com.greedy.onoff.common.paging.ResponseDtoWithPaging;
import com.greedy.onoff.member.dto.MemberDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j  
@RestController
@RequestMapping("/ono")
public class AttachController {
	
	private final AttachService attachService;
	
	public AttachController(AttachService attachService) {
		this.attachService = attachService;
	}

	

	/*1. 내강의 목록조회(강사) - 페이징 , 로그인한 강사의 해당수업 강의만 조회  */
	
	@GetMapping("/teacherclass")
	public ResponseEntity<ResponseDto> myclassListForTeacher(@RequestParam(name="page", defaultValue="1")int page, @AuthenticationPrincipal MemberDto teacher) {
		
		log.info("[AttachController] myclassList Start=================================" );
		log.info("[AttachController] page :{}", page);
		log.info("[AttachController] teacher :{}", teacher);
		
		Page<OpenClassesDto> classesDtoList = attachService.findbyClassesforteacher(page, teacher);
		
		PagingButtonInfo pageInfo = Pagenation.getPagingButtonInfo(classesDtoList);
		log.info("[ProductController] pageInfo : {}", pageInfo);
		
		ResponseDtoWithPaging responseDtoWithPaging = new ResponseDtoWithPaging();
		responseDtoWithPaging.setPageInfo(pageInfo);
		responseDtoWithPaging.setData(classesDtoList.getContent());
		
		
		log.info("[AttachController] myclassList End===================================" );
		
		return ResponseEntity.ok().body(new ResponseDto(HttpStatus.OK, "조회성공", responseDtoWithPaging));
	
	
	}
	
	
	
	/*2. 내강의 목록 상세조회(강사)*/
	@GetMapping("/myclass/{classCode}") 
	public ResponseEntity<ResponseDto> myclassDetail(@PathVariable Long classCode) {
		
		return ResponseEntity
				.ok()
				.body(new ResponseDto(HttpStatus.OK,"내강의 상세 정보 조회 성공", attachService.selectMyclass(classCode)));
	}
	
	
	
	/*3. 강의 자료 공유 등록 */
	
	@PostMapping("/myclass/{classCode}/attach")                 //폼데이타 형태로 들어오는 어노테이션
	public ResponseEntity<ResponseDto> attachRegist(@ModelAttribute AttachDto attachRegist ){
		
		
		log.info("[ATTACHREGIST] attachRegist : {}", attachRegist);

		return ResponseEntity.ok().body(new ResponseDto(HttpStatus.OK, "파일첨부 등록 성공", attachService.attachRegist(attachRegist)));
		 
	}
	
	

	
	
//	/*4. 강의 자료 조회 */
//	
//
//	@GetMapping("/myclass/{classCode}")
//	public ResponseEntity<ResponseDto> attachSearch(@PathVariable AttachDto classCode ){
//		
//		AttachDto attachSearchList = attachService.findByattachSearch(classCode.getClassCode()); 
//		
//		return ResponseEntity.ok().body(new ResponseDto(HttpStatus.OK,"조회성공", attachService.findByattachSearch(classCode.getClassCode())));
//		
//	}
//	
	
	
	/*5. 강의 자료 수정,삭제 */
	
	
	
	
	
	/*6. 강의 시간표 조회 */
	
	
	
	
	/*7. 원생정보 조회 */
	
	@GetMapping("/studentinfo")
	public ResponseEntity<ResponseDto> studentslist(@RequestParam(name="page", defaultValue="1")int page, String memberName ){
		
		
		Page<MemberDto> studentDtoList = attachService.findBystudentList(page, memberName);
		
		PagingButtonInfo pageInfo = Pagenation.getPagingButtonInfo(studentDtoList);
		
		ResponseDtoWithPaging responseDtoWithPaging = new ResponseDtoWithPaging();
		responseDtoWithPaging.setPageInfo(pageInfo);
		responseDtoWithPaging.setData(studentDtoList.getContent());
				
		return ResponseEntity.ok().body(new ResponseDto(HttpStatus.OK, "조회성공", responseDtoWithPaging));
	}
	
	
	
	/*8. 원생정보 상세 조회 */
	
	
	
	
	
	
	/*9. 상담관리 */
	
	
	
	
	
	/*10. 상담관리 내역 조회 */
	
	
	
	

}

