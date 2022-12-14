package com.greedy.onoff.member.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.greedy.onoff.member.dto.MemberDto;
import com.greedy.onoff.member.entity.Member;
import com.greedy.onoff.member.exception.DuplicateMemberEmailException;
import com.greedy.onoff.member.exception.DuplicateMemberIdException;
import com.greedy.onoff.member.repository.MemberInsertRepository;
import com.greedy.onoff.member.repository.MemberRepository;
import com.greedy.onoff.teacher.dto.TeacherHistoryDto;
import com.greedy.onoff.teacher.entity.TeacherHistory;
import com.greedy.onoff.teacher.repository.TeacherHistoryRepository;
import com.greedy.onoff.util.FileUploadUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MemberService {
	
	@Value("${image.image-dir}")
	private String IMAGE_DIR;
	@Value("${image.image-url}")
	private String IMAGE_URL;
	
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;
	private final TeacherHistoryRepository teacherHistoryRepository;
	private final MemberInsertRepository memberInsertRepository;
	
	public MemberService(ModelMapper modelMapper,  PasswordEncoder passwordEncoder, MemberRepository memberRepository, 
			TeacherHistoryRepository teacherHistoryRepository,  MemberInsertRepository memberInsertRepository) {
		this.modelMapper = modelMapper;
		this.passwordEncoder = passwordEncoder;
		this.memberRepository = memberRepository;
		this.teacherHistoryRepository = teacherHistoryRepository;
		this.memberInsertRepository = memberInsertRepository;
		
	}
	
	/* ?????? ?????? */
	@Transactional
	public MemberDto teacherRegister(MemberDto memberDto) {
		
		log.info("[MemberService] teacherRegister Start ============================");
		log.info("[MemberService] memberDto : {}", memberDto);
		String imageName = UUID.randomUUID().toString().replace("-", "");
		String replaceFileName = null;
		
		if(memberInsertRepository.findByMemberId(memberDto.getMemberId()) != null) {
			log.info("[MemberService] ???????????? ???????????????.");
			throw new DuplicateMemberIdException("???????????? ???????????????.");
		}
		
		if(memberInsertRepository.findByMemberEmail(memberDto.getMemberEmail()) != null) {
			throw new DuplicateMemberEmailException("???????????? ???????????????.");
		}
		
		try {
			replaceFileName = FileUploadUtils.saveFile(IMAGE_DIR, imageName, memberDto.getMemberImage());
			memberDto.setMemberImageUrl(replaceFileName);
			log.info("[TeacherService] replaceFileName : {}", replaceFileName);
			
			
			memberDto.setMemberPassword(passwordEncoder.encode(memberDto.getMemberPassword()));
			memberDto.setMemberRole("ROLE_TEACHER");
			memberRepository.save(modelMapper.map(memberDto, Member.class));
	
		} catch (IOException e) {
			e.printStackTrace();
			 try {
				 FileUploadUtils.deleteFile(IMAGE_DIR, replaceFileName);
			} catch (IOException e1) {
				e1.printStackTrace();
			}	 
		}
		

		/* ??????????????????*/
		TeacherHistoryDto teacherHistoryDto = new TeacherHistoryDto();
		teacherHistoryDto.setJoinDate(memberDto.getMemberRegisterDate());
		/* ?????? ???????????? ?????? ?????? ?????? */
		Long memberCode = memberRepository.getCurrvalMemberCodeSequence();
		MemberDto memberdto = new MemberDto();
		memberdto.setMemberCode(memberCode);
		teacherHistoryDto.setMember(memberdto);	
		teacherHistoryRepository.save(modelMapper.map(teacherHistoryDto, TeacherHistory.class));
		

		return memberDto;
	}





	

	/* 1. ?????? ?????? ?????? - ?????????, ?????? 'N' ??????, Role teacher (?????????) */

	public Page<MemberDto> selectTeacherListForAdmin(int page) {
		log.info("[MemberService] selectTeacherListForAdmin Start =====================" );

		
		Pageable pageable = PageRequest.of(page - 1, 7, Sort.by("memberCode").descending());

		Page<Member> memberList = memberRepository.findByMemberRole(pageable,"ROLE_TEACHER");
		Page<MemberDto> memberDtoList = memberList.map(member -> modelMapper.map(member, MemberDto.class));
		
		memberDtoList.forEach(member -> member.setMemberImageUrl(IMAGE_URL + member.getMemberImageUrl()));
		
		log.info("[MemberService] memberDtoList : {}", memberDtoList.getContent());
		
		log.info("[MemberService] selectTeacherListForAdmin End =====================" );
		
		return memberDtoList;
	}

	
	/* ?????? ?????? ?????? -  ?????? ?????? 'N'??????, ????????? X (?????????)*/
	public List<MemberDto> selectTeacherListForAdmin() {
			

			List<Member> memberList = memberRepository.findByMemberRole("ROLE_TEACHER");
			List<MemberDto> memberDtoList = memberList.stream()
			.map(member -> modelMapper.map(member, MemberDto.class))
			.collect(Collectors.toList());
			
			
			log.info("[MemberService] memberDtoList End =====================" );
			
			return memberDtoList;
		}	
		
	
	/* ???????????? ?????? - ?????? ?????? ??????, ?????????, ?????? 'N' ??????, Role teacher (?????????) */
	public Page<MemberDto> selectTeacherListByMemberName(int page, String memberName) {
		
		log.info("[MemberService] selectTeacherListByMemberName Start =====================" );
		
		Pageable pageable = PageRequest.of(page - 1, 9, Sort.by("memberCode").descending());
		
		Page<Member> memberList = memberRepository.findByMemberNameContainsAndMemberRole(pageable, memberName,"ROLE_TEACHER");
		Page<MemberDto> memberDtoList = memberList.map(member -> modelMapper.map(member, MemberDto.class));
		/* ??????????????? ????????? ????????? ?????? ??? ????????? ?????? ??? ????????? ????????? ?????? */
		memberDtoList.forEach(member -> member.setMemberImageUrl(IMAGE_URL + member.getMemberImageUrl()));
		
		log.info("[MemberService] memberDtoList : {}", memberDtoList.getContent());
		
		log.info("[MemberService] selectTeacherListByMemberName End =====================" );
		
		return memberDtoList;
	}

	/* ?????? ?????? ?????? - memberCode??? ?????? 1??? ?????? , ???????????? 'N' ?????? (?????????) */
	public MemberDto selectTeacherForAdmin(Long memberCode) { 
        log.info("[MemberService] selectTeacherForAdmin Start ===================================");
        log.info("[MemberService] memberCode : " + memberCode);
        
        Member member = memberRepository.findById(memberCode)
        		.orElseThrow(() -> new IllegalArgumentException("?????? ????????? ????????????. memberCode=" + memberCode));
        MemberDto memberDto = modelMapper.map(member, MemberDto.class);
        memberDto.setMemberImageUrl(IMAGE_URL + memberDto.getMemberImageUrl());
        
        log.info("[MemberService] memberDto : " + memberDto);
        
        log.info("[MemberService] selectTeacherForAdmin End ===================================");
        
        return memberDto;
	}



	/* 8. ?????? ?????? */
	@Transactional
	public MemberDto updateMember(MemberDto memberDto) {

		log.info("[MemberService] updateMember Start ===================================");
		log.info("[MemberService] memberDto : {}", memberDto);
		
		Date date = new Date();
		long timeInMilliSeconds = date.getTime();
		java.sql.Date sqlDate = new java.sql.Date(timeInMilliSeconds);
		log.info("SQL Date: " + sqlDate);
		
		
		String replaceFileName = null;

		try {

			Member oriMember = memberRepository.findById(memberDto.getMemberCode()).orElseThrow(
					() -> new IllegalArgumentException("?????? ????????? ????????????. memberCode=" + memberDto.getMemberCode()));
			String oriImage = oriMember.getMemberImageUrl();

			/* ???????????? ???????????? ?????? */
			if (memberDto.getMemberImage() != null) {
					
				/* ?????? ?????? ??? ????????? ?????? */
				String imageName = UUID.randomUUID().toString().replace("-", "");
				replaceFileName = FileUploadUtils.saveFile(IMAGE_DIR, imageName, memberDto.getMemberImage());
				memberDto.setMemberImageUrl(replaceFileName);
				if(oriImage != null) 
				/* ????????? ?????? ??? ????????? ??????*/
				FileUploadUtils.deleteFile(IMAGE_DIR, oriImage);

			} else { 
				/* ???????????? ???????????? ?????? ?????? */
				memberDto.setMemberImageUrl(oriImage);
			}
			

			/* ????????? ???????????? ???????????? ????????? ????????? ?????? */
			if(memberDto.getMemberStatus().equals("N") && oriMember.getMemberStatus().equals("Y") )
			{
				log.info("=============if ??? ?????????========");
				/* ???????????? ???????????? ??????????????? ???????????????.*/
				List<TeacherHistory> teacherHistoryList = teacherHistoryRepository.findAll();
				TeacherHistory foundTeacherHistory = teacherHistoryList.stream()
					        .filter(h -> h.getMember().getMemberCode() == memberDto.getMemberCode()&& h.getRetirementDate() == (null))
					        .findFirst()
					        .orElseThrow(() -> new IllegalArgumentException());
				log.info(foundTeacherHistory.toString());
				TeacherHistory oriTeacherHistory = teacherHistoryRepository.findByHistoryCode(foundTeacherHistory.getHistoryCode())
						.orElseThrow(() -> new IllegalArgumentException("?????? ??????????????? ????????????. historyCode =" + foundTeacherHistory.getHistoryCode()));
				
				oriTeacherHistory.update(sqlDate);
				
				teacherHistoryRepository.save(oriTeacherHistory);
				log.info("=======if ??? ?????? ========");
			}

			/* ???????????? ????????? ?????? ????????? ????????? ??? */
			// ?????? ?????? ???????????? ???????????? ???????????????. 
			if(memberDto.getMemberStatus().equals("Y") && oriMember.getMemberStatus().equals("N"))
			{
				TeacherHistoryDto teacherHistoryDto = new TeacherHistoryDto();
				/* ?????? ????????? ???????????? ????????????.*/
				memberDto.setMemberRegisterDate(sqlDate);
				teacherHistoryDto.setJoinDate(memberDto.getMemberRegisterDate());
				teacherHistoryDto.setMember(memberDto);			
				teacherHistoryRepository.save(modelMapper.map(teacherHistoryDto, TeacherHistory.class));
			}
			
			/* ?????? ?????? ?????? ???????????? ????????? ?????? */
			oriMember.update(memberDto.getMemberName(), 
					memberDto.getMemberPhone(), 
					memberDto.getMemberGender(),
					memberDto.getMemberBirthday(), 
					memberDto.getMemberEmail(), 
					memberDto.getMemberStatus(),
					memberDto.getMemberAddress(),
					memberDto.getMemberImageUrl(),
					memberDto.getMemberRegisterDate());
			
			
					memberRepository.save(oriMember);
			
		} catch (IOException e) {
			e.printStackTrace();
			try {
				FileUploadUtils.deleteFile(IMAGE_DIR, replaceFileName);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		log.info("[MemberService] updateMember End ===================================");

		return memberDto;
	}


	

}